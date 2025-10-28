package com.isp392.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MenuSuggestion {
    private DishResponse drink;
    private DishResponse salad;
    private DishResponse mainCourse;
    private DishResponse dessert;
    private double totalCalories;
    private double targetCaloriesPerMeal;
}