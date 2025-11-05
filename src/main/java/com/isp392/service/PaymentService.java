package com.isp392.service;

import com.isp392.dto.request.PayOSWebhookBody;
import com.isp392.dto.request.PayOSWebhookData;
import com.isp392.dto.request.PaymentCreationRequest;
import com.isp392.dto.response.PaymentResponse;
import com.isp392.entity.*;
import com.isp392.enums.PaymentMethod;
import com.isp392.enums.PaymentStatus;
import com.isp392.mapper.PaymentMapper;
import com.isp392.repository.OrdersRepository;
import com.isp392.repository.PaymentRepository;
import com.isp392.repository.TableRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import com.fasterxml.jackson.databind.ObjectMapper;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    PaymentMapper paymentMapper;
    PaymentRepository paymentRepository;
    OrdersRepository ordersRepository;
    EmailService emailService;
    TableService tableService;
    PayOS payOs;
    private final TableRepository tableRepository;

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


    @Transactional
    public PaymentResponse createPayment(PaymentCreationRequest request) {
        Orders order = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        double total = order.getOrderDetails().stream()
                .mapToDouble(OrderDetail::getTotalPrice)
                .sum();
        if (total <= 0)
            throw new RuntimeException("Tổng tiền phải lớn hơn 0 để thanh toán.");

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment method: " + request.getMethod());
        }

        Payment payment = paymentRepository.findByOrder_OrderId(request.getOrderId())
                .orElseGet(() -> {
                    log.info("No existing payment found for order {}, creating new.", request.getOrderId());
                    Payment newPayment = paymentMapper.toPayment(request);
                    newPayment.setOrder(order);
                    newPayment.setMethod(method);
                    return newPayment;
                });

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new RuntimeException("Đơn hàng này đã được thanh toán rồi!");
        }
        TableEntity table = order.getTable();
        // CASE 1: Thanh toán tiền mặt
        if (method == PaymentMethod.CASH) {
            log.info(" Processing CASH payment for order {}", request.getOrderId());

            payment.setMethod(PaymentMethod.CASH);
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTotal(total);
            payment.setPaidAt(LocalDateTime.now());

            payment.setPayosOrderCode(null);
            payment.setCheckoutUrl(null);
            payment.setQrCode(null);
            payment.setPaymentLinkId(null);

            order.setPaid(true);
            if (table != null && table.isServing()) {
                table.setServing(false);
                tableRepository.save(table);
            }
            Payment savedPayment = paymentRepository.save(payment);
            ordersRepository.save(order);
            try {
                Account customerAccount = order.getCustomer().getAccount();
                if (customerAccount != null && customerAccount.getEmail() != null) {
                    emailService.sendPaymentSuccessEmail(
                            customerAccount.getEmail(),
                            customerAccount.getFullName(),
                            order,
                            payment.getMethod(),
                            savedPayment.getPaidAt()
                    );
                }
            } catch (Exception e) {
                log.error("Failed to send CASH payment success email for order {}: {}", order.getOrderId(), e.getMessage(), e);
            }

            log.info(" CASH payment completed successfully for order {}", order.getOrderId());
            return paymentMapper.toPaymentResponse(savedPayment);
        }

        // CASE 2: Thanh toán chuyển khoản (PayOS)
        if (method == PaymentMethod.BANK_TRANSFER) {
            log.info(" Processing BANK_TRANSFER for order {}", request.getOrderId());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setMethod(PaymentMethod.BANK_TRANSFER);
            payment.setTotal(total);

            try {
                long payosOrderCode = Long.parseLong(order.getOrderId() + "" + (System.currentTimeMillis() % 10000));
                log.info("Generating PayOS link with orderCode: {}", payosOrderCode);

                List<PaymentLinkItem> items = List.of(); // Không cần item cụ thể nếu chỉ thanh toán tổng

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

                Payment savedPayment = paymentRepository.save(payment);
                log.info("✅ PayOS link created successfully for order {}", order.getOrderId());
                return paymentMapper.toPaymentResponse(savedPayment);

            } catch (Exception e) {
                log.error(" Error creating PayOS link for order {}: {}", request.getOrderId(), e.getMessage());
                throw new RuntimeException("Lỗi tạo thanh toán PayOS: " + e.getMessage());
            }
        }

        // CASE 3: Các phương thức không hỗ trợ
        log.error("Unsupported payment method: {}", method);
        throw new RuntimeException("Unsupported payment method: " + method);
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
        }

        // 2. Parse JSON body
        PayOSWebhookData webhookData;
        try {
            webhookData = objectMapper.readValue(rawBody, PayOSWebhookData.class);
        } catch (Exception e) {
            log.error("Failed to parse PayOS webhook JSON body: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid webhook body format");
        }

        // 3. Lấy đối tượng "data" bên trong
        PayOSWebhookBody transactionData = webhookData.getData();
        if (transactionData == null) {
            log.error("Webhook 'data' object is null in the received payload.");
            throw new RuntimeException("Webhook data object is null");
        }

        // 4. Lấy thông tin từ transactionData
        long orderCodeFromWebhook = transactionData.getOrderCode();
        String status = transactionData.getCode();
        String description = transactionData.getDescription();

        log.info("Processing webhook for order code: {}, Status: {}, Description: {}", orderCodeFromWebhook, status, description);

        // 5. Tìm Payment bằng Order ID (orderCodeFromWebhook)
        Optional<Payment> paymentOpt = paymentRepository.findByPayosOrderCode(orderCodeFromWebhook);


        if (paymentOpt.isEmpty()) {
            log.error("Webhook Error: Payment not found for order code: {}", orderCodeFromWebhook);
            return; // Trả về OK để PayOS không gửi lại
        }
        Payment payment = paymentOpt.get();

        // 6. Kiểm tra trạng thái hiện tại của Payment
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Webhook ignored: Payment for order code {} is already processed. Current status: {}",
                    orderCodeFromWebhook, payment.getStatus());
            return; // Đã xử lý rồi
        }

        // 7. Cập nhật trạng thái Payment và Order
        String code = transactionData.getCode();

        log.info("Webhook status from PayOS: code={}, orderCode={}",
                code, orderCodeFromWebhook);

// --- Thanh toán thành công ---
        if ("00".equalsIgnoreCase(code)) {
            log.info("Payment SUCCESSFUL (Code 00) for order code: {}", orderCodeFromWebhook);
            payment.setStatus(PaymentStatus.COMPLETED);

            LocalDateTime paidAtTime;

            try {
                if (transactionData.getTransactionDateTime() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    paidAtTime = LocalDateTime.parse(transactionData.getTransactionDateTime(), formatter);
                    payment.setPaidAt(paidAtTime);
                } else {
                    paidAtTime = LocalDateTime.now();
                    payment.setPaidAt(paidAtTime);
                }
            } catch (Exception e) {
                log.warn("Không thể parse transactionDateTime: {}", transactionData.getTransactionDateTime());
                paidAtTime = LocalDateTime.now(); 
                payment.setPaidAt(paidAtTime); // fallback
            }

            // Giờ đây, paidAtTime chắc chắn đã được khởi tạo

            Orders order = payment.getOrder();
            if (order != null) {
                order.setPaid(true);
                ordersRepository.save(order);
                TableEntity table = order.getTable();
                if (table != null && table.isServing()) {
                    table.setServing(false);
                    tableRepository.save(table);
                }
                log.info("Order ID {} marked as paid.", order.getOrderId());
                try {
                    Account customerAccount = order.getCustomer().getAccount();
                    if (customerAccount != null && customerAccount.getEmail() != null) {
                        emailService.sendPaymentSuccessEmail(
                                customerAccount.getEmail(),
                                customerAccount.getFullName(),
                                order,
                                payment.getMethod(),
                                paidAtTime
                        );
                    }
                } catch (Exception e) {
                    log.error("Failed to send BANK_TRANSFER payment success email for order {}: {}", order.getOrderId(), e.getMessage(), e);
                }

            } else {
                log.error("Critical Error: Order relationship not found for order code {}", orderCodeFromWebhook);
            }

// --- Giao dịch thất bại hoặc bị hủy (bất kỳ code nào khác "00") ---
        } else {
            log.warn("Payment FAILED/CANCELLED (Code {}) for order code: {}",
                    code, orderCodeFromWebhook);
            // Bạn có thể gộp FAILED và CANCELLED làm một
            payment.setStatus(PaymentStatus.FAILED);
        }

// 8. Lưu thay đổi của Payment
        paymentRepository.save(payment);
        log.info("Updated Payment status to {} for order code: {}", payment.getStatus(), orderCodeFromWebhook);
    }

    // --- HÀM HỖ TRỢ TÍNH HMAC-SHA256 ---
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

    public Page<PaymentResponse> getPaymentByCusId(int customerId, Pageable pageable) {
        Page<Payment> payment = paymentRepository.findByOrder_Customer_CustomerId(customerId, pageable);

        return payment.map(paymentMapper::toPaymentResponse);
    }


    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(paymentMapper::toPaymentResponse);
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
