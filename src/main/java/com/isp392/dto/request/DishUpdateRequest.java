package com.isp392.dto.request;

import java.math.BigDecimal;

public class DishUpdateRequest {
    private String dishName;
    private BigDecimal price;
    private String description;

    // Getters and setters
    public String getDishName() { return dishName; }
    public void setDishName(String dishName) { this.dishName = dishName; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
