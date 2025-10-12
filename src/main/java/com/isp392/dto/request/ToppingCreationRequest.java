package com.isp392.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ToppingCreationRequest {
    @NotBlank(message = "INGREDIENT_NAME_NOT_BLANKED")
    @Size(min = 3, message = "INGREDIENT_NAME_INVALID")
    String name;

    @NotNull(message = "INGREDIENT_CALORIES_REQUIRED")
    @Min(value = 0, message = "INGREDIENT_CALORIES_NEGATIVE")
    double calories;

    @NotNull(message = "INGREDIENT_QUANTITY_REQUIRED")
    @Min(value = 0, message = "INGREDIENT_QUANTITY_NEGATIVE")
    double quantity;

    @NotNull(message = "INGREDIENT_PRICE_REQUIRED")
    @Min(value = 0, message = "INGREDIENT_PRICE_NEGATIVE")
    double price;

    @NotNull(message = "INGREDIENT_GRAM_REQUIRED")
    @Min(value = 0, message = "INGREDIENT_GRAM_NEGATIVE")
    double gram;
}
