package com.isp392.dto.response;

import com.isp392.enums.Category;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishResponse {
    int dishId;
    String dishName;
    String description;
    BigDecimal price;
    BigDecimal calo;
    Boolean isAvailable;
    String picture;
    Category category;
    List<DishToppingResponse> dishToppings; // Chỉ chứa DTO, không reference entity
}
