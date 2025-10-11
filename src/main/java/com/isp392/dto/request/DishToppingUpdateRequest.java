// DishToppingUpdateRequest.java
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
public class DishToppingUpdateRequest {
    @NotNull(message = "Dish ID cannot be null")
    Integer dishId;

    @NotNull(message = "Topping ID cannot be null")
    Integer toppingId;
}
