package com.ddd.manage_attendance.domain.auth.infrastructure.jwt;

import com.ddd.manage_attendance.domain.auth.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenProvider {

    private final SecretKey key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public TokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
    }

    public String createAccessToken(Long userId, UserRole role) {
        return createToken(String.valueOf(userId), role, accessTokenValidityInMilliseconds);
    }

    public String createRefreshToken(Long userId) {
        return createToken(String.valueOf(userId), null, refreshTokenValidityInMilliseconds);
    }

    private String createToken(String subject, UserRole role, long validity) {
        long now = (new Date()).getTime();
        Date validityDate = new Date(now + validity);

        var builder =
                Jwts.builder()
                        .subject(subject)
                        .issuedAt(new Date())
                        .expiration(validityDate)
                        .signWith(key);

        if (role != null) {
            builder.claim("role", role.name());
        }

        return builder.compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public String getRole(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return claims.get("role", String.class);
    }
}
