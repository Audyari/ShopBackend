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

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
        // 1. Cari user di database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan!"));

        // 2. Verifikasi password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password salah!");
        }

        // 3. Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), "USER");

        // 4. Return response
        return new AuthResponse(token, user.getEmail(), user.getName());
    }
}
