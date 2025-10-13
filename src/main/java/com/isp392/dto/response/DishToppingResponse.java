package com.isp392.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishToppingResponse {
    int toppingId;
    String toppingName;
    BigDecimal price;
    double calories;
    double gram;

}

