package com.isp392.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailCreationRequest {
    @NotNull
    Integer orderId;
    @NotNull
    Integer dishId;

    String note;

    List<ToppingSelection> toppings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ToppingSelection {
        Integer toppingId;
        Integer quantity;
    }
}
