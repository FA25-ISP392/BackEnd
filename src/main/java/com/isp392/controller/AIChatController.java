package com.isp392.controller;

import com.isp392.dto.request.AIChatRequest;
import com.isp392.dto.response.AIChatResponse; // 👈 SỬA ĐỔI
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
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class AIChatController {

    private final AISuggestionService aiSuggestionService;

    @PostMapping("/suggest")
    @PreAuthorize("hasRole('CUSTOMER')")
    // 👇 SỬA ĐỔI kiểu trả về là AIChatResponse
    public ApiResponse<AIChatResponse> getSuggestion(
            @RequestBody AIChatRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String username = jwt.getClaimAsString("sub");

        // 👇 SỬA ĐỔI: Gọi hàm service mới, truyền cả request
        AIChatResponse aiResponse = aiSuggestionService.getChatSuggestion(request, username);

        return ApiResponse.<AIChatResponse>builder()
                .result(aiResponse)
                .build();
    }
}