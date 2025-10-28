package com.isp392.controller;

import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.response.ItemSaleStatResponse;
import com.isp392.dto.response.RevenueReportResponse;
import com.isp392.enums.PaymentMethod; // 👈 Đảm bảo import này tồn tại
import com.isp392.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    /**
     * Lấy báo cáo doanh thu (tổng hợp và theo phương thức thanh toán).
     * @param day Ngày (tùy chọn)
     * @param month Tháng (tùy chọn)
     * @param year Năm (bắt buộc)
     * @param method Phương thức thanh toán (tùy chọn: CASH, BANK_TRANSFER)
     */
    @GetMapping("/revenue/summary")
    public ApiResponse<RevenueReportResponse> getRevenueReport(
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) Integer month,
            @RequestParam int year,
            @RequestParam(required = false) PaymentMethod method // 👈 Tham số mới để lọc
    ) {
        DateRange range = calculateDateRange(day, month, year);
        // 👇 Truyền `method` vào service
        RevenueReportResponse report = statisticService.getRevenueReport(range.start, range.end, method);
        return ApiResponse.<RevenueReportResponse>builder()
                .result(report)
                .build();
    }


    /**
     * Lấy danh sách món bán chạy nhất.
     * @param day Ngày (tùy chọn)
     * @param month Tháng (tùy chọn)
     * @param year Năm (bắt buộc)
     * @param limit Số lượng món muốn lấy (mặc định là 5)
     */
    @GetMapping("/dishes/best")
    public ApiResponse<List<ItemSaleStatResponse>> getBestSellingDishes(
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) Integer month,
            @RequestParam int year,
            @RequestParam(defaultValue = "5") int limit
    ) {
        DateRange range = calculateDateRange(day, month, year);
        List<ItemSaleStatResponse> stats = statisticService.getBestSellingDishes(range.start, range.end, limit);
        return ApiResponse.<List<ItemSaleStatResponse>>builder()
                .result(stats)
                .build();
    }

    /**
     * Lấy danh sách món bán tệ nhất.
     * @param day Ngày (tùy chọn)
     * @param month Tháng (tùy chọn)
     * @param year Năm (bắt buộc)
     * @param limit Số lượng món muốn lấy (mặc định là 5)
     */
    @GetMapping("/dishes/worst")
    public ApiResponse<List<ItemSaleStatResponse>> getWorstSellingDishes(
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) Integer month,
            @RequestParam int year,
            @RequestParam(defaultValue = "5") int limit
    ) {
        DateRange range = calculateDateRange(day, month, year);
        List<ItemSaleStatResponse> stats = statisticService.getWorstSellingDishes(range.start, range.end, limit);
        return ApiResponse.<List<ItemSaleStatResponse>>builder()
                .result(stats)
                .build();
    }

    // Lớp helper nhỏ để lưu ngày bắt đầu và kết thúc
    private record DateRange(LocalDate start, LocalDate end) {}

    // Hàm private để tính toán khoảng ngày dựa trên input
    private DateRange calculateDateRange(Integer day, Integer month, int year) {
        LocalDate startDate;
        LocalDate endDate;

        if (month == null && day == null) {
            // Thống kê theo NĂM
            startDate = LocalDate.of(year, 1, 1);
            endDate = LocalDate.of(year, 12, 31);
        } else if (day == null) {
            // Thống kê theo THÁNG
            YearMonth yearMonth = YearMonth.of(year, month);
            startDate = yearMonth.atDay(1);
            endDate = yearMonth.atEndOfMonth();
        } else {
            // Thống kê theo NGÀY
            startDate = LocalDate.of(year, month, day);
            endDate = startDate;
        }
        return new DateRange(startDate, endDate);
    }
}