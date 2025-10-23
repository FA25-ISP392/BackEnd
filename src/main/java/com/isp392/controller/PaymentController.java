package com.isp392.controller;

import com.isp392.dto.request.PaymentCreationRequest;
import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.response.PaymentResponse;
import com.isp392.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    @Autowired
    PaymentService paymentService;

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(@RequestBody PaymentCreationRequest request) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.createPayment(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<PaymentResponse>> getAllPayments() {
        return ApiResponse.<List<PaymentResponse>>builder()
                .result(paymentService.getAllPayments())
                .build();
    }


    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getPaymentById(@PathVariable int id) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.getPaymentById(id))
                .build();
    }
    @PostMapping("/cancel/{orderId}")
    public ApiResponse<String> cancelPayment(@PathVariable int orderId) {
        paymentService.cancelPayment(orderId);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Payment Cancelled")
                .build();
    }

}
