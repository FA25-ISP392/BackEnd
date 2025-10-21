package com.isp392.service;

import com.isp392.dto.request.AuthenticationRequest;
import com.isp392.dto.request.IntrospectRequest;
import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.response.AuthenticationResponse;
import com.isp392.dto.response.IntrospectResponse;
import com.isp392.entity.Account;
import com.isp392.entity.Customer;
import com.isp392.entity.EmailVerificationToken;
import com.isp392.entity.PasswordResetToken;
import com.isp392.enums.Role;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.repository.AccountRepository;
import com.isp392.repository.CustomerRepository;
import com.isp392.repository.EmailVerificationTokenRepository;
import com.isp392.repository.PasswordResetTokenRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    PasswordEncoder passwordEncoder;
    AccountRepository accountRepository;
    CustomerRepository customerRepository;
    PasswordResetTokenRepository tokenRepository;
    PasswordResetTokenService passwordResetTokenService;
    EmailVerificationTokenRepository emailTokenRepository;
    EmailService emailService;
    @Value("${app.frontend.reset-password-url}")
    @NonFinal
    String frontendResetPasswordUrl;

    @NonFinal
    @Value("${jwt.signerKey}")
    String SIGNER_KEY;

    @Transactional
    public AuthenticationResponse authenticateGoogleUser(String email, String name) {
        // Kiểm tra xem user đã tồn tại chưa
        var account = accountRepository.findByEmail(email)
                .orElseGet(() -> {
                    Account newAcc = new Account();
                    newAcc.setEmail(email);
                    newAcc.setUsername(email); // có thể dùng email làm username
                    newAcc.setFullName(name);
                    newAcc.setPassword(UUID.randomUUID().toString());
                    newAcc.setRole(Role.CUSTOMER);
                    Customer customer = Customer.builder()
                            .account(newAcc)
                            .build();
                    accountRepository.save(newAcc);
                    customerRepository.save(customer);
                    return newAcc;
                });

        String token = generateToken(account.getUsername(), account.getRole());

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expityTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        return IntrospectResponse.builder()
                .valid(verified && expityTime.after(new Date()))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // 1️⃣ Tìm account theo username
        var account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 2️⃣ Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // 3️⃣ Sinh token có claim role
        var token = generateToken(account.getUsername(), account.getRole());

        // 4️⃣ Trả về response
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    private AuthenticationResponse buildResponse(String username, Role role) {
        String token = generateToken(username, role);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    private String generateToken(String username, Role role) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(username)
                .issuer("isp392")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .claim("role", role.name())
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(claims.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Error creating JWT", e);
        }
    }

    public String sendResetPasswordLink(String email) {
        Optional<Account> userOpt = accountRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new AppException(ErrorCode.EMAIL_INVALID);
        }

        // Xóa token cũ và tạo mới
        passwordResetTokenService.deleteOldTokenByEmail(email);
        String token = passwordResetTokenService.createTokenForEmail(email);

        // Tạo link reset (Frontend URL lấy từ config)
        String resetLink = frontendResetPasswordUrl + "?token=" + token;

        // Gửi mail
        emailService.sendResetPasswordEmail(email, resetLink);

        return "Password reset link sent to your email";
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        Account user = accountRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(user);

        // Xóa token sau khi dùng xong
        tokenRepository.delete(resetToken);
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            emailTokenRepository.delete(verificationToken);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        // Lấy email từ token và tìm account (giống logic reset pass)
        String email = verificationToken.getEmail();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (account.getIsVerified()==true) {
            emailTokenRepository.delete(verificationToken);
            //có thể thêm ErrorCode.EMAIL_ALREADY_VERIFIED nếu cần
            throw new AppException(ErrorCode.INVALID_ARGUMENT);
        }

        account.setIsVerified(true);
        accountRepository.save(account);

        // Xóa token sau khi dùng
        emailTokenRepository.delete(verificationToken);
    }
}
