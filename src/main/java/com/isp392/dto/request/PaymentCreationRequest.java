package com.isp392.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCreationRequest {
    int orderId;
    String method;
    double total;
    String description;
    String buyerName;
    String buyerEmail;
    String buyerPhone;
    String returnUrl;
    String cancelUrl;
}
