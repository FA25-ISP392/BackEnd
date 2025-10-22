package com.isp392.service;

import com.isp392.dto.request.PaymentCreationRequest;
import com.isp392.dto.response.PaymentResponse;
import com.isp392.entity.OrderDetail;
import com.isp392.entity.Orders;
import com.isp392.entity.Payment;
import com.isp392.mapper.PaymentMapper;
import com.isp392.repository.OrdersRepository;
import com.isp392.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    PaymentMapper paymentMapper;
    PaymentRepository paymentRepository;
    OrdersRepository ordersRepository;
    PayOS payOs;

    public PaymentResponse createPayment(PaymentCreationRequest request) {
        Orders order = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        try {
            long payosOrderCode = System.currentTimeMillis() % 1000000000;
            double total = order.getOrderDetails().stream().mapToDouble(OrderDetail::getTotalPrice).sum();


            List<PaymentLinkItem> items = List.of(
                    PaymentLinkItem.builder()
                            .name("Meal Order")
                            .quantity(1)
                            .price((long) total)
                            .build()
            );

            CreatePaymentLinkRequest req = CreatePaymentLinkRequest.builder()
                    .orderCode(payosOrderCode)
                    .amount((long) total)
                    .description("Thanh toán đơn hàng #" + order.getOrderId())
                    .items(items)
                    .returnUrl("https://localhost:5173/payment/success")
                    .cancelUrl("https://localhost:5173/payment/cancel")
                    .build();

            CreatePaymentLinkResponse res = payOs.paymentRequests().create(req);

            Payment payment = paymentMapper.toPayment(request);
            payment.setOrder(order);
            payment.setPayosOrderCode(payosOrderCode);
            payment.setCheckoutUrl(res.getCheckoutUrl());
            payment.setQrCode(res.getQrCode());
            payment.setPaymentLinkId(res.getPaymentLinkId());
            paymentRepository.save(payment);

            PaymentResponse response = paymentMapper.toPaymentResponse(payment);
            response.setCheckoutUrl(res.getCheckoutUrl());
            response.setQrCode(res.getQrCode());
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo thanh toán PayOS: " + e.getMessage());
        }
//        Payment payment = paymentMapper.toPayment(request);
//        payment.setOrder(order);
//        paymentRepository.save(payment);
//        return paymentMapper.toPaymentResponse(payment);
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
