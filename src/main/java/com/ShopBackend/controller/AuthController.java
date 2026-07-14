package com.ShopBackend.controller;

import com.ShopBackend.dto.LoginRequest;
import com.ShopBackend.dto.RegisterRequest;
import com.ShopBackend.dto.AuthResponse;
import com.ShopBackend.model.User;
import com.ShopBackend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        User user = authService.register(request);
        return ResponseEntity.ok("User " + user.getEmail() + " berhasil daftar!");
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

     // ===== ⭐ LOGOUT (TAMBAHKAN INI!) =====
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Invalid Authorization header!");
            }

            String token = authorizationHeader.substring(7);
            authService.logout(token);

            return ResponseEntity.ok("Logout berhasil!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
