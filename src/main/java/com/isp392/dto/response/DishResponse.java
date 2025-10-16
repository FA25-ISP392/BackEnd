package com.isp392.dto.response;

import com.isp392.enums.Category;
import com.isp392.enums.DishType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishResponse {
    int dishId;
    String dishName;
    String description;
    BigDecimal price;
    BigDecimal calo;
    Boolean isAvailable;
    String picture;
    Category category;
    DishType type;
    // ✅ SỐ LƯỢỢNG CÒN LẠI CỦA MÓN ĂN
    int remainingQuantity;

    // ✅ DANH SÁCH TOPPING TÙY CHỌN KÈM SỐ LƯỢNG
    List<ToppingWithQuantityResponse> optionalToppings;
}