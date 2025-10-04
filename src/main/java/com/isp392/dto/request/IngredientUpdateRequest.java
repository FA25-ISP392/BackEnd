package com.isp392.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IngredientUpdateRequest {
    String name;
    @Min(value = 0, message = "Calories must be >= 0")
    Double calories;
    @Min(value = 0, message = "quantity must be >= 0")
    Double quantity;
    @Min(value = 0, message = "price must be >= 0")
    Double price;
}
