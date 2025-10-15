package com.isp392.service;

import com.isp392.dto.request.DailyPlanCreationRequest;
import com.isp392.dto.request.DailyPlanUpdateRequest;
import com.isp392.dto.response.DailyPlanResponse;
import com.isp392.entity.*;
import com.isp392.enums.ItemType;
import com.isp392.enums.Role;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.DailyPlanMapper;
import com.isp392.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DailyPlanService {

    DailyPlanRepository dailyPlanRepository;
    DishRepository dishRepository;
    ToppingRepository toppingRepository;
    StaffRepository staffRepository;
    DailyPlanMapper dailyPlanMapper;

    // ... các phương thức createDailyPlan và createDailyPlansBatch sẽ hoạt động đúng ...
    @Transactional
    public DailyPlanResponse createDailyPlan(DailyPlanCreationRequest request, Authentication authentication) {
        String username = authentication.getName();
        // Lời gọi này bây giờ sẽ đúng nhờ hàm hasAuthority đã được sửa
        boolean isManager = hasAuthority(authentication, Role.MANAGER.name());

        Staff planner;

        if (isManager && request.getStaffId() != null) {
            planner = staffRepository.findByIdWithAccount(request.getStaffId())
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        } else {
            planner = staffRepository.findByUsernameWithAccount(username)
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        }

        dailyPlanRepository.findByItemIdAndItemTypeAndPlanDate(
                request.getItemId(), request.getItemType(), request.getPlanDate()
        ).ifPresent(plan -> {
            throw new AppException(ErrorCode.PLAN_ALREADY_EXISTS);
        });

        validateItemExists(request.getItemId(), request.getItemType());

        DailyPlan dailyPlan = dailyPlanMapper.toDailyPlan(request);
        dailyPlan.setPlannerStaff(planner);
        dailyPlan.setRemainingQuantity(request.getPlannedQuantity());
        dailyPlan.setStatus(false);
        dailyPlan.setPlanDate(request.getPlanDate());

        DailyPlan savedPlan = dailyPlanRepository.save(dailyPlan);
        return mapToResponseWithItemName(savedPlan);
    }

    // Phương thức updateDailyPlan và các phương thức khác bây giờ sẽ chạy đúng
    @Transactional
    public DailyPlanResponse updateDailyPlan(int planId, DailyPlanUpdateRequest request, Authentication authentication) {
        String username = authentication.getName();
        boolean isManagerOrAdmin = hasAuthority(authentication, Role.MANAGER.name()) || hasAuthority(authentication, Role.ADMIN.name());

        DailyPlan dailyPlan = dailyPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        if (!isManagerOrAdmin && !dailyPlan.getPlannerStaff().getAccount().getUsername().equals(username)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        if (!isManagerOrAdmin && dailyPlan.getStatus()) {
            throw new AppException(ErrorCode.PLAN_ALREADY_APPROVED);
        }

        if (isManagerOrAdmin) {
            if (request.getPlannedQuantity() != null) {
                dailyPlan.setPlannedQuantity(request.getPlannedQuantity());
            }
            if (request.getRemainingQuantity() != null) {
                dailyPlan.setRemainingQuantity(request.getRemainingQuantity());
            }
            if (request.getStatus() != null && request.getStatus()) {
                Staff approver = staffRepository.findByUsernameWithAccount(username)
                        .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
                dailyPlan.setApproverStaff(approver);
                dailyPlan.setStatus(true);
            }
        } else {
            if (request.getPlannedQuantity() != null) {
                dailyPlan.setPlannedQuantity(request.getPlannedQuantity());
                dailyPlan.setRemainingQuantity(request.getPlannedQuantity());
            }
        }

        DailyPlan updatedPlan = dailyPlanRepository.save(dailyPlan);
        return mapToResponseWithItemName(updatedPlan);
    }

    // ✅ **ĐÂY LÀ CHỖ SỬA**
    // Hàm helper này bây giờ đã biết cách thêm tiền tố "ROLE_"
    private boolean hasAuthority(Authentication authentication, String role) {
        String roleWithPrefix = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(roleWithPrefix));
    }

    // ... tất cả các phương thức khác được giữ nguyên ...
    @Transactional
    public List<DailyPlanResponse> createDailyPlansBatch(List<DailyPlanCreationRequest> requests, Authentication authentication) {
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }

        String username = authentication.getName();
        Staff planner = staffRepository.findByUsernameWithAccount(username)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        List<DailyPlan> plansToSave = requests.stream().map(request -> {
            dailyPlanRepository.findByItemIdAndItemTypeAndPlanDate(
                    request.getItemId(), request.getItemType(), request.getPlanDate()
            ).ifPresent(plan -> {
                throw new AppException(ErrorCode.PLAN_ALREADY_EXISTS_BATCH);
            });
            validateItemExists(request.getItemId(), request.getItemType());

            if (request.getStaffId() != null && !request.getStaffId().equals(planner.getStaffId())) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }

            DailyPlan dailyPlan = dailyPlanMapper.toDailyPlan(request);
            dailyPlan.setPlannerStaff(planner);
            dailyPlan.setRemainingQuantity(request.getPlannedQuantity());
            dailyPlan.setStatus(false);
            dailyPlan.setPlanDate(request.getPlanDate());
            return dailyPlan;
        }).collect(Collectors.toList());

        List<DailyPlan> savedPlans = dailyPlanRepository.saveAll(plansToSave);
        return savedPlans.stream()
                .map(this::mapToResponseWithItemName)
                .collect(Collectors.toList());
    }

    public List<DailyPlanResponse> getAllDailyPlans() {
        List<DailyPlan> plans = dailyPlanRepository.findAll();
        if (plans.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> dishIds = plans.stream().filter(p -> p.getItemType() == ItemType.DISH).map(DailyPlan::getItemId).toList();
        List<Integer> toppingIds = plans.stream().filter(p -> p.getItemType() == ItemType.TOPPING).map(DailyPlan::getItemId).toList();

        Map<Integer, String> dishNames = dishRepository.findAllById(dishIds).stream().collect(Collectors.toMap(Dish::getDishId, Dish::getDishName));
        Map<Integer, String> toppingNames = toppingRepository.findAllById(toppingIds).stream().collect(Collectors.toMap(Topping::getToppingId, Topping::getName));

        return plans.stream().map(plan -> {
            DailyPlanResponse response = dailyPlanMapper.toDailyPlanResponse(plan);
            String itemName = switch (plan.getItemType()) {
                case DISH -> dishNames.getOrDefault(plan.getItemId(), "N/A");
                case TOPPING -> toppingNames.getOrDefault(plan.getItemId(), "N/A");
            };
            response.setItemName(itemName);
            return response;
        }).collect(Collectors.toList());
    }

    public DailyPlanResponse getDailyPlanById(int planId) {
        DailyPlan dailyPlan = dailyPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));
        return mapToResponseWithItemName(dailyPlan);
    }

    public void deleteDailyPlan(int planId) {
        if (!dailyPlanRepository.existsById(planId)) {
            throw new AppException(ErrorCode.PLAN_NOT_FOUND);
        }
        dailyPlanRepository.deleteById(planId);
    }

    private void validateItemExists(int itemId, ItemType itemType) {
        boolean exists = switch (itemType) {
            case DISH -> dishRepository.existsById(itemId);
            case TOPPING -> toppingRepository.existsById(itemId);
        };
        if (!exists) {
            throw new AppException(ErrorCode.ITEM_NOT_FOUND);
        }
    }

    private DailyPlanResponse mapToResponseWithItemName(DailyPlan dailyPlan) {
        DailyPlanResponse response = dailyPlanMapper.toDailyPlanResponse(dailyPlan);
        String itemName = switch (dailyPlan.getItemType()) {
            case DISH -> dishRepository.findById(dailyPlan.getItemId()).map(Dish::getDishName).orElse("N/A");
            case TOPPING -> toppingRepository.findById(dailyPlan.getItemId()).map(Topping::getName).orElse("N/A");
        };
        response.setItemName(itemName);
        return response;
    }
}