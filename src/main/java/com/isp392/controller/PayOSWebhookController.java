package com.isp392.controller;

import com.isp392.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.webhooks.Webhook;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PayOSWebhookController {

    private final PaymentService paymentService;

    //
    @PostMapping("/payos-webhook")
    public ResponseEntity<String> handlePayOSWebhook(
            @RequestBody String rawBody, // <-- Nhận JSON dạng String
            @RequestHeader(value = "x-payos-signature", required = false) String signature) { // Header chứa chữ ký

        log.info("Received PayOS Webhook Raw Body."); // Che log body để tránh lộ sensitive data
        if (signature == null || signature.isEmpty()) {
            log.warn("Missing x-payos-signature header");
            // Cân nhắc trả lỗi hoặc xử lý tùy theo yêu cầu bảo mật
        }

        try {
            // Gọi phương thức xử lý thủ công trong service
            paymentService.processPayOSWebhookManual(rawBody, signature);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing PayOS webhook: {}", e.getMessage(), e);
            // Vẫn trả 200 OK để PayOS không gửi lại
            return ResponseEntity.ok("Error processing webhook, but acknowledged: " + e.getMessage());
        }
    }
}