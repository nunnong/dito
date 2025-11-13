package com.ssafy.Dito.global.jwt.util;

import com.ssafy.Dito.global.security.environment.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final SecurityProperties securityProperties;

    private SecretKey key;
    private long accessTokenValidityMs;
    private long refreshTokenValidityMs;

    @PostConstruct
    private void init() {
        this.key = Keys.hmacShaKeyFor(
                securityProperties.jwt().access().secret().getBytes(StandardCharsets.UTF_8)
        );
        this.accessTokenValidityMs = securityProperties.jwt().access().expiration();
        this.refreshTokenValidityMs = securityProperties.jwt().refresh().expirationDefault();
    }


    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidityMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidityMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public JwtClaims validateAndGetClaims(String token) throws JwtException {
        return new JwtClaims(Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody());
    }

    public long getRemainingTime(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    public long getRefreshTokenValidityMs() {
        return refreshTokenValidityMs;
    }
}
