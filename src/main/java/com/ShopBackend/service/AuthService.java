package com.ShopBackend.service;

import com.ShopBackend.dto.LoginRequest;
import com.ShopBackend.dto.RegisterRequest;
import com.ShopBackend.dto.AuthResponse;
import com.ShopBackend.model.User;
import com.ShopBackend.repository.UserRepository;
import com.ShopBackend.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    // ===== LOGIN =====
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password salah!");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), "USER");

        return new AuthResponse(token, user.getEmail(), user.getName());
    }

    // ===== ⭐ LOGOUT (TAMBAHKAN INI!) =====
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token tidak ditemukan!");
        }

        // Cek apakah token sudah di-blacklist
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            throw new RuntimeException("Token sudah logout sebelumnya!");
        }

        // Blacklist token
        tokenBlacklistService.blacklistToken(token);
    }
}
