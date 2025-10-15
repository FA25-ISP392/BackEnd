package com.isp392.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToppingWithQuantityResponse {
    // Thông tin cơ bản của topping
    private int toppingId;
    private String name;
    private double price;
    private double calories;
    private double gram;

    // Thông tin số lượng bán trong ngày
    private int remainingQuantity;
}