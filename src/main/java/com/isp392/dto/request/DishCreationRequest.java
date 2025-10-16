package com.isp392.dto.request;

import com.isp392.enums.Category;
import com.isp392.enums.DishType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishCreationRequest {
    @NotBlank(message = "DISH_NAME_REQUIRED")
    String dishName;

    @NotBlank(message = "DISH_DESCRIPTION_NOT_BLANKED")
    String description;

    @NotNull(message = "DISH_PRICE_REQUIRED")
    @PositiveOrZero(message = "DISH_PRICE_NEGATIVE")
    BigDecimal price;

    @NotNull(message = "DISH_CALO_REQUIRED")
    @PositiveOrZero(message = "DISH_CALO_NEGATIVE")
    BigDecimal calo;

    @NotNull(message = "DISH_CATEGORY_REQUIRED")
    Category category;

    String picture;

    @NotNull(message = "DISH_STATUS_REQUIRED")
    Boolean isAvailable;

    private DishType type;
}
