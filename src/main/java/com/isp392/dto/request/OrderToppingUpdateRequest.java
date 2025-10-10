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
public class OrderToppingUpdateRequest {
    @NotNull(message = "OrderdetailId không null")
    Integer orderDetailId;

    @NotNull(message = "toppingId không null")
    Integer toppingId;
    @NotBlank(message = "INGREDIENT_NAME_NOT_BLANKED")
    @Size(min = 3, message = "INGREDIENT_NAME_INVALID")
    String name;
    @Min(value = 0, message = "INGREDIENT_CALORIES_NEGATIVE")
    Double calories;

    @Min(value = 0, message = "INGREDIENT_QUANTITY_NEGATIVE")
    Double quantity;

    @Min(value = 0, message = "INGREDIENT_PRICE_NEGATIVE")
    Double price;
}
