package com.ddd.manage_attendance.domain.oauth.infrastructure.common;

import static com.ddd.manage_attendance.core.common.util.Base64Util.decodeBase64UrlToBigInteger;

import com.ddd.manage_attendance.domain.oauth.exception.OAuthTokenValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class OAuthPublicKeyService {
    private static final String KEYS_KEY = "keys";
    private static final String KID_KEY = "kid";
    private static final String MODULUS_KEY = "n";
    private static final String EXPONENT_KEY = "e";
    private static final String RSA_ALGORITHM = "RSA";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, PublicKey> publicKeyCache = new ConcurrentHashMap<>();

    public PublicKey getPublicKey(String kid, String publicKeyUrl, String provider) {
        String cacheKey = provider + ":" + kid;
        return publicKeyCache.computeIfAbsent(
                cacheKey, k -> fetchPublicKey(kid, publicKeyUrl, provider));
    }

    private PublicKey fetchPublicKey(String kid, String publicKeyUrl, String provider) {
        if (kid == null || kid.trim().isEmpty()) {
            throw new OAuthTokenValidationException(provider, "kid가 비어있습니다.");
        }

        try {
            String response = restTemplate.getForObject(publicKeyUrl, String.class);
            if (response == null || response.trim().isEmpty()) {
                throw new OAuthTokenValidationException(provider, "공개키 응답이 비어있습니다.");
            }

            Map<String, Object> keysResponse = objectMapper.readValue(response, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> keys = (List<Map<String, Object>>) keysResponse.get(KEYS_KEY);

            if (keys == null || keys.isEmpty()) {
                throw new OAuthTokenValidationException(provider, "공개키 목록이 비어있습니다.");
            }

            return findKeyByKid(keys, kid, provider);
        } catch (OAuthTokenValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new OAuthTokenValidationException(provider, "공개키 조회에 실패했습니다.", e);
        }
    }

    private PublicKey findKeyByKid(List<Map<String, Object>> keys, String kid, String provider) {
        return keys.stream()
                .filter(key -> kid.equals(key.get(KID_KEY)))
                .findFirst()
                .map(keyData -> buildPublicKey(keyData, provider))
                .orElseThrow(
                        () ->
                                new OAuthTokenValidationException(
                                        provider, "kid에 해당하는 공개키를 찾을 수 없습니다: " + kid));
    }

    private PublicKey buildPublicKey(Map<String, Object> keyData, String provider) {
        try {
            String modulusBase64 = (String) keyData.get(MODULUS_KEY);
            String exponentBase64 = (String) keyData.get(EXPONENT_KEY);

            BigInteger modulus = decodeBase64UrlToBigInteger(modulusBase64);
            BigInteger exponent = decodeBase64UrlToBigInteger(exponentBase64);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new OAuthTokenValidationException(provider, "공개키 생성에 실패했습니다.", e);
        }
    }
}
