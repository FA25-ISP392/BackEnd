package com.isp392.controller;

import com.isp392.dto.request.OrderDetailCreationRequest;
import com.isp392.dto.request.OrderDetailUpdateRequest;
import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.response.OrderDetailResponse;
import com.isp392.service.OrderDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order-details")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class OrderDetailController {

    OrderDetailService orderDetailService;


    @PostMapping
    public ApiResponse<OrderDetailResponse> create(@RequestBody @Valid OrderDetailCreationRequest request) {
        OrderDetailResponse result = orderDetailService.createOrderDetail(request);
        return ApiResponse.<OrderDetailResponse>builder()
                .result(result)
                .build();
    }

    @GetMapping("/{orderDetailId}")
    public ApiResponse<OrderDetailResponse> get(@PathVariable Integer orderDetailId) {
        OrderDetailResponse result = orderDetailService.getOrderDetail(orderDetailId);
        return ApiResponse.<OrderDetailResponse>builder()
                .result(result)
                .build();
    }

//    @PostMapping("/{orderDetailId}/toppings")
//    public ApiResponse<OrderDetailResponse> addTopping(@PathVariable Integer orderDetailId, @RequestBody OrderDetailUpdateRequest request) {
//
//        OrderDetailResponse response = orderDetailService.updateOrderDetail(orderDetailId, request);
//        return ApiResponse.<OrderDetailResponse>builder()
//                .result(response)
//                .build();
//    }

}
