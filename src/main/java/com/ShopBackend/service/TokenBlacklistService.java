package com.ShopBackend.service;

import com.ShopBackend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    private static final String BLACKLIST_KEY_PREFIX = "blacklist:";

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate, JwtUtil jwtUtil) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    // ===== 1. BLACKLIST TOKEN =====
    public void blacklistToken(String token) {
        // Extract expiration time dari token
        Claims claims = jwtUtil.extractClaims(token);
        long expirationTime = claims.getExpiration().getTime();
        long now = System.currentTimeMillis();
        long ttlMillis = expirationTime - now;

        if (ttlMillis <= 0) {
            // Token sudah expired, tidak perlu di-blacklist
            return;
        }

        // Simpan token ke Redis dengan TTL sesuai sisa waktu token
        String key = BLACKLIST_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, "logged_out", ttlMillis, TimeUnit.MILLISECONDS);
    }

    // ===== 2. CEK APAKAH TOKEN DI BLACKLIST =====
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_KEY_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
