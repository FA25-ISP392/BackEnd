package com.isp392.controller;

import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.request.OrdersCreationRequest;
import com.isp392.dto.request.OrdersUpdateRequest;
import com.isp392.dto.response.OrdersResponse;
import com.isp392.service.OrdersService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrdersController {

    OrdersService ordersService;

    @PostMapping
    public ApiResponse<OrdersResponse> createOrder(@RequestBody @Valid OrdersCreationRequest request) {
        OrdersResponse result = ordersService.createOrder(request);
        return ApiResponse.<OrdersResponse>builder()
                .result(result)
                .build();
    }

//    @GetMapping
//    public ApiResponse<List<OrdersResponse>> getOrder() {
//        List<OrdersResponse> result = ordersService.getOrder();
//        return ApiResponse.<List<OrdersResponse>>builder()
//                .result(result)
//                .build();
//    }

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
}
