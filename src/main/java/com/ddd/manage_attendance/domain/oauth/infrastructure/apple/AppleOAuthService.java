package com.ddd.manage_attendance.domain.oauth.infrastructure.apple;

import com.ddd.manage_attendance.domain.oauth.domain.OAuthService;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
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
    public OAuthUserInfo authenticate(String idToken) {
        return appleTokenValidator.validate(idToken);
    }

    @Override
    public void revoke(String token) {
        try {
            String clientSecret = createClientSecret();
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://appleid.apple.com/auth/revoke";

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);
            map.add("token", token);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            restTemplate.postForLocation(url, request);
        } catch (Exception e) {
            throw new RuntimeException("Apple OAuth 철회 중 오류가 발생했습니다. cause: " + e.getMessage(), e);
        }
    }

    private String createClientSecret() throws NoSuchAlgorithmException, InvalidKeySpecException {
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

    private PrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
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
    }
}
