package com.ShopBackend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ===== KIRIM EMAIL OTP =====
    public void sendOTPEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("🔐 Kode Verifikasi Akun Anda");
        message.setText(
            "Halo,\n\n" +
            "Terima kasih telah mendaftar di ShopBackend!\n\n" +
            "Kode verifikasi Anda adalah: " + otp + "\n\n" +
            "Kode ini berlaku selama 5 menit.\n\n" +
            "Jika Anda tidak merasa mendaftar, abaikan email ini.\n\n" +
            "Terima kasih,\n" +
            "Tim ShopBackend"
        );
        
        mailSender.send(message);
    }
}
