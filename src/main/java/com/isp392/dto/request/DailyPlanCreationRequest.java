package com.isp392.dto.request;

import com.isp392.enums.ItemType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPlanCreationRequest {
    @NotNull(message = "Item ID is required")
    private int itemId;

    @NotNull(message = "Item type is required")
    private ItemType itemType;

    @Positive(message = "Planned quantity must be positive")
    private int plannedQuantity;

    @NotNull(message = "Plan date is required")
    private LocalDate planDate;

    // Trường này không bắt buộc, dành cho Manager tạo hộ
    private Integer staffId;
}