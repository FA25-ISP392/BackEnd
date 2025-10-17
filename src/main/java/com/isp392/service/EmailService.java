package com.isp392.service;

import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {

    private final JavaMailSender mailSender;

    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    public void sendResetPasswordEmail(String email, String resetLink) {
        String subject = "Reset your password";
        String body = "Hello,\n\nWe received a request to reset your password.\n\n"
                + "Click the link below to reset it (valid for 15 minutes):\n"
                + resetLink
                + "\n\nIf you didn't request this, please ignore this email.";

        send(email, subject, body);
        log.info("Reset password email sent to {}", email);
    }
}
