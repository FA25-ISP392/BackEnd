package com.isp392.controller;

import com.isp392.dto.request.ResetPasswordRequest;
import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.request.AuthenticationRequest;
import com.isp392.dto.request.IntrospectRequest;
import com.isp392.dto.response.AuthenticationResponse;
import com.isp392.dto.response.IntrospectResponse;
import com.isp392.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.text.ParseException;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @GetMapping("/verify-email")
    public ApiResponse<String> verifyEmail(@RequestParam String token) {
        authenticationService.verifyEmail(token);
        return ApiResponse.<String>builder()
                .result("Email verified successfully")
                .build();
    }


    @GetMapping("/google/success")
    public RedirectView googleLoginSuccess(OAuth2AuthenticationToken authentication) {
        String redirectUrl = authenticationService.handleGoogleLoginSuccess(authentication);
        return new RedirectView(redirectUrl);
    }

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }


    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestParam String email) {
        String result = authenticationService.sendResetPasswordLink(email);
        return ApiResponse.<String>builder()
                .result(result)
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request.getToken(), request.getNewPassword());
        return ApiResponse.<String>builder()
                .result("Password has been reset successfully")
                .build();
    }

}
