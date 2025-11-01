package com.isp392.dto.request;

import com.isp392.enums.ActivityLevel;
import com.isp392.enums.DishType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SuggestionCreationRequest {
    @NotNull(message = "Activity level is required")
    ActivityLevel activityLevel;

    Integer age;

    DishType goal;
}