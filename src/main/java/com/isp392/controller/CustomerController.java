package com.isp392.controller;

import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.request.CustomerCreationRequest;
import com.isp392.dto.request.CustomerUpdateRequest;
import com.isp392.dto.response.CustomerResponse;
import com.isp392.service.CustomerService;
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
@RequestMapping("/customer")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerController {

    CustomerService customerService;

    @PostMapping
    public ApiResponse<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreationRequest request) {
        CustomerResponse createdCustomer = customerService.createCustomer(request);
        return ApiResponse.<CustomerResponse>builder()
                .result(createdCustomer)
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<List<CustomerResponse>> getCustomers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size) {
        List<CustomerResponse> customers = customerService.getCustomer(page, size);
        return ApiResponse.<List<CustomerResponse>>builder()
                .result(customers)
                .build();
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<CustomerResponse> getCustomer(@PathVariable Integer customerId, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("sub");
        CustomerResponse customer = customerService.getCustomer(customerId, username);
        return ApiResponse.<CustomerResponse>builder()
                .result(customer)
                .build();
    }

    @GetMapping("/by-username/{username}")
    @PreAuthorize("isAuthenticated() and (#username == principal.subject or hasRole('ADMIN'))")
    public ApiResponse<CustomerResponse> getCustomerByUsername(@PathVariable String username) {
        CustomerResponse customer = customerService.getCustomerByUsername(username);
        return ApiResponse.<CustomerResponse>builder()
                .result(customer)
                .build();
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<CustomerResponse> updateCustomer(@PathVariable Integer customerId, @Valid @RequestBody CustomerUpdateRequest request) {
        CustomerResponse updatedCustomer = customerService.updateCustomer(customerId, request);
        return ApiResponse.<CustomerResponse>builder()
                .result(updatedCustomer)
                .build();
    }


    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteCustomer(@PathVariable Integer customerId) {
        customerService.deleteCustomer(customerId);
        return ApiResponse.<Void>builder()
                .message("Customer deleted successfully")
                .build();
    }
}

