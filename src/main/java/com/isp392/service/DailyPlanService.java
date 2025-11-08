package com.isp392.service;

import com.isp392.dto.request.DailyPlanBatchApproveRequest;
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



    @Transactional
    public DailyPlanResponse createDailyPlan(DailyPlanCreationRequest request, Authentication authentication) {
        // VIỆC 1: Lấy Staff
        Staff planner = getPlannerFromAuth(authentication, request.getStaffId());

        // VIỆC 2: Kiểm tra nghiệp vụ
        validatePlanDoesNotExist(request.getItemId(), request.getItemType(), request.getPlanDate());
        validateItemExists(request.getItemId(), request.getItemType());

        // VIỆC 3: Tạo entity
        DailyPlan dailyPlan = buildNewPlan(request, planner);

        // VIỆC 4: Lưu
        DailyPlan savedPlan = dailyPlanRepository.save(dailyPlan);

        // VIỆC 5: Map và trả về
        String itemName = getItemName(savedPlan.getItemType(), savedPlan.getItemId());
        return mapToResponse(savedPlan, itemName);
    }

    @Transactional
    public List<DailyPlanResponse> createDailyPlansBatch(List<DailyPlanCreationRequest> requests, Authentication authentication) {
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }

        // VIỆC 1: Xác định người lập kế hoạch
        Staff planner = getPlannerForBatch(requests.get(0), authentication);
        final Integer targetStaffId = planner.getStaffId();

        List<DailyPlan> plansToSave = new ArrayList<>();

        // VIỆC 2: Xử lý từng request
        for (DailyPlanCreationRequest request : requests) {
            DailyPlan planToSave = processSinglePlanRequest(request, planner, targetStaffId);
            if (planToSave != null) {
                plansToSave.add(planToSave);
            }
        }

        // VIỆC 3: Lưu
        List<DailyPlan> savedPlans = dailyPlanRepository.saveAll(plansToSave);

        // VIỆC 4: Map hiệu quả (tránh N+1)
        return mapToResponseList(savedPlans);
    }

    @Transactional(readOnly = true)
    public List<DailyPlanResponse> getAllDailyPlans() {
        // VIỆC 1: Lấy dữ liệu
        List<DailyPlan> plans = dailyPlanRepository.findAll();
        // VIỆC 2: Map hiệu quả
        return mapToResponseList(plans);
    }

    @Transactional(readOnly = true)
    public DailyPlanResponse getDailyPlanById(int planId) {
        // VIỆC 1: Lấy dữ liệu
        DailyPlan dailyPlan = findPlanById(planId);
        // VIỆC 2: Lấy tên item
        String itemName = getItemName(dailyPlan.getItemType(), dailyPlan.getItemId());
        // VIỆC 3: Map
        return mapToResponse(dailyPlan, itemName);
    }

    @Transactional
    public DailyPlanResponse updateDailyPlan(int planId, DailyPlanUpdateRequest request, Authentication authentication) {
        // VIỆC 1: Lấy thông tin
        Staff staff = getPlannerFromAuth(authentication, null); // Lấy staff đang thực thi
        boolean isManagerOrAdmin = hasAuthority(authentication, Role.MANAGER.name()) || hasAuthority(authentication, Role.ADMIN.name());
        DailyPlan dailyPlan = findPlanByIdWithPlanner(planId);

        // VIỆC 2: Kiểm tra (Validate)
        validateAuthorization(dailyPlan, staff, isManagerOrAdmin);
        validateCanUpdate(dailyPlan, isManagerOrAdmin);

        // VIỆC 3: Áp dụng thay đổi
        applyUpdates(dailyPlan, request, staff, isManagerOrAdmin);

        // VIỆC 4: Lưu
        DailyPlan updatedPlan = dailyPlanRepository.save(dailyPlan);

        // VIỆC 5: Map
        String itemName = getItemName(updatedPlan.getItemType(), updatedPlan.getItemId());
        return mapToResponse(updatedPlan, itemName);
    }

    @Transactional
    public List<DailyPlanResponse> approveDailyPlansBatch(DailyPlanBatchApproveRequest request, Authentication authentication) {
        // VIỆC 1: Lấy người duyệt
        Staff approver = getPlannerFromAuth(authentication, null); // Lấy staff đang thực thi

        // VIỆC 2: Lấy các plan
        List<DailyPlan> plansToApprove = dailyPlanRepository.findAllById(request.getPlanIds());

        // VIỆC 3: Duyệt và cập nhật
        for (DailyPlan plan : plansToApprove) {
            if (!plan.getStatus()) {
                plan.setStatus(true);
                plan.setApproverStaff(approver);
            }
        }

        // VIỆC 4: Lưu
        List<DailyPlan> savedPlans = dailyPlanRepository.saveAll(plansToApprove);

        // VIỆC 5: Map hiệu quả
        return mapToResponseList(savedPlans);
    }

    // (Hàm này đã tuân thủ SRP, giữ nguyên)
    public void deleteDailyPlan(int planId) {
        if (!dailyPlanRepository.existsById(planId)) {
            throw new AppException(ErrorCode.PLAN_NOT_FOUND);
        }
        dailyPlanRepository.deleteById(planId);
    }


    // ===================================================================
    // API CUNG CẤP CHO CÁC SERVICE KHÁC (ĐÃ REFACTOR)
    // ===================================================================

    @Transactional
    public void updateRemainingQuantity(Integer itemId, ItemType itemType, LocalDate date, int quantityChange) {
        if (quantityChange == 0) return;

        // VIỆC 1: Tìm plan
        DailyPlan dailyPlan = findPlanByItemAndDate(itemId, itemType, date);

        // VIỆC 2: Kiểm tra
        validatePlanIsActive(dailyPlan);

        // VIỆC 3: Tính toán và kiểm tra
        int newRemaining = dailyPlan.getRemainingQuantity() + quantityChange;
        validateNewRemainingQuantity(newRemaining);

        // VIỆC 4: Cập nhật và Lưu
        dailyPlan.setRemainingQuantity(newRemaining);
        dailyPlanRepository.save(dailyPlan);
    }

    @Transactional(readOnly = true)
    public Map<Integer, Integer> getRemainingQuantitiesForItems(ItemType itemType, List<Integer> itemIds, LocalDate date) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<DailyPlan> plans = dailyPlanRepository.findByPlanDateAndItemTypeAndItemIdIn(date, itemType, itemIds);

        // Trả về Map (ItemId -> Quantity) chỉ của các plan ĐÃ ĐƯỢC DUYỆT
        return plans.stream()
                .filter(plan -> plan.getStatus() != null && plan.getStatus())
                .collect(Collectors.toMap(
                        DailyPlan::getItemId,
                        DailyPlan::getRemainingQuantity
                ));
    }


    // ===================================================================
    // HELPER: LẤY DỮ LIỆU (FINDERS)
    // ===================================================================

    private DailyPlan findPlanById(int planId) {
        return dailyPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));
    }

    private DailyPlan findPlanByIdWithPlanner(int planId) {
        return dailyPlanRepository.findByIdWithPlannerDetails(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));
    }

    private DailyPlan findPlanByItemAndDate(Integer itemId, ItemType itemType, LocalDate date) {
        return dailyPlanRepository.findByItemIdAndItemTypeAndPlanDate(itemId, itemType, date)
                .orElseThrow(() -> new AppException(
                        itemType == ItemType.DISH ? ErrorCode.DISH_NOT_FOUND : ErrorCode.TOPPING_NOT_FOUND
                ));
    }

    private Staff getPlannerFromAuth(Authentication authentication, Integer staffId) {
        String username = authentication.getName();
        boolean isManager = hasAuthority(authentication, Role.MANAGER.name());

        // Nếu là Manager VÀ có staffId được cung cấp (tạo hộ)
        if (isManager && staffId != null) {
            return staffRepository.findByIdWithAccount(staffId)
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        }

        // Ngược lại, lấy chính staff đang đăng nhập
        return staffRepository.findByUsernameWithAccount(username)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
    }

    // (Helper này giữ nguyên từ code gốc)
    private Staff getPlannerForBatch(DailyPlanCreationRequest firstRequest, Authentication authentication) {
        String username = authentication.getName();
        boolean isManager = hasAuthority(authentication, Role.MANAGER.name());

        if (isManager && firstRequest.getStaffId() != null) {
            return staffRepository.findByIdWithAccount(firstRequest.getStaffId())
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        } else {
            return staffRepository.findByUsernameWithAccount(username)
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        }
    }

    // ===================================================================
    // HELPER: KIỂM TRA (VALIDATORS)
    // ===================================================================

    private boolean hasAuthority(Authentication authentication, String role) {
        String roleWithPrefix = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(roleWithPrefix));
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

    private void validatePlanDoesNotExist(int itemId, ItemType itemType, LocalDate date) {
        dailyPlanRepository.findByItemIdAndItemTypeAndPlanDate(itemId, itemType, date)
                .ifPresent(plan -> {
                    throw new AppException(ErrorCode.PLAN_ALREADY_EXISTS);
                });
    }

    private void validateAuthorization(DailyPlan dailyPlan, Staff staff, boolean isManagerOrAdmin) {
        if (!isManagerOrAdmin && !dailyPlan.getPlannerStaff().getAccount().getUsername().equals(staff.getAccount().getUsername())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void validateCanUpdate(DailyPlan dailyPlan, boolean isManagerOrAdmin) {
        // Nếu không phải quản lý, VÀ plan đã duyệt, VÀ vẫn còn hàng
        if (!isManagerOrAdmin && dailyPlan.getStatus() && dailyPlan.getRemainingQuantity() > 0) {
            throw new AppException(ErrorCode.PLAN_ALREADY_APPROVED);
        }
    }

    private void validatePlanIsActive(DailyPlan dailyPlan) {
        if (dailyPlan.getStatus() == null || !dailyPlan.getStatus()) {
            // Lỗi nghiệp vụ: Không thể trừ kho một món chưa được duyệt bán
            throw new AppException(ErrorCode.PLAN_ALREADY_APPROVED); // (Cân nhắc đổi tên ErrorCode này)
        }
    }

    private void validateNewRemainingQuantity(int newRemaining) {
        if (newRemaining < 0) {
            throw new AppException(ErrorCode.NOT_ENOUGH_QUANTITY);
        }
    }

    // ===================================================================
    // HELPER: XÂY DỰNG & MAP (BUILDERS & MAPPERS)
    // ===================================================================

    /**
     * Chỉ làm 1 việc: Tạo entity DailyPlan mới từ DTO và Staff
     */
    private DailyPlan buildNewPlan(DailyPlanCreationRequest request, Staff planner) {
        DailyPlan dailyPlan = dailyPlanMapper.toDailyPlan(request);
        dailyPlan.setPlannerStaff(planner);
        dailyPlan.setRemainingQuantity(request.getPlannedQuantity());
        dailyPlan.setStatus(false);
        dailyPlan.setPlanDate(request.getPlanDate());
        return dailyPlan;
    }

    /**
     * Chỉ làm 1 việc: Map 1 entity sang 1 DTO (với tên đã có)
     */
    private DailyPlanResponse mapToResponse(DailyPlan dailyPlan, String itemName) {
        DailyPlanResponse response = dailyPlanMapper.toDailyPlanResponse(dailyPlan);
        response.setItemName(itemName);
        return response;
    }

    /**
     * Chỉ làm 1 việc: Map 1 List entity sang 1 List DTO (hiệu quả)
     */
    private List<DailyPlanResponse> mapToResponseList(List<DailyPlan> plans) {
        if (plans == null || plans.isEmpty()) {
            return Collections.emptyList();
        }

        // Tải Map tên item (tránh N+1)
        Map<String, String> itemNameMap = loadItemNameMap(plans);

        return plans.stream().map(plan -> {
            String key = plan.getItemType().name() + "_" + plan.getItemId();
            String itemName = itemNameMap.getOrDefault(key, "N/A");
            return mapToResponse(plan, itemName); // Tái sử dụng
        }).collect(Collectors.toList());
    }

    /**
     * Chỉ làm 1 việc: Lấy tên item (Dish/Topping) từ DB
     */
    private String getItemName(ItemType itemType, int itemId) {
        return switch (itemType) {
            case DISH -> dishRepository.findById(itemId).map(Dish::getDishName).orElse("N/A");
            case TOPPING -> toppingRepository.findById(itemId).map(Topping::getName).orElse("N/A");
        };
    }

    /**
     * Chỉ làm 1 việc: Tải 1 Map chứa tên của tất cả item trong list plan
     */
    private Map<String, String> loadItemNameMap(List<DailyPlan> plans) {
        // Tách ID
        List<Integer> dishIds = plans.stream()
                .filter(p -> p.getItemType() == ItemType.DISH)
                .map(DailyPlan::getItemId)
                .distinct().toList();

        List<Integer> toppingIds = plans.stream()
                .filter(p -> p.getItemType() == ItemType.TOPPING)
                .map(DailyPlan::getItemId)
                .distinct().toList();

        // Query 2 lần
        Map<String, String> dishNameMap = dishRepository.findAllById(dishIds).stream()
                .collect(Collectors.toMap(
                        dish -> "DISH_" + dish.getDishId(), // Key: "DISH_1"
                        Dish::getDishName
                ));

        Map<String, String> toppingNameMap = toppingRepository.findAllById(toppingIds).stream()
                .collect(Collectors.toMap(
                        topping -> "TOPPING_" + topping.getToppingId(), // Key: "TOPPING_5"
                        Topping::getName
                ));

        // Gộp 2 Map
        dishNameMap.putAll(toppingNameMap);
        return dishNameMap;
    }

    // ===================================================================
    // HELPER: LOGIC NGHIỆP VỤ (PROCESSORS)
    // ===================================================================

    /**
     * Chỉ làm 1 việc: Áp dụng logic update phức tạp
     */
    private void applyUpdates(DailyPlan dailyPlan, DailyPlanUpdateRequest request, Staff staff, boolean isManagerOrAdmin) {
        if (isManagerOrAdmin) {
            // Logic cho Manager/Admin
            if (request.getPlannedQuantity() != null) {
                dailyPlan.setPlannedQuantity(request.getPlannedQuantity());
            }
            if (request.getRemainingQuantity() != null) {
                dailyPlan.setRemainingQuantity(request.getRemainingQuantity());
            }
            if (request.getStatus() != null) {
                dailyPlan.setStatus(request.getStatus());
                // Gán hoặc xóa người duyệt
                dailyPlan.setApproverStaff(request.getStatus() ? staff : null);
            }
        } else {
            // Logic cho Chef
            if (request.getPlannedQuantity() != null) {
                dailyPlan.setPlannedQuantity(request.getPlannedQuantity());
                dailyPlan.setRemainingQuantity(request.getPlannedQuantity()); // Chef reset số lượng

                // Nếu plan đã duyệt và hết hàng, cho phép reset về "chưa duyệt"
                if (dailyPlan.getStatus() && dailyPlan.getRemainingQuantity() == 0) {
                    dailyPlan.setStatus(false);
                    dailyPlan.setApproverStaff(null);
                }
            }
            // Chef không được set status trực tiếp
        }
    }

    // (Helper này giữ nguyên từ code gốc, logic của nó đã là 1 việc)
    private DailyPlan processSinglePlanRequest(DailyPlanCreationRequest request, Staff planner, Integer targetStaffId) {
        // 3a. Kiểm tra tính nhất quán của StaffId (nếu có)
        if (request.getStaffId() != null && !request.getStaffId().equals(targetStaffId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 3b. Kiểm tra item tồn tại
        validateItemExists(request.getItemId(), request.getItemType());

        // 3c. Tìm plan đã tồn tại
        var existingPlanOpt = dailyPlanRepository.findByItemIdAndItemTypeAndPlanDate(
                request.getItemId(), request.getItemType(), request.getPlanDate()
        );

        if (existingPlanOpt.isPresent()) {
            // 3d. Nếu plan tồn tại -> Cập nhật
            return prepareUpdatedPlan(existingPlanOpt.get(), request, planner);
        } else {
            // 3e. Nếu plan không tồn tại -> Tạo mới
            return prepareNewPlan(request, planner);
        }
    }

    // (Helper này giữ nguyên từ code gốc)
    private DailyPlan prepareUpdatedPlan(DailyPlan existingPlan, DailyPlanCreationRequest request, Staff planner) {
        int oldPlanned = existingPlan.getPlannedQuantity();
        int newPlanned = request.getPlannedQuantity();

        if (newPlanned == oldPlanned) return null; // Không thay đổi

        int oldRemaining = existingPlan.getRemainingQuantity();
        int newRemaining = Math.max(0, oldRemaining + (newPlanned - oldPlanned));

        existingPlan.setPlannedQuantity(newPlanned);
        existingPlan.setRemainingQuantity(newRemaining);
        existingPlan.setStatus(false);
        existingPlan.setApproverStaff(null);
        existingPlan.setPlannerStaff(planner);

        return existingPlan;
    }

    // (Helper này giữ nguyên từ code gốc, nhưng TÁI SỬ DỤNG buildNewPlan)
    private DailyPlan prepareNewPlan(DailyPlanCreationRequest request, Staff planner) {
        // Tái sử dụng hàm build đã tạo
        return buildNewPlan(request, planner);
    }
}