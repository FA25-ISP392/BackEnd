package com.isp392.controller;

import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.request.StaffCreationRequest;
import com.isp392.dto.request.StaffUpdateRequest;
import com.isp392.dto.response.StaffResponse;
import com.isp392.service.StaffService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaffController {

    StaffService staffService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StaffResponse> createStaff(@Valid @RequestBody StaffCreationRequest request) {
        StaffResponse createdStaff = staffService.createStaff(request);
        return ApiResponse.<StaffResponse>builder()
                .result(createdStaff)
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<List<StaffResponse>> getStaff(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size) {
        List<StaffResponse> staffList = staffService.getStaff(page, size);
        return ApiResponse.<List<StaffResponse>>builder()
                .result(staffList)
                .build();
    }

    @GetMapping("/{staffId}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ApiResponse<StaffResponse> getStaff(@PathVariable Integer staffId, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("sub");
        StaffResponse staff = staffService.getStaff(staffId, username);
        return ApiResponse.<StaffResponse>builder()
                .result(staff)
                .build();
    }

    @PutMapping("/{staffId}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ApiResponse<StaffResponse> updateStaff(@PathVariable Integer staffId, @Valid @RequestBody StaffUpdateRequest request) {
        StaffResponse updatedStaff = staffService.updateStaff(staffId, request);
        return ApiResponse.<StaffResponse>builder()
                .result(updatedStaff)
                .build();
    }

    @DeleteMapping("/{staffId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteStaff(@PathVariable Integer staffId) {
        staffService.deleteStaff(staffId);
        return ApiResponse.<Void>builder()
                .message("Staff deleted successfully")
                .build();
    }

}
