package com.isp392.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class SuggestionResponse {
    DishResponse drink;
    DishResponse salad;
    DishResponse mainCourse;
    DishResponse dessert;
    double totalCalories;
    double targetCaloriesPerMeal;
}