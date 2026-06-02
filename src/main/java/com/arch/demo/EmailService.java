package com.arch.demo;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendWelcome(String email) {
        System.out.println("Sending welcome email to: " + email);
    }

    public void sendPasswordReset(String email, String token) {
        System.out.println("Sending reset token to: " + email);
    }
}
