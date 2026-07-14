package com.ShopBackend.service;

import com.ShopBackend.dto.LoginRequest;
import com.ShopBackend.dto.RegisterRequest;
import com.ShopBackend.dto.AuthResponse;
import com.ShopBackend.model.User;
import com.ShopBackend.repository.UserRepository;
import com.ShopBackend.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final OTPService otpService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder, 
                       JwtUtil jwtUtil,
                       TokenBlacklistService tokenBlacklistService,
                       OTPService otpService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    // ===== REGISTER (dengan OTP) =====
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email sudah terdaftar!");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setIsVerified(false); // ⭐ BELUM VERIFIED!

        // Simpan user ke database
        User savedUser = userRepository.save(user);

        // Generate OTP dan kirim email
        String otp = otpService.generateOTP(request.getEmail());
        emailService.sendOTPEmail(request.getEmail(), otp);

        return savedUser;
    }

    // ===== VERIFY OTP =====
    @Transactional
    public String verifyOTP(String email, String otp) {
        // Cek apakah user ada
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan!"));

        // Cek apakah user sudah verified
        if (user.getIsVerified()) {
            throw new RuntimeException("Email sudah diverifikasi sebelumnya!");
        }

        // Validasi OTP
        if (!otpService.validateOTP(email, otp)) {
            throw new RuntimeException("OTP tidak valid atau sudah expired!");
        }

        // Update user menjadi verified
        user.setIsVerified(true);
        userRepository.save(user);

        return "Email berhasil diverifikasi!";
    }

    // ===== RESEND OTP =====
    public String resendOTP(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan!"));

        if (user.getIsVerified()) {
            throw new RuntimeException("Email sudah diverifikasi!");
        }

        // Resend OTP
        otpService.resendOTP(email);
        String newOtp = otpService.generateOTP(email);
        emailService.sendOTPEmail(email, newOtp);

        return "OTP baru telah dikirim ke email Anda!";
    }

    // ===== LOGIN (Cek Verified) =====
    public AuthResponse login(LoginRequest request) {
        // Cari user yang sudah verified
        User user = userRepository.findByEmailAndIsVerifiedTrue(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan atau belum diverifikasi!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password salah!");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), "USER");
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        tokenBlacklistService.saveRefreshToken(refreshToken);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getName());
    }

    // ===== LOGOUT =====
    public void logout(String accessToken, String refreshToken) {
        tokenBlacklistService.blacklistAccessToken(accessToken);
        if (refreshToken != null && !refreshToken.isEmpty()) {
            String email = jwtUtil.extractEmail(refreshToken);
            tokenBlacklistService.revokeRefreshToken(email);
            tokenBlacklistService.blacklistRefreshToken(refreshToken);
        }
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
}
