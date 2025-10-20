package com.isp392.service;

import com.isp392.dto.request.PaymentCreationRequest;
import com.isp392.dto.response.PaymentResponse;
import com.isp392.entity.Orders;
import com.isp392.entity.Payment;
import com.isp392.mapper.PaymentMapper;
import com.isp392.repository.OrdersRepository;
import com.isp392.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    PaymentMapper paymentMapper;
    PaymentRepository paymentRepository;
    OrdersRepository ordersRepository;

    public PaymentResponse createPayment(PaymentCreationRequest request) {
        Orders order = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = paymentMapper.toPayment(request);
        payment.setOrder(order);
        paymentRepository.save(payment);
        return paymentMapper.toPaymentResponse(payment);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(paymentMapper::toPaymentResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse getPaymentById(int id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return paymentMapper.toPaymentResponse(payment);
    }
}
