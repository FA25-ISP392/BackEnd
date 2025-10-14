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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
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

    @Transactional
    public DailyPlanResponse createDailyPlan(DailyPlanCreationRequest request, Authentication authentication) {
        String username = authentication.getName();
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

    @Transactional
    public DailyPlanResponse updateDailyPlan(int planId, DailyPlanUpdateRequest request, Authentication authentication) {
        String username = authentication.getName();
        boolean isManagerOrAdmin = hasAuthority(authentication, Role.MANAGER.name()) || hasAuthority(authentication, Role.ADMIN.name());

        DailyPlan dailyPlan = dailyPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        // --- BẮT ĐẦU LOGIC PHÂN QUYỀN CHI TIẾT ---

        // 1. Kiểm tra quyền sở hữu nếu người dùng không phải là quản lý
        if (!isManagerOrAdmin && !dailyPlan.getPlannerStaff().getAccount().getUsername().equals(username)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        // 2. Nếu là Manager/Admin: Có toàn quyền
        if (isManagerOrAdmin) {
            // Họ có thể thay đổi số lượng kế hoạch
            if (request.getPlannedQuantity() != null) {
                dailyPlan.setPlannedQuantity(request.getPlannedQuantity());
            }
            // Họ có thể thay đổi số lượng còn lại (ví dụ: điều chỉnh kho)
            if (request.getRemainingQuantity() != null) {
                dailyPlan.setRemainingQuantity(request.getRemainingQuantity());
            }
            // Họ có thể phê duyệt kế hoạch
            if (request.getStatus() != null && request.getStatus()) {
                Staff approver = staffRepository.findByUsernameWithAccount(username)
                        .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
                dailyPlan.setApproverStaff(approver);
                dailyPlan.setStatus(true);
            }
        }
        // 3. Nếu là Chef (và là chủ sở hữu): Quyền bị giới hạn
        else {
            // Chef không được sửa kế hoạch đã được duyệt
            if (dailyPlan.getStatus()) {
                throw new AppException(ErrorCode.PLAN_ALREADY_APPROVED);
            }

            // Chef chỉ được phép sửa `plannedQuantity`
            if (request.getPlannedQuantity() != null) {
                dailyPlan.setPlannedQuantity(request.getPlannedQuantity());
                // Khi sửa kế hoạch, số lượng còn lại cũng phải cập nhật theo
                dailyPlan.setRemainingQuantity(request.getPlannedQuantity());
            }

            // Một Chef không thể tự duyệt (thay đổi status) hoặc tự ý sửa remainingQuantity.
            // Nếu họ gửi các trường này trong request, chúng sẽ bị bỏ qua.
        }

        DailyPlan updatedPlan = dailyPlanRepository.save(dailyPlan);
        return mapToResponseWithItemName(updatedPlan);
    }


    public void deleteDailyPlan(int planId) {
        if (!dailyPlanRepository.existsById(planId)) {
            throw new AppException(ErrorCode.PLAN_NOT_FOUND);
        }
        dailyPlanRepository.deleteById(planId);
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(authority));
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