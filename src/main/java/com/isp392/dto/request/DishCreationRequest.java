// src/main/java/com/isp392/dto/request/DishCreationRequest.java
package com.isp392.dto.request;

import com.isp392.enums.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishCreationRequest {
    @NotBlank
    String dishName;

    @NotNull
    BigDecimal price;

    String description;

    @NotNull
    Category category;

    String imageUrl;

    Boolean status;
}