package com.isp392.controller;

import com.isp392.dto.request.ApiResponse;
import com.isp392.dto.request.StaffCreationRequest;
import com.isp392.dto.request.StaffUpdateRequest;
import com.isp392.dto.response.StaffResponse;
import com.isp392.entity.Staff;
import com.isp392.service.StaffService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaffController {

    StaffService staffService;

    @PostMapping
    ApiResponse<Staff> createStaff(@RequestBody @Valid StaffCreationRequest request) {
        ApiResponse<Staff> apiResponse = new ApiResponse<>();
        apiResponse.setResult(staffService.createStaff(request));
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<Staff>> getStaff() {
        ApiResponse<List<Staff>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(staffService.getStaffs());
        return apiResponse;
    }

    @GetMapping("/{staffId}")
    ApiResponse<StaffResponse> getStaff(@PathVariable long staffId, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("sub");
        StaffResponse staff = staffService.getStaff(staffId, username);
        ApiResponse<StaffResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(staff);
        return apiResponse;
    }

    @PutMapping("/{staffId}")
    ApiResponse<StaffResponse> updateStaff(@PathVariable long staffId, @RequestBody @Valid StaffUpdateRequest request) {
        ApiResponse<StaffResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(staffService.updateStaff(staffId, request));
        return apiResponse;
    }

    @DeleteMapping("/{staffId}")
    ApiResponse<String> deleteStaff(@PathVariable long staffId) {
        staffService.deleteStaff(staffId);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Delete user successfully");
        return apiResponse;
    }
}
