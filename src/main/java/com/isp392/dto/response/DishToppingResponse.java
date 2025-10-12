package com.isp392.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishToppingResponse {
    int dishId;
    int toppingId;
    String dishName;
    String toppingName;
}
