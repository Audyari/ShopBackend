package com.ShopBackend.service;

import com.ShopBackend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    private static final String ACCESS_BLACKLIST_PREFIX = "blacklist:access:";
    private static final String REFRESH_BLACKLIST_PREFIX = "blacklist:refresh:";

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate, JwtUtil jwtUtil) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

     // ===== BLACKLIST ACCESS TOKEN =====
    public void blacklistAccessToken(String token) {
        Claims claims = jwtUtil.extractClaims(token);
        long expirationTime = claims.getExpiration().getTime();
        long now = System.currentTimeMillis();
        long ttlMillis = expirationTime - now;

        if (ttlMillis <= 0) {
            return;
        }

        String key = ACCESS_BLACKLIST_PREFIX + token;
        // ✅ PAKE Duration (tidak deprecated)
        redisTemplate.opsForValue().set(key, "logged_out", Duration.ofMillis(ttlMillis));
    }

    // ===== BLACKLIST REFRESH TOKEN =====
    public void blacklistRefreshToken(String token) {
        Claims claims = jwtUtil.extractClaims(token);
        long expirationTime = claims.getExpiration().getTime();
        long now = System.currentTimeMillis();
        long ttlMillis = expirationTime - now;

        if (ttlMillis <= 0) {
            return;
        }

        String key = REFRESH_BLACKLIST_PREFIX + token;
        // ✅ PAKE Duration (tidak deprecated)
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofMillis(ttlMillis));
    }

    // ===== IS ACCESS TOKEN BLACKLISTED? =====
    public boolean isAccessTokenBlacklisted(String token) {
        String key = ACCESS_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // ===== IS REFRESH TOKEN BLACKLISTED? =====
    public boolean isRefreshTokenBlacklisted(String token) {
        String key = REFRESH_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // ===== SAVE REFRESH TOKEN (untuk validasi) =====
    public void saveRefreshToken(String token) {
        Claims claims = jwtUtil.extractClaims(token);
        long expirationTime = claims.getExpiration().getTime();
        long now = System.currentTimeMillis();
        long ttlMillis = expirationTime - now;

        if (ttlMillis <= 0) {
            return;
        }

        String key = "refresh:" + jwtUtil.extractEmail(token);
        // ✅ PAKE Duration (tidak deprecated)
        redisTemplate.opsForValue().set(key, token, Duration.ofMillis(ttlMillis));
    }

    // ===== VALIDATE REFRESH TOKEN =====
    public boolean validateRefreshToken(String email, String refreshToken) {
        String key = "refresh:" + email;
        String storedToken = redisTemplate.opsForValue().get(key);
        return refreshToken.equals(storedToken);
    }

    // ===== REVOKE REFRESH TOKEN (logout) =====
    public void revokeRefreshToken(String email) {
        String key = "refresh:" + email;
        redisTemplate.delete(key);
    }
}

