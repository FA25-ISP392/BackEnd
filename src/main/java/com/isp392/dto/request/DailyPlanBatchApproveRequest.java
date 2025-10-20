package com.isp392.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class DailyPlanBatchApproveRequest {

    @NotEmpty(message = "List of plan IDs cannot be empty")
    private List<Integer> planIds;
}