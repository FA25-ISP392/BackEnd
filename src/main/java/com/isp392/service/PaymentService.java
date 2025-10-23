package com.isp392.service;

import com.isp392.dto.request.PayOSWebhookBody;
import com.isp392.dto.request.PayOSWebhookData;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.payos.model.webhooks.Webhook;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    PaymentMapper paymentMapper;
    PaymentRepository paymentRepository;
    OrdersRepository ordersRepository;
    PayOS payOs;

    @NonFinal
    ObjectMapper objectMapper = new ObjectMapper();

    @Value("${payos.return-url}")
    @NonFinal
    String payosReturnUrl;

    @Value("${payos.cancel-url}")
    @NonFinal // Cần thiết vì class dùng @RequiredArgsConstructor
    String payosCancelUrl;

    @Value("${payos.webhook-key}")
    @NonFinal
    String webhookKey;

    public PaymentResponse createPayment(PaymentCreationRequest request) {
        Orders order = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        Optional<Payment> existingPaymentOpt = paymentRepository.findByOrder_OrderId(request.getOrderId());
        if (existingPaymentOpt.isPresent()) {
            Payment existingPayment = existingPaymentOpt.get();
            if (existingPayment.getStatus() == PaymentStatus.COMPLETED) {
                throw new RuntimeException("Order is already paid.");
            }
            if (existingPayment.getStatus() == PaymentStatus.PENDING && existingPayment.getCheckoutUrl() != null) {
                log.warn("Payment for order {} already exists with status PENDING. Returning existing checkout URL.", request.getOrderId());
                PaymentResponse response = paymentMapper.toPaymentResponse(existingPayment);
                response.setCheckoutUrl(existingPayment.getCheckoutUrl());
                response.setQrCode(existingPayment.getQrCode());
                return response;
            }
        }
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
                long payosOrderCode = order.getOrderId();
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

    @Transactional
    public void processPayOSWebhookManual(String rawBody, String signature) throws Exception {
        // 1. Xác thực chữ ký Webhook (QUAN TRỌNG)
        if (webhookKey == null || webhookKey.isEmpty() || webhookKey.equals("YOUR_WEBHOOK_KEY_HERE")) {
            log.error("PayOS Webhook Key is not configured properly in application.yaml!");
            throw new RuntimeException("Webhook key not configured");
        }
        // Chỉ xác thực nếu có signature được gửi đến
        if (signature != null && !signature.isEmpty()) {
            String calculatedSignature = calculateHMACSHA256(rawBody, webhookKey);
            if (!calculatedSignature.equalsIgnoreCase(signature)) {
                log.warn("Invalid PayOS webhook signature. Received: {}, Calculated: {}", signature, calculatedSignature);
                throw new RuntimeException("Invalid webhook signature");
            }
            log.info("Webhook signature verified successfully.");
        } else {
            log.warn("Missing PayOS webhook signature. Processing without verification (UNSAFE - NOT FOR PRODUCTION).");
            // Khi deploy thật, NÊN ném lỗi ở đây nếu không có signature:
            // throw new RuntimeException("Missing webhook signature");
        }

        // 2. Parse JSON body
        PayOSWebhookData webhookData;
        try {
            webhookData = objectMapper.readValue(rawBody, PayOSWebhookData.class); // <-- THAY ĐỔI
        } catch (Exception e) {
            log.error("Failed to parse PayOS webhook JSON body: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid webhook body format");
        }

        // 3. Lấy đối tượng "data" bên trong
        PayOSWebhookBody transactionData = webhookData.getData(); // <-- THÊM MỚI
        if (transactionData == null) {
            log.error("Webhook 'data' object is null in the received payload.");
            throw new RuntimeException("Webhook data object is null");
        }

        // 4. Lấy thông tin từ transactionData
        long orderCodeFromWebhook = transactionData.getOrderCode(); // <-- Lấy từ data
        String status = transactionData.getCode();                   // <-- Lấy từ data
        String description = transactionData.getDescription();       // <-- Lấy từ data

        log.info("Processing webhook for order code: {}, Status: {}, Description: {}", orderCodeFromWebhook, status, description);

        // 5. Tìm Payment bằng Order ID (orderCodeFromWebhook)
        // ===== THAY ĐỔI CÁCH TÌM PAYMENT =====
        Optional<Payment> paymentOpt = paymentRepository.findByOrder_OrderId((int) orderCodeFromWebhook);
        // HOẶC Optional<Payment> paymentOpt = paymentRepository.findByPayosOrderCode(orderCodeFromWebhook); (Nếu bạn lưu Order ID vào cột payos_order_code)
        // =====================================

        if (paymentOpt.isEmpty()) { // <-- THÊM KIỂM TRA isEmpty
            log.error("Webhook Error: Payment not found for order code: {}", orderCodeFromWebhook);
            return; // Trả về OK để PayOS không gửi lại
        }
        Payment payment = paymentOpt.get();

        // 6. Kiểm tra trạng thái hiện tại của Payment
        // ===== THÊM KIỂM TRA TRẠNG THÁI =====
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Webhook ignored: Payment for order code {} is already processed. Current status: {}",
                    orderCodeFromWebhook, payment.getStatus());
            return; // Đã xử lý rồi
        }
        // =====================================

        // 7. Cập nhật trạng thái Payment và Order
        if ("00".equals(status)) { // <-- So sánh status lấy từ transactionData
            log.info("Payment SUCCESSFUL for order code: {}", orderCodeFromWebhook);
            payment.setStatus(PaymentStatus.COMPLETED);

            Orders order = payment.getOrder();
            if (order != null) {
                order.setPaid(true); // <-- CHỈ SET PAID KHI THÀNH CÔNG
                ordersRepository.save(order);
                log.info("Order ID {} marked as paid.", order.getOrderId());
            } else {
                log.error("Critical Error: Order relationship not found...");
            }
        } else {
            log.warn("Payment FAILED or CANCELLED for order code: {}...", orderCodeFromWebhook, status, description);
            payment.setStatus(PaymentStatus.CANCELLED); // Hoặc FAILED tùy logic
        }

        // 8. Lưu thay đổi của Payment
        paymentRepository.save(payment);
        log.info("Updated Payment status to {} for order code: {}", payment.getStatus(), orderCodeFromWebhook);
    }

    // --- HÀM HỖ TRỢ TÍNH HMAC-SHA256 ---
    // **QUAN TRỌNG:** HÀM NÀY DỰA TRÊN GIẢ ĐỊNH PayOS DÙNG HMAC-SHA256 VÀ KẾT QUẢ HEX LOWERCASE.
    // HÃY LUÔN KIỂM TRA LẠI VỚI TÀI LIỆU CHÍNH THỨC CỦA PAYOS.
    private String calculateHMACSHA256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        final String algo = "HmacSHA256";
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algo);
        Mac mac = Mac.getInstance(algo);
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Chuyển byte array sang chuỗi hex lowercase
        Formatter formatter = new Formatter();
        for (byte b : hmacBytes) {
            formatter.format("%02x", b);
        }
        String hex = formatter.toString();
        formatter.close();
        return hex;
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
