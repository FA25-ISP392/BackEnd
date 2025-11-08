package com.isp392.controller;

import com.isp392.dto.request.AIChatRequest;
import com.isp392.dto.response.AIChatResponse;
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
    @PreAuthorize("permitAll()")
    public ApiResponse<AIChatResponse> getSuggestion(
            @RequestBody AIChatRequest request,
            // Sửa 1: Thêm dấu ) để đóng danh sách tham số
            @AuthenticationPrincipal Jwt jwt) {

        // Sửa 2: Kiểm tra jwt != null trước khi sử dụng
        String username = null;
        if (jwt != null) {
            username = jwt.getClaimAsString("sub");
        }

        AIChatResponse aiResponse = aiSuggestionService.getChatSuggestion(request, username);

        return ApiResponse.<AIChatResponse>builder()
                .result(aiResponse)
                .build();
    }
}
