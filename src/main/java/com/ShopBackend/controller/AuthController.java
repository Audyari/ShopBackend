package com.ShopBackend.controller;

import com.ShopBackend.dto.LoginRequest;
import com.ShopBackend.dto.RegisterRequest;
import com.ShopBackend.dto.AuthResponse;
import com.ShopBackend.model.User;
import com.ShopBackend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ===== REGISTER =====
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            return ResponseEntity.ok("User " + user.getEmail() + " berhasil daftar! Cek email Anda untuk kode OTP.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===== ⭐ VERIFY OTP =====
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            
            if (email == null || otp == null) {
                return ResponseEntity.badRequest().body("Email dan OTP wajib diisi!");
            }
            
            String result = authService.verifyOTP(email, otp);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===== ⭐ RESEND OTP =====
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOTP(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null) {
                return ResponseEntity.badRequest().body("Email wajib diisi!");
            }
            
            String result = authService.resendOTP(email);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===== LOGIN =====
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===== LOGOUT =====
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String accessTokenHeader,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshTokenHeader) {
        try {
            if (accessTokenHeader == null || !accessTokenHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Invalid Authorization header!");
            }

            String accessToken = accessTokenHeader.substring(7);
            authService.logout(accessToken, refreshTokenHeader);

            return ResponseEntity.ok("Logout berhasil!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===== REFRESH TOKEN =====
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Invalid Authorization header!");
            }

            String refreshToken = authorizationHeader.substring(7);
            Map<String, String> response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
