package com.example.community.security.jwt;

import com.example.community.domain.user.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity-seconds}")
    private long accessTokenValiditySeconds;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(
            Long userId,
            Long studentId,
            UserRole role,
            String sessionId
    ) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(accessTokenValiditySeconds);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .id(sessionId)
                .claim("studentId", studentId)
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public String getSessionId(String token) {
        return parseClaims(token).getId();
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public long getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}