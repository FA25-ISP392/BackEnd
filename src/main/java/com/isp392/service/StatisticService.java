package com.isp392.service;

import com.isp392.dto.response.ItemSaleStatResponse;
import com.isp392.entity.Dish;
import com.isp392.repository.DailyPlanRepository;
import com.isp392.repository.DishRepository;
import com.isp392.repository.projection.DishSalesProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final DailyPlanRepository dailyPlanRepository;
    private final DishRepository dishRepository;

    public List<ItemSaleStatResponse> getBestSellingDishes(LocalDate startDate, LocalDate endDate, int limit) {
        // PageRequest.of(0, limit) dùng để lấy trang đầu tiên (0) với số lượng 'limit'
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

        // 1. Lấy danh sách dish IDs từ kết quả query
        List<Integer> dishIds = projections.stream()
                .map(DishSalesProjection::getItemId)
                .collect(Collectors.toList());

        // 2. Lấy thông tin (tên) của các món ăn từ DishRepository
        Map<Integer, String> dishNamesMap = dishRepository.findAllById(dishIds).stream()
                .collect(Collectors.toMap(Dish::getDishId, Dish::getDishName));

        // 3. Map sang DTO để trả về
        return projections.stream()
                .map(p -> ItemSaleStatResponse.builder()
                        .itemId(p.getItemId())
                        .itemName(dishNamesMap.getOrDefault(p.getItemId(), "Món không xác định"))
                        .totalSold(p.getTotalSold())
                        .build())
                .collect(Collectors.toList());
    }
}