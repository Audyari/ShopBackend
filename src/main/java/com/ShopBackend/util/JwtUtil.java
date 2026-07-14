package com.ShopBackend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "mySuperSecretKeyForJWTGeneration1234567890123456";
    private static final long ACCESS_TOKEN_EXPIRATION = 900000; // 15 menit
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 hari
    
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // ===== GENERATE ACCESS TOKEN =====
    public String generateAccessToken(Long userId, String email, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("email", email)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSecretKey())
                .compact();
    }

    // ===== GENERATE REFRESH TOKEN =====
    public String generateRefreshToken(Long userId, String email) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("email", email)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSecretKey())
                .compact();
    }

    // ===== EXTRACT CLAIMS =====
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ===== EXTRACT EMAIL =====
    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    // ===== EXTRACT USER ID =====
    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    // ===== EXTRACT TOKEN TYPE =====
    public String extractTokenType(String token) {
        return extractClaims(token).get("type", String.class);
    }

    // ===== VALIDATE TOKEN =====
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
