package com.isp392.controller;

import com.isp392.dto.request.PaymentCreationRequest;
import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.response.PaymentResponse;
import com.isp392.entity.Payment;
import com.isp392.enums.PaymentStatus;
import com.isp392.repository.PaymentRepository;
import com.isp392.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    @Autowired
    PaymentService paymentService;
    @Autowired
    PaymentRepository paymentRepository;

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(@RequestBody PaymentCreationRequest request) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.createPayment(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<PaymentResponse>> getAllPayments(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("paidAt").descending());
        return ApiResponse.<Page<PaymentResponse>>builder()
                .result(paymentService.getAllPayments(pageable))
                .build();
    }


    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getPaymentById(@PathVariable int id) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.getPaymentById(id))
                .build();
    }

    @GetMapping("/{customerId}")
    public ApiResponse<Page<PaymentResponse>> getPaymentByCusId(@PathVariable int customerId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size) {
        return ApiResponse.<Page<PaymentResponse>>builder()
                .result(paymentService.getPaymentByCusId(customerId, PageRequest.of(page, size, Sort.by("paidAt").descending())))
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
    @GetMapping("/cancel")
    public ApiResponse<String> cancelPayment(
            @RequestParam String id,
            @RequestParam(required = false) String status) {

        Payment payment = paymentRepository.findByPaymentLinkId(id);


        if (status != null && status.equalsIgnoreCase("CANCELLED")) {
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
        }

        return ApiResponse.<String>builder()
                .code(200)
                .message("Payment Cancelled")
                .build();
    }

}
