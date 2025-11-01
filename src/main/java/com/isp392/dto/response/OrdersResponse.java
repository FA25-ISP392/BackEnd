package com.isp392.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrdersResponse {
    Integer orderId;
    Integer customerId;
    Integer tableId;
    LocalDateTime orderDate;
    double totalPrice;
    boolean paid;
    List<OrderDetailResponse> orderDetails;
}
