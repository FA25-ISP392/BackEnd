package com.isp392.controller;

import com.isp392.dto.request.ApiResponse;
import com.isp392.dto.request.CustomerCreationRequest;
import com.isp392.dto.request.CustomerUpdateRequest;
import com.isp392.dto.response.CustomerResponse;
import com.isp392.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    CustomerService customerService;
    @GetMapping("/{name}")
    ApiResponse<CustomerResponse> getCustomerByName(@PathVariable String name) {
        ApiResponse<CustomerResponse> response = new ApiResponse<>();
        response.setResult(customerService.findByName(name));
        return response;
    }
    @GetMapping
    ApiResponse<List<CustomerResponse>> getAllCustomers() {
        ApiResponse<List<CustomerResponse>> response = new ApiResponse<>();
        response.setResult(customerService.findAll());
        return response;
    }
    @PostMapping
    ApiResponse<CustomerResponse> create(@RequestBody @Valid CustomerCreationRequest customerCreationRequest) {
        ApiResponse<CustomerResponse> response = new ApiResponse<>();
        response.setResult(customerService.createCustomer(customerCreationRequest));
        return response;
    }

    @PutMapping("/{id}")
    ApiResponse<CustomerResponse> update(@PathVariable int id, @RequestBody @Valid CustomerUpdateRequest customerUpdateRequest) {
        ApiResponse<CustomerResponse> response = new ApiResponse<>();
        response.setResult(customerService.updateCustomer(id, customerUpdateRequest));
        return response;
    }
    @DeleteMapping("/{id}")
    ApiResponse<String> deleteCustomer(@PathVariable int id) {
        ApiResponse<String> response = new ApiResponse<>();
        customerService.deleteCustomer(id);
        response.setResult("Delete Successfully!");
        return response;
    }
}
