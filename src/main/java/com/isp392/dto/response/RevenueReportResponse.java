package com.isp392.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class RevenueReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private double grandTotalRevenue; // Tổng doanh thu (Tất cả hoặc đã lọc)
    private long grandTotalOrders;    // Tổng số đơn (Tất cả hoặc đã lọc)
    private List<RevenueByMethodResponse> revenueByMethod; // Danh sách chi tiết (Tất cả hoặc đã lọc)
}