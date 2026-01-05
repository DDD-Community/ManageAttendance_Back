package com.ddd.manage_attendance.domain.oauth.infrastructure.apple;

import com.ddd.manage_attendance.domain.oauth.domain.OAuthService;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import com.ddd.manage_attendance.domain.oauth.domain.dto.OAuthRevocationRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleOAuthService implements OAuthService {
    @Value("${apple.auth.service-id}")
    private String clientId;

    @Value("${apple.auth.team-id}")
    private String teamId;

    @Value("${apple.auth.key-id}")
    private String keyId;

    @Value("${apple.auth.private-key}")
    private String privateKeyString;

    private final AppleTokenValidator appleTokenValidator;

    @Override
    public OAuthUserInfo authenticate(String codeOrToken) {
        // 입력값이 JWT(ID Token) 형식인지 확인 (간단히 점 2개 포함 여부로 판단)
        if (codeOrToken != null && codeOrToken.split("\\.").length == 3) {
            return (AppleUserInfo) appleTokenValidator.validate(codeOrToken);
        }

        String clientSecret = createClientSecret();
        AppleTokenResponse tokenResponse = getAppleToken(codeOrToken, clientSecret);
        if (tokenResponse == null || tokenResponse.idToken() == null) {
            throw new RuntimeException("Apple ID Token 발급 실패");
        }
        AppleUserInfo userInfo =
                (AppleUserInfo) appleTokenValidator.validate(tokenResponse.idToken());
        userInfo.setRefreshToken(tokenResponse.refreshToken());
        return userInfo;
    }

    @Override
    public void revoke(OAuthRevocationRequest request) {
        // 우선순위: DB에 저장된 Refresh Token > 클라이언트가 준 Token
        String tokenToRevoke =
                (request.refreshTokenFromDb() != null)
                        ? request.refreshTokenFromDb()
                        : request.tokenFromClient();

        if (tokenToRevoke == null || tokenToRevoke.isBlank()) {
            log.warn("Apple Revoke failed: No token provided.");
            return;
        }

        try {
            String clientSecret = createClientSecret();
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://appleid.apple.com/auth/revoke";

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);
            map.add("token", tokenToRevoke);
            map.add("token_type_hint", "refresh_token");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> httpRequest = new HttpEntity<>(map, headers);

            restTemplate.postForLocation(url, httpRequest);
        } catch (Exception e) {
            throw new RuntimeException("Apple OAuth 철회 중 오류가 발생했습니다. cause: " + e.getMessage(), e);
        }
    }

    private AppleTokenResponse getAppleToken(String code, String clientSecret) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://appleid.apple.com/auth/token";

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("code", code);
        map.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> httpRequest = new HttpEntity<>(map, headers);

        try {
            return restTemplate.postForObject(url, httpRequest, AppleTokenResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Apple Token 교환 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private String createClientSecret() {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 300000); // 5 minutes

        return Jwts.builder()
                .header()
                .add("kid", keyId)
                .and()
                .issuer(teamId)
                .issuedAt(now)
                .expiration(expiration)
                .audience()
                .add("https://appleid.apple.com")
                .and()
                .subject(clientId)
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();
    }

    private PrivateKey getPrivateKey() {
        try {
            String privateKeyContent =
                    privateKeyString
                            .replace("\\n", "")
                            .replace("-----BEGIN PRIVATE KEY-----", "")
                            .replace("-----END PRIVATE KEY-----", "")
                            .replaceAll("\\s+", "");

            byte[] encoded = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Apple Private Key 파싱 실패. 키 값을 확인해주세요.", e);
        }
    }

    // 내부 DTO
    record AppleTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") Integer expiresIn,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("id_token") String idToken) {}
}
