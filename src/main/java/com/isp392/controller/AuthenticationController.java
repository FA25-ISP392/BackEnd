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
        authenticationService.verifyEmail(token); // Logic sẽ được thêm ở Bước 7
        return ApiResponse.<String>builder()
                .result("Email verified successfully")
                .build();
    }

    @GetMapping("/google/popup")
    public void googleLoginPopup(
            HttpServletRequest request,
            HttpServletResponse response,
            OAuth2AuthenticationToken authentication
    ) throws IOException {
        String email = authentication.getPrincipal().getAttribute("email");
        String name = authentication.getPrincipal().getAttribute("name");

        AuthenticationResponse auth = authenticationService.authenticateGoogleUser(email, name);
        String token = auth.getToken();

        // frontend origin
        String feOrigin = Optional.ofNullable(System.getenv("FRONTEND_ORIGIN"))
                .orElse("http://localhost:5173"); // fallback local

        // escape token
        String safeToken = token.replace("\\", "\\\\").replace("\"", "\\\"");

        String html = """
                <!doctype html>
                <html><head><meta charset="utf-8"></head><body>
                <script>
                (function(){
                  var data = { type: 'OAUTH_SUCCESS', token: "%s" };
                  try {
                    if (window.opener) {
                      window.opener.postMessage(data, "%s");
                    }
                  } catch(e){}
                  window.close();
                })();
                </script>
                Đang đăng nhập... (Bạn có thể đóng cửa sổ này)
                </body></html>
                """.formatted(safeToken, feOrigin);

        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(html);
    }


    @GetMapping("/google/success")
    public ApiResponse<AuthenticationResponse> googleLoginSuccess(OAuth2AuthenticationToken authentication) {
        String email = authentication.getPrincipal().getAttribute("email");
        String name = authentication.getPrincipal().getAttribute("name");

        AuthenticationResponse response = authenticationService.authenticateGoogleUser(email, name);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(response)
                .build();
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
