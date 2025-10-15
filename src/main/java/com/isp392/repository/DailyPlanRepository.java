package com.isp392.repository;

import com.isp392.entity.DailyPlan;
import com.isp392.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyPlanRepository extends JpaRepository<DailyPlan, Integer> {
    Optional<DailyPlan> findByItemIdAndItemTypeAndPlanDate(int itemId, ItemType itemType, LocalDate planDate);

    List<DailyPlan> findByPlanDateAndItemTypeAndItemIdIn(LocalDate date, ItemType itemType, List<Integer> itemIds);
}