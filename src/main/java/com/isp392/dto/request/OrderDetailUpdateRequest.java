package com.isp392.dto.request;

import com.isp392.enums.OrderDetailStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class OrderDetailUpdateRequest {

    @NotNull(message = "OrderDetailId is required")
    Integer orderDetailId;

    String note;

    OrderDetailStatus status; // PENDING, COMPLETED, CANCELLED, v.v.

    // Nếu muốn cập nhật topping
    List<ToppingSelection> toppings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class ToppingSelection {
        @NotNull
        Integer toppingId;
        @Min(1)
        Integer quantity;
    }
}
