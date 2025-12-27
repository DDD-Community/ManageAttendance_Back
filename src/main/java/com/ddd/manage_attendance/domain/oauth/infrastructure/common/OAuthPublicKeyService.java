package com.ddd.manage_attendance.domain.oauth.infrastructure.common;

import static com.ddd.manage_attendance.core.common.util.Base64Util.decodeBase64UrlToBigInteger;
import static com.ddd.manage_attendance.domain.oauth.infrastructure.common.JWKConstants.*;

import com.ddd.manage_attendance.domain.oauth.exception.OAuthTokenValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class OAuthPublicKeyService {
    private static final ECParameterSpec EC_PARAMETER_SPEC = createECParameterSpec();
    private static final long CACHE_TTL_SECONDS = 86400;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, CachedPublicKey> publicKeyCache = new ConcurrentHashMap<>();

    public PublicKey getPublicKey(String kid, String publicKeyUrl, String provider) {
        String cacheKey = provider + ":" + kid;
        CachedPublicKey cachedKey = publicKeyCache.get(cacheKey);

        if (cachedKey != null && !isExpired(cachedKey)) {
            return cachedKey.publicKey();
        }

        return fetchAndCachePublicKey(kid, publicKeyUrl, provider, cacheKey);
    }

    private synchronized PublicKey fetchAndCachePublicKey(
            String kid, String publicKeyUrl, String provider, String cacheKey) {
        CachedPublicKey cachedKey = publicKeyCache.get(cacheKey);
        if (cachedKey != null && !isExpired(cachedKey)) {
            return cachedKey.publicKey();
        }

        PublicKey publicKey = fetchPublicKey(kid, publicKeyUrl, provider);
        publicKeyCache.put(
                cacheKey, new CachedPublicKey(publicKey, Instant.now().getEpochSecond()));
        return publicKey;
    }

    private boolean isExpired(CachedPublicKey cachedKey) {
        return Instant.now().getEpochSecond() - cachedKey.cachedTime() > CACHE_TTL_SECONDS;
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
            List<Map<String, Object>> keys = (List<Map<String, Object>>) keysResponse.get(KEYS);

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
                .filter(key -> kid.equals(key.get(KID)))
                .findFirst()
                .map(keyData -> buildPublicKey(keyData, provider))
                .orElseThrow(
                        () ->
                                new OAuthTokenValidationException(
                                        provider, "kid에 해당하는 공개키를 찾을 수 없습니다: " + kid));
    }

    private PublicKey buildPublicKey(Map<String, Object> keyData, String provider) {
        String kty = (String) keyData.get(KTY);
        if (EC_ALGORITHM.equals(kty)) {
            return buildECPublicKey(keyData, provider);
        } else if (RSA_ALGORITHM.equals(kty)) {
            return buildRSAPublicKey(keyData, provider);
        } else {
            throw new OAuthTokenValidationException(provider, "지원하지 않는 키 타입입니다: " + kty);
        }
    }

    private PublicKey buildRSAPublicKey(Map<String, Object> keyData, String provider) {
        try {
            String modulusBase64 = (String) keyData.get(MODULUS);
            String exponentBase64 = (String) keyData.get(EXPONENT);

            BigInteger modulus = decodeBase64UrlToBigInteger(modulusBase64);
            BigInteger exponent = decodeBase64UrlToBigInteger(exponentBase64);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new OAuthTokenValidationException(provider, "RSA 공개키 생성에 실패했습니다.", e);
        }
    }

    private PublicKey buildECPublicKey(Map<String, Object> keyData, String provider) {
        String crv = (String) keyData.get(CRV);
        if (!EC_CURVE_P256.equals(crv)) {
            throw new OAuthTokenValidationException(provider, "지원하지 않는 곡선입니다: " + crv);
        }

        try {
            String xBase64 = (String) keyData.get(X);
            String yBase64 = (String) keyData.get(Y);

            BigInteger x = decodeBase64UrlToBigInteger(xBase64);
            BigInteger y = decodeBase64UrlToBigInteger(yBase64);
            ECPoint point = new ECPoint(x, y);

            ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(point, EC_PARAMETER_SPEC);
            KeyFactory keyFactory = KeyFactory.getInstance(EC_ALGORITHM);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            throw new OAuthTokenValidationException(provider, "EC 공개키 생성에 실패했습니다.", e);
        }
    }

    private static ECParameterSpec createECParameterSpec() {
        try {
            AlgorithmParameters parameters = AlgorithmParameters.getInstance(EC_ALGORITHM);
            parameters.init(new ECGenParameterSpec(EC_CURVE_SECP256R1));
            return parameters.getParameterSpec(ECParameterSpec.class);
        } catch (Exception e) {
            throw new RuntimeException("EC ParameterSpec 생성에 실패했습니다.", e);
        }
    }

    private record CachedPublicKey(PublicKey publicKey, long cachedTime) {}
}
