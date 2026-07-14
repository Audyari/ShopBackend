package com.ShopBackend.controller;

import com.ShopBackend.dto.RegisterRequest;
import com.ShopBackend.model.User;
import com.ShopBackend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    // Constructor HARUS ada dan public
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.ok("User " + user.getEmail() + " berhasil daftar!");
    }

    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody RegisterRequest request) {
        boolean result = authService.verifyPassword(request.getEmail(), request.getPassword());
        if (result) {
            return ResponseEntity.ok("Password benar!");
        } else {
            return ResponseEntity.badRequest().body("Password salah!");
        }
    }
}
