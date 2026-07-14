package com.ShopBackend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    // Secret key harus minimal 256 bit (32 karakter)
    private static final String SECRET_KEY = "mySuperSecretKeyForJWTGeneration1234567890123456";
    private static final long EXPIRATION_TIME = 86400000; // 24 jam dalam milidetik

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // 1. Generate JWT Token
    public String generateToken(Long userId, String email, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSecretKey())
                .compact();
    }

    // 2. Extract semua claims dari token
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 3. Extract email dari token
    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    // 4. Extract userId dari token
    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    // 5. Validasi token
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
