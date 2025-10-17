package com.isp392.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderToppingResponse {
    Integer toppingId;
    String toppingName;
    Integer quantity;
    Double toppingPrice;
}
