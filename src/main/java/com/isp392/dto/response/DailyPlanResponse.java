package com.isp392.dto.response;

import com.isp392.enums.ItemType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DailyPlanResponse {
    int planId;
    int itemId;
    String itemName;
    ItemType itemType;
    LocalDate planDate;
    int plannedQuantity;
    int remainingQuantity;
    Boolean status;
    int staffId;
    String staffName;
    Integer approvedByStaffId;
    String approverName;
}