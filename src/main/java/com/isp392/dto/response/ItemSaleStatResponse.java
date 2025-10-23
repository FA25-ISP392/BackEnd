package com.isp392.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemSaleStatResponse {
    private int itemId;
    private String itemName;
    private long totalSold; // Tổng số lượng đã bán
}