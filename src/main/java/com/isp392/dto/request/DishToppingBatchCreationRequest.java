package com.isp392.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DishToppingBatchCreationRequest {

    @NotNull(message = "Dish ID cannot be null")
    private Integer dishId;

    @NotEmpty(message = "Topping IDs list cannot be empty")
    private List<Integer> toppingIds;
}