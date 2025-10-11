// DishToppingCreationRequest.java
package com.isp392.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishToppingCreationRequest {
    @NotNull(message = "Dish ID cannot be null")
    int dishId;

    @NotNull(message = "Topping ID cannot be null")
    int toppingId;

}
