package com.isp392.controller;

import com.isp392.dto.request.ApiResponse;
import com.isp392.dto.request.CustomerCreationRequest;
import com.isp392.dto.request.CustomerUpdateRequest;
import com.isp392.dto.response.CustomerResponse;
import com.isp392.service.CustomerService;
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
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CustomerResponse>> getCustomers() {
        List<CustomerResponse> customers = customerService.getCustomer();
        return ApiResponse.<List<CustomerResponse>>builder()
                .result(customers)
                .build();
    }

    @GetMapping("/{customerId}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CustomerResponse> getCustomer(@PathVariable Integer customerId, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("sub");
        CustomerResponse customer = customerService.getCustomer(customerId, username);
        return ApiResponse.<CustomerResponse>builder()
                .result(customer)
                .build();
    }

    @PutMapping("/{customerId}")
//    @PreAuthorize("hasRole('CUSTOMER')")
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

