package com.isp392.dto.response;

import com.isp392.enums.Category;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishResponse {
    int id;
    String dishName;
    BigDecimal price;
    String description;
    Category category;
    String imageUrl;
    Boolean status;
}
