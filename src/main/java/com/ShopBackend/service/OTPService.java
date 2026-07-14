package com.ShopBackend.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OTPService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String OTP_PREFIX = "otp:";
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRATION_MINUTES = 5;

    public OTPService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ===== 1. GENERATE OTP =====
    public String generateOTP(String email) {
        String otp = generateRandomOTP();
        String key = OTP_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(OTP_EXPIRATION_MINUTES));
        return otp;
    }

    // ===== 2. VALIDATE OTP =====
    public boolean validateOTP(String email, String otp) {
        String key = OTP_PREFIX + email;
        String storedOTP = redisTemplate.opsForValue().get(key);
        
        if (storedOTP == null) {
            return false; // OTP expired atau tidak ditemukan
        }
        
        if (storedOTP.equals(otp)) {
            redisTemplate.delete(key); // Hapus OTP setelah berhasil
            return true;
        }
        
        return false;
    }

    // ===== 3. GENERATE RANDOM OTP =====
    private String generateRandomOTP() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    // ===== 4. RESEND OTP =====
    public void resendOTP(String email) {
        String key = OTP_PREFIX + email;
        redisTemplate.delete(key); // Hapus OTP lama
        generateOTP(email); // Generate OTP baru
    }
}
