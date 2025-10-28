package com.isp392.dto.response;

import com.isp392.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
    int id;
    int orderId;
    String method;
    double total;
    PaymentStatus status;
    Long payosOrderCode;
    String paymentLinkId;
    String checkoutUrl;
    String qrCode;
    String paidAt;
}
