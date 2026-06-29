package com.example.gaokaoproject.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration}")
    private Long accessExpiration;

    @PostConstruct
    public void init() {
        log.info("JWT 初始化: secret.length={}, expiration={}ms", secret.length(), accessExpiration);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        log.info("JWT 生成成功: userId={}, token 前20字符={}", userId, token.substring(0, Math.min(20, token.length())));
        return token;
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            log.warn("JWT Claims 为 null，token 前20字符={}", token.substring(0, Math.min(20, token.length())));
            return null;
        }
        Object userIdObj = claims.get("userId");
        log.info("JWT 解析成功: userId={}", userIdObj);
        return Long.valueOf(userIdObj.toString());
    }

    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }
}
