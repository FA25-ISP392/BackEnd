package com.isp392.dto.request;

import com.isp392.enums.ActivityLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SuggestionRequest {
    @NotNull(message = "Activity level is required")
    private ActivityLevel activityLevel;

    @NotNull(message = "Number of meals is required")
    @Min(value = 1, message = "Must have at least 1 meal")
    private Integer numberOfMeals;
}