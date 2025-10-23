package com.isp392.repository;

import com.isp392.entity.DailyPlan;
import com.isp392.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.isp392.repository.projection.DishSalesProjection;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyPlanRepository extends JpaRepository<DailyPlan, Integer> {

    Optional<DailyPlan> findByItemIdAndItemTypeAndPlanDate(int itemId, ItemType itemType, LocalDate planDate);

    List<DailyPlan> findByPlanDateAndItemTypeAndItemIdIn(LocalDate date, ItemType itemType, List<Integer> itemIds);

    @Query("SELECT dp FROM DailyPlan dp " +
            "JOIN FETCH dp.plannerStaff ps " +
            "JOIN FETCH ps.account " +
            "WHERE dp.planId = :planId")
    Optional<DailyPlan> findByIdWithPlannerDetails(@Param("planId") int planId);

    @Query("SELECT dp.itemId as itemId, SUM(dp.plannedQuantity - dp.remainingQuantity) as totalSold " +
            "FROM DailyPlan dp " +
            "WHERE dp.itemType = com.isp392.enums.ItemType.DISH " +
            "AND dp.status = true " +
            "AND dp.planDate BETWEEN :startDate AND :endDate " +
            "GROUP BY dp.itemId " +
            "HAVING SUM(dp.plannedQuantity - dp.remainingQuantity) > 0 " + // Chỉ lấy món có bán
            "ORDER BY totalSold DESC")
    List<DishSalesProjection> findBestSellingDishes(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    /**
     * Tìm các món ăn (DISH) bán tệ nhất trong một khoảng thời gian.
     * Chỉ tính các plan đã được duyệt (status = true).
     * Sắp xếp theo tổng số lượng đã bán (totalSold) tăng dần.
     */
    @Query("SELECT dp.itemId as itemId, SUM(dp.plannedQuantity - dp.remainingQuantity) as totalSold " +
            "FROM DailyPlan dp " +
            "WHERE dp.itemType = com.isp392.enums.ItemType.DISH " +
            "AND dp.status = true " +
            "AND dp.planDate BETWEEN :startDate AND :endDate " +
            "GROUP BY dp.itemId " +
            "HAVING SUM(dp.plannedQuantity - dp.remainingQuantity) > 0 " + // Chỉ lấy món có bán
            "ORDER BY totalSold ASC")
    List<DishSalesProjection> findWorstSellingDishes(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);
}