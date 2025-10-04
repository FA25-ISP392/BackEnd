package com.isp392.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IngredientCreationRequest {
    @NotBlank(message = "Ingredient Name can not empty")
    @Size(min = 3, message = "Ingredient Name is Invalid")
    String name;

    @NotNull(message = "Calories is required")
    @Min(value = 0, message = "Calories must be >= 0")
    double calories;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be >= 0")
    double quantity;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be >= 0")
    double price;
}
