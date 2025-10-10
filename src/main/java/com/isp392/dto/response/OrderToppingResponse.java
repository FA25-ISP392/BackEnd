package com.isp392.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderToppingResponse {
    int orderDetailId;
    int toppingId;
    int quantity;
    double toppingPrice;
}
