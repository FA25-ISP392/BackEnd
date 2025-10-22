package com.isp392.dto.response;

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
    String status;
    Long payosOrderCode;
    String paymentLinkId;
    String checkoutUrl;
    String qrCode;
}
