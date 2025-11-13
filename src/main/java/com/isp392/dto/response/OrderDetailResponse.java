package com.isp392.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailResponse {
    Integer orderDetailId;
    Integer orderId;
    int tableId;
    Integer dishId;
    String dishName;
    Double totalPrice;
    String status;
    String note;
    LocalDateTime orderDate;
    String tableName;
    Integer staffId;
    String staffName;

    // Danh sách topping đi kèm món
    List<OrderToppingResponse> toppings;
}
