package com.isp392.controller;

import com.isp392.dto.request.SuggestionCreationRequest;
import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.response.MenuSuggestion;
import com.isp392.service.SuggestionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/suggestions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SuggestionController {

    SuggestionService suggestionService;

    @PostMapping("/menu")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<List<MenuSuggestion>> getMenuSuggestions(@RequestBody @Valid SuggestionCreationRequest request, @AuthenticationPrincipal Jwt jwt) {

        String username = jwt.getClaimAsString("sub");
        List<MenuSuggestion> suggestions = suggestionService.getSuggestionsForCustomer(username, request);

        return ApiResponse.<List<MenuSuggestion>>builder()
                .result(suggestions)
                .build();
    }
}