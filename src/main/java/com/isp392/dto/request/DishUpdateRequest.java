package com.isp392.dto.request;

import com.isp392.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishUpdateRequest {
    String dishName;
    String description;
    BigDecimal price;
    BigDecimal calo;
    Category category;
    String picture;
    Boolean isAvailable;
}
