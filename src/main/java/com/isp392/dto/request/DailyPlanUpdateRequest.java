package com.isp392.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPlanUpdateRequest {
    @PositiveOrZero(message = "Planned quantity must be non-negative")
    private Integer plannedQuantity;

    @PositiveOrZero(message = "Remaining quantity must be non-negative")
    private Integer remainingQuantity;

    // Dành cho Manager/Admin duyệt
    private Boolean status;
}