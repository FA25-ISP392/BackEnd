package com.isp392.controller;

import com.isp392.dto.request.ApiResponse;
import com.isp392.dto.request.CustomerCreationRequest;
import com.isp392.dto.request.CustomerUpdateRequest;
import com.isp392.dto.response.CustomerResponse;
import com.isp392.entity.Customer;
import com.isp392.service.CustomerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerController {

    CustomerService customerService;

    @GetMapping("/{name}")
    ApiResponse<List<CustomerResponse>> getCustomer(@RequestParam String name) {
        ApiResponse<List<CustomerResponse>> response = new ApiResponse<>();
        response.setResult(customerService.findByName(name));
        return response;
    }

    @GetMapping
    ApiResponse<List<CustomerResponse>> getCustomer() {
        ApiResponse<List<CustomerResponse>> response = new ApiResponse<>();
        response.setResult(customerService.findAll());
        return response;
    }

    @PostMapping
    ApiResponse<Customer> createCustomer(@RequestBody @Valid CustomerCreationRequest request) {
        ApiResponse<Customer> response = new ApiResponse<>();
        response.setResult(customerService.createCustomer(request));
        return response;
    }

    @PutMapping("/{id}")
    ApiResponse<CustomerResponse> updateCustomer(@PathVariable Long id, @RequestBody @Valid CustomerUpdateRequest customerUpdateRequest) {
        ApiResponse<CustomerResponse> response = new ApiResponse<>();
        response.setResult(customerService.updateCustomer(id, customerUpdateRequest));
        return response;
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteCustomer(@PathVariable Long id) {
        ApiResponse<String> response = new ApiResponse<>();
        customerService.deleteCustomer(id);
        response.setResult("Delete Successfully!");
        return response;
    }
}
