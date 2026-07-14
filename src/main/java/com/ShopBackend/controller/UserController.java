package com.ShopBackend.controller;

import com.ShopBackend.model.User;
import com.ShopBackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ===== GET PROFILE (PAKAI TOKEN!) =====
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        // Ambil email dari SecurityContext (yang udah di-set oleh JwtAuthenticationFilter)
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));
        
        return ResponseEntity.ok(user);
    }
}
