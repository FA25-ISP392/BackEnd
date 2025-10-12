package com.isp392.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ToppingUpdateRequest {
    @NotBlank(message = "INGREDIENT_NAME_NOT_BLANKED")
    @Size(min = 3, message = "INGREDIENT_NAME_INVALID")
    String name;
    @Min(value = 0, message = "INGREDIENT_CALORIES_NEGATIVE")
    Double calories;

    @Min(value = 0, message = "INGREDIENT_QUANTITY_NEGATIVE")
    Double quantity;

    @Min(value = 0, message = "INGREDIENT_PRICE_NEGATIVE")
    Double price;

    @Min(value = 0, message = "INGREDIENT_GRAM_NEGATIVE")
    Double gram;
}
