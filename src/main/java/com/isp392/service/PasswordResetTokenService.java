package com.isp392.service;

import com.isp392.entity.PasswordResetToken;
import com.isp392.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PasswordResetTokenService {

    PasswordResetTokenRepository tokenRepository;

    @Transactional
    public void deleteOldTokenByEmail(String email) {
        tokenRepository.deleteByEmail(email.trim());
    }

    public String createTokenForEmail(String email) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(email.trim())
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        tokenRepository.save(resetToken);
        return token;
    }

    public boolean isTokenValid(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElse(null);
        if (resetToken == null) return false;
        return resetToken.getExpiryDate().isAfter(LocalDateTime.now());
    }
}
