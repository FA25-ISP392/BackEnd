package com.isp392.controller;

import com.isp392.dto.request.DailyPlanCreationRequest;
import com.isp392.dto.request.DailyPlanUpdateRequest;
import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.response.DailyPlanResponse;
import com.isp392.service.DailyPlanService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/daily-plans")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DailyPlanController {

    DailyPlanService dailyPlanService;

    @PostMapping
  //  @PreAuthorize("hasAnyRole('CHEF', 'MANAGER')")
    public ApiResponse<DailyPlanResponse> createDailyPlan(@RequestBody @Valid DailyPlanCreationRequest request, Authentication authentication) {
        return ApiResponse.<DailyPlanResponse>builder()
                .result(dailyPlanService.createDailyPlan(request, authentication))
                .build();
    }

    @PostMapping("/batch")
   // @PreAuthorize("hasAnyRole('CHEF', 'MANAGER')")
    public ApiResponse<List<DailyPlanResponse>> createDailyPlansBatch(@RequestBody @Valid List<DailyPlanCreationRequest> requests, Authentication authentication) {
        return ApiResponse.<List<DailyPlanResponse>>builder()
                .result(dailyPlanService.createDailyPlansBatch(requests, authentication))
                .build();
    }

    @GetMapping
  //  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CHEF')")
    public ApiResponse<List<DailyPlanResponse>> getAllDailyPlans() {
        return ApiResponse.<List<DailyPlanResponse>>builder()
                .result(dailyPlanService.getAllDailyPlans())
                .build();
    }

    @GetMapping("/{planId}")
   // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CHEF')")
    public ApiResponse<DailyPlanResponse> getDailyPlanById(@PathVariable int planId) {
        return ApiResponse.<DailyPlanResponse>builder()
                .result(dailyPlanService.getDailyPlanById(planId))
                .build();
    }

    @PutMapping("/{planId}")
   // @PreAuthorize("hasAnyRole('CHEF', 'MANAGER', 'ADMIN')")
    public ApiResponse<DailyPlanResponse> updateDailyPlan(@PathVariable int planId, @RequestBody @Valid DailyPlanUpdateRequest request, Authentication authentication) {
        return ApiResponse.<DailyPlanResponse>builder()
                .result(dailyPlanService.updateDailyPlan(planId, request, authentication))
                .build();
    }

    @DeleteMapping("/{planId}")
    //@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CHEF')")
    public ApiResponse<String> deleteDailyPlan(@PathVariable int planId) {
        dailyPlanService.deleteDailyPlan(planId);
        return ApiResponse.<String>builder()
                .result("Daily plan deleted successfully.")
                .build();
    }
}