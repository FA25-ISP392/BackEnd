package com.isp392.controller;

import com.isp392.dto.request.AIChatRequest;
import com.isp392.dto.response.ApiResponse;
import com.isp392.service.AISuggestionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai-chat")
@RequiredArgsConstructor
@CrossOrigin("*") // Thêm CrossOrigin để frontend gọi được
@SecurityRequirement(name = "bearerAuth") // Giống các controller khác của bạn
public class AIChatController {

    private final AISuggestionService aiSuggestionService;

    @PostMapping("/suggest")
    @PreAuthorize("hasRole('CUSTOMER')") // Chỉ customer mới được dùng
    public ApiResponse<String> getSuggestion(
            @RequestBody AIChatRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        // Lấy username của người dùng đã đăng nhập từ JWT
        String username = jwt.getClaimAsString("sub");

        String aiResponse = aiSuggestionService.getChatSuggestion(request.getQuery(), username);

        return ApiResponse.<String>builder()
                .result(aiResponse)
                .build();
    }
}
