package com.ShopBackend.service;

import com.ShopBackend.dto.LoginRequest;
import com.ShopBackend.dto.RegisterRequest;
import com.ShopBackend.dto.AuthResponse;
import com.ShopBackend.model.User;
import com.ShopBackend.repository.UserRepository;
import com.ShopBackend.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    
    public AuthService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder, 
                       JwtUtil jwtUtil,
                       TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }
    
    // ===== REGISTER =====
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email sudah terdaftar!");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());

        return userRepository.save(user);
    }

    // ===== LOGIN (Generate 2 Token) =====
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password salah!");
        }

        // Generate 2 token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), "USER");
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        // Simpan refresh token ke Redis
        tokenBlacklistService.saveRefreshToken(refreshToken);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getName());
    }

    // ===== REFRESH TOKEN =====
    public Map<String, String> refreshToken(String refreshToken) {
        // 1. Cek apakah refresh token valid
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new RuntimeException("Refresh token tidak valid!");
        }

        // 2. Cek apakah refresh token di-blacklist
        if (tokenBlacklistService.isRefreshTokenBlacklisted(refreshToken)) {
            throw new RuntimeException("Refresh token sudah dicabut!");
        }

        // 3. Extract data dari refresh token
        String email = jwtUtil.extractEmail(refreshToken);
        Long userId = jwtUtil.extractUserId(refreshToken);

        // 4. Validasi refresh token di Redis
        if (!tokenBlacklistService.validateRefreshToken(email, refreshToken)) {
            throw new RuntimeException("Refresh token tidak valid!");
        }

        // 5. Generate access token baru
        String newAccessToken = jwtUtil.generateAccessToken(userId, email, "USER");

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        return response;
    }

    // ===== LOGOUT (Revoke Refresh Token) =====
    public void logout(String accessToken, String refreshToken) {
        // Blacklist access token
        tokenBlacklistService.blacklistAccessToken(accessToken);

        // Revoke refresh token
        if (refreshToken != null && !refreshToken.isEmpty()) {
            String email = jwtUtil.extractEmail(refreshToken);
            tokenBlacklistService.revokeRefreshToken(email);
            tokenBlacklistService.blacklistRefreshToken(refreshToken);
        }
    }
}
