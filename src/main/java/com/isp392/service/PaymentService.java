package com.isp392.service;

import com.isp392.dto.request.PaymentCreationRequest;
import com.isp392.dto.response.PaymentResponse;
import com.isp392.entity.OrderDetail;
import com.isp392.entity.Orders;
import com.isp392.entity.Payment;
import com.isp392.enums.PaymentMethod;
import com.isp392.enums.PaymentStatus;
import com.isp392.mapper.PaymentMapper;
import com.isp392.repository.OrdersRepository;
import com.isp392.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${payos.return-url}")
    @NonFinal
    String payosReturnUrl;

    @Value("${payos.cancel-url}")
    @NonFinal // Cần thiết vì class dùng @RequiredArgsConstructor
    String payosCancelUrl;

    public PaymentResponse createPayment(PaymentCreationRequest request) {
        Orders order = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getPaid() != null && order.getPaid()) {
            throw new RuntimeException("Order is already paid.");
        }
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            throw new RuntimeException("Đơn hàng chưa có món ăn, không thể thanh toán.");
        }



        double total = order.getOrderDetails().stream().mapToDouble(OrderDetail::getTotalPrice).sum();

        if (total <= 0) {
            throw new RuntimeException("Tổng tiền phải lớn hơn 0 để thanh toán.");
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment method: " + request.getMethod());
        }

        Payment payment = paymentMapper.toPayment(request);
        payment.setOrder(order);
        payment.setMethod(method);
        payment.setTotal(total);

        if (method == PaymentMethod.CASH) {
            payment.setStatus(PaymentStatus.COMPLETED);
            order.setPaid(true);
            paymentRepository.save(payment);
            ordersRepository.save(order);
            return paymentMapper.toPaymentResponse(payment);
        } else if (method == PaymentMethod.BANK_TRANSFER) {

            payment.setStatus(PaymentStatus.PENDING);

            try {
                long payosOrderCode = System.currentTimeMillis() % 1000000000;
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
                        .returnUrl(payosReturnUrl)
                        .cancelUrl(payosCancelUrl)
                        .build();

                CreatePaymentLinkResponse res = payOs.paymentRequests().create(req);

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
        } else {
            throw new RuntimeException("Unsupported payment method");
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
    @Transactional
    public PaymentResponse cancelPayment(int orderId) {
        Payment payment = paymentRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán cho đơn hàng #" + orderId));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new RuntimeException("Không thể hủy thanh toán đã hoàn tất.");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        Orders order = payment.getOrder();
        order.setPaid(false);
        ordersRepository.save(order);

        return paymentMapper.toPaymentResponse(payment);
    }

}
