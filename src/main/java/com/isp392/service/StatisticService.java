package com.isp392.service;

import com.isp392.dto.response.ItemSaleStatResponse;
import com.isp392.dto.response.RevenueByMethodResponse;
import com.isp392.dto.response.RevenueReportResponse;
import com.isp392.entity.Dish;
import com.isp392.enums.PaymentMethod; // 👈 Đảm bảo import này tồn tại
import com.isp392.repository.DailyPlanRepository;
import com.isp392.repository.DishRepository;
import com.isp392.repository.PaymentRepository;
import com.isp392.repository.projection.DishSalesProjection;
import com.isp392.repository.projection.RevenueByMethodProjection;
import com.isp392.repository.projection.TotalRevenueProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final DailyPlanRepository dailyPlanRepository;
    private final DishRepository dishRepository;
    private final PaymentRepository paymentRepository;

    public List<ItemSaleStatResponse> getBestSellingDishes(LocalDate startDate, LocalDate endDate, int limit) {
        PageRequest pageable = PageRequest.of(0, limit);
        List<DishSalesProjection> projections = dailyPlanRepository.findBestSellingDishes(startDate, endDate, pageable);
        return mapProjectionsToResponse(projections);
    }

    public List<ItemSaleStatResponse> getWorstSellingDishes(LocalDate startDate, LocalDate endDate, int limit) {
        PageRequest pageable = PageRequest.of(0, limit);
        List<DishSalesProjection> projections = dailyPlanRepository.findWorstSellingDishes(startDate, endDate, pageable);
        return mapProjectionsToResponse(projections);
    }

    private List<ItemSaleStatResponse> mapProjectionsToResponse(List<DishSalesProjection> projections) {
        if (projections.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> dishIds = projections.stream()
                .map(DishSalesProjection::getItemId)
                .collect(Collectors.toList());

        Map<Integer, String> dishNamesMap = dishRepository.findAllById(dishIds).stream()
                .collect(Collectors.toMap(Dish::getDishId, Dish::getDishName));

        return projections.stream()
                .map(p -> ItemSaleStatResponse.builder()
                        .itemId(p.getItemId())
                        .itemName(dishNamesMap.getOrDefault(p.getItemId(), "Món không xác định"))
                        .totalSold(p.getTotalSold())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Lấy báo cáo doanh thu tổng hợp và chi tiết theo phương thức thanh toán.
     *
     * @param startDate Ngày bắt đầu (bao gồm)
     * @param endDate   Ngày kết thúc (bao gồm)
     * @param method    Phương thức thanh toán (null = tất cả, hoặc lọc theo CASH/BANK_TRANSFER)
     * @return Báo cáo doanh thu
     */
    // 👇 Sửa lại chữ ký hàm để nhận `method`
    public RevenueReportResponse getRevenueReport(LocalDate startDate, LocalDate endDate, PaymentMethod method) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 1. Query tổng doanh thu (Không thay đổi)
        TotalRevenueProjection totalProj = paymentRepository.findTotalRevenueByDateRange(startDateTime, endDateTime);

        // 2. Query doanh thu theo từng phương thức (Không thay đổi)
        List<RevenueByMethodProjection> methodProjs = paymentRepository.findRevenueByMethodAndDateRange(startDateTime, endDateTime);

        // 3. Chuyển đổi projection sang DTO (Không thay đổi)
        List<RevenueByMethodResponse> revenueByMethodList = methodProjs.stream()
                .map(proj -> RevenueByMethodResponse.builder()
                        .method(proj.getMethod())
                        .totalRevenue(proj.getTotalRevenue() != null ? proj.getTotalRevenue() : 0.0)
                        .totalOrders(proj.getTotalOrders() != null ? proj.getTotalOrders() : 0L)
                        .build())
                .collect(Collectors.toList());

        // 👇 BẮT ĐẦU LOGIC LỌC MỚI

        double grandTotalRevenue;
        long grandTotalOrders;
        List<RevenueByMethodResponse> finalRevenueList;

        if (method == null) {
            // --- TRƯỜNG HỢP 1: Không lọc (giống như cũ) ---
            grandTotalRevenue = (totalProj != null && totalProj.getTotalRevenue() != null) ? totalProj.getTotalRevenue() : 0.0;
            grandTotalOrders = (totalProj != null && totalProj.getTotalOrders() != null) ? totalProj.getTotalOrders() : 0L;
            finalRevenueList = revenueByMethodList; // Giữ nguyên danh sách đầy đủ

        } else {
            // --- TRƯỜNG HỢP 2: Lọc theo `method` ---

            // Lọc danh sách chi tiết chỉ giữ lại method được yêu cầu
            finalRevenueList = revenueByMethodList.stream()
                    .filter(r -> r.getMethod() == method)
                    .collect(Collectors.toList());

            // Tính lại tổng doanh thu và tổng đơn hàng TỪ danh sách đã lọc
            grandTotalRevenue = finalRevenueList.stream()
                    .mapToDouble(RevenueByMethodResponse::getTotalRevenue)
                    .sum();

            grandTotalOrders = finalRevenueList.stream()
                    .mapToLong(RevenueByMethodResponse::getTotalOrders)
                    .sum();
        }

        // 👆 KẾT THÚC LOGIC LỌC MỚI

        // 5. Xây dựng DTO Response cuối cùng (dùng các biến `final...` đã tính toán)
        return RevenueReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .grandTotalRevenue(grandTotalRevenue)
                .grandTotalOrders(grandTotalOrders)
                .revenueByMethod(finalRevenueList) // Trả về danh sách đã lọc (hoặc đầy đủ)
                .build();
    }
}