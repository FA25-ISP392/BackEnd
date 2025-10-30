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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

    @Value("${app.frontend.google-callback-url}")
    @NonFinal
    String frontendGoogleCallbackUrl;

    @Transactional
    public String handleGoogleLoginSuccess(OAuth2AuthenticationToken authentication) {
        // 1. Lấy thông tin người dùng từ Google
        String email = authentication.getPrincipal().getAttribute("email");
        String name = authentication.getPrincipal().getAttribute("name");

        // 2. Tìm hoặc tạo Account và Customer (Logic "Find or Create")
        Account account = accountRepository.findByEmail(email)
                .orElseGet(() -> createGoogleUserAccountAndCustomer(email, name)); // Gọi hàm helper

        // 3. Tạo JWT token cho người dùng (dù mới hay cũ)
        String token = generateToken(account.getUsername(), account.getRole());

        // 4. Tạo URL redirect về frontend với token trong fragment (#)
        // Ví dụ: https://moncuaban.vercel.app/auth/callback#token=xxxx
        return frontendGoogleCallbackUrl + "#token=" + token;
    }

    private Account createGoogleUserAccountAndCustomer(String email, String name) {
        // Tạo Account mới (CHƯA LƯU)
        Account newAcc = new Account();
        newAcc.setEmail(email);
        newAcc.setUsername(email); // Sử dụng email làm username
        newAcc.setFullName(name);
        newAcc.setRole(Role.CUSTOMER);
        newAcc.setPassword(UUID.randomUUID().toString()); // Mật khẩu ngẫu nhiên
        newAcc.setVerified(true); // User Google coi như đã xác thực email

        // Tạo Customer và liên kết với Account
        Customer newCustomer = Customer.builder()
                .account(newAcc)
                .build();

        // CHỈ LƯU CUSTOMER (Cascade sẽ tự lưu Account)
        customerRepository.save(newCustomer);

        // Trả về Account đã được quản lý bởi JPA
        return newCustomer.getAccount();
    }

    @Transactional // Đảm bảo toàn bộ là một giao dịch
    public AuthenticationResponse authenticateGoogleUser(String email, String name) {
        var account = accountRepository.findByEmail(email)
                .orElseGet(() -> {
                    // --- BẮT ĐẦU SỬA ---
                    // 1. Tạo Account mới (CHƯA LƯU)
                    Account newAcc = new Account();
                    newAcc.setEmail(email);
                    newAcc.setUsername(email); // Hoặc logic username khác
                    newAcc.setFullName(name);
                    newAcc.setRole(Role.CUSTOMER);
                    newAcc.setPassword(UUID.randomUUID().toString()); // Mật khẩu ngẫu nhiên cho user Google
                    newAcc.setVerified(true); // User Google coi như đã xác thực email

                    // 2. Tạo Customer và liên kết với Account vừa tạo
                    Customer newCustomer = Customer.builder()
                            .account(newAcc) // Liên kết Account vào Customer
                            .build();

                    // 3. CHỈ LƯU CUSTOMER (Cascade sẽ tự lưu Account)
                    customerRepository.save(newCustomer); // Lưu Customer, JPA sẽ tự lưu newAcc do cascade

                    // 4. Trả về Account đã được lưu (thông qua newCustomer)
                    // Lưu ý: Sau khi save newCustomer, newAcc bên trong nó cũng đã được quản lý
                    // và có thể đã được cập nhật ID bởi JPA.
                    return newCustomer.getAccount();
                    // --- KẾT THÚC SỬA ---
                });

        // Tạo token (phần này giữ nguyên)
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

        // 2️⃣ Kiểm tra verified
        if (!account.isVerified()) {  // hoặc account.getVerified() nếu dùng Boolean
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 3️⃣ Kiểm tra mật khẩu
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

        if (account.isVerified()) {
            emailTokenRepository.delete(verificationToken);
            throw new AppException(ErrorCode.INVALID_ARGUMENT);
        }

        account.setVerified(true);
        accountRepository.save(account);

        // Xóa token sau khi dùng
        emailTokenRepository.delete(verificationToken);
    }
}
