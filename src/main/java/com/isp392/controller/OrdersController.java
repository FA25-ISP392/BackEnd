package com.isp392.controller;

import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.request.OrdersCreationRequest;
import com.isp392.dto.request.OrdersUpdateRequest;
import com.isp392.dto.response.OrdersResponse;
import com.isp392.service.OrdersService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrdersController {

    OrdersService ordersService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ApiResponse<OrdersResponse> createOrder(@RequestBody @Valid OrdersCreationRequest request) {
        OrdersResponse result = ordersService.createOrder(request);
        return ApiResponse.<OrdersResponse>builder()
                .result(result)
                .build();
    }


    @GetMapping("/{orderId}")
    public ApiResponse<OrdersResponse> getOrder(@PathVariable Integer orderId) {
        OrdersResponse result = ordersService.getOrder(orderId);
        return ApiResponse.<OrdersResponse>builder()
                .result(result)
                .build();
    }

    @PutMapping("/{orderId}")
    public ApiResponse<OrdersResponse> updateOrder(@PathVariable Integer orderId, @RequestBody @Valid OrdersUpdateRequest request) {
        OrdersResponse result = ordersService.updateOrder(orderId, request);
        return ApiResponse.<OrdersResponse>builder()
                .result(result)
                .build();
    }

    @DeleteMapping("/{orderId}")
    public ApiResponse<String> deleteOrder(@PathVariable Integer orderId) {
        ordersService.deleteOrder(orderId);
        return ApiResponse.<String>builder()
                .result("Delete Successfully!")
                .build();
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<Page<OrdersResponse>> getOrdersByCustomer(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Page<OrdersResponse> result = ordersService.getOrdersByCustomerId(customerId, pageable);
        return ApiResponse.<Page<OrdersResponse>>builder()
                .result(result)
                .build();
    }
}
