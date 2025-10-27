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

//    @Transactional
//    public List<DailyPlanResponse> createDailyPlansBatch(List<DailyPlanCreationRequest> requests, Authentication authentication) {
//        if (requests == null || requests.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        String username = authentication.getName();
//        boolean isManager = hasAuthority(authentication, Role.MANAGER.name());
//
//        DailyPlanCreationRequest firstRequest = requests.get(0);
//        Staff planner;
//
//        if (isManager && firstRequest.getStaffId() != null) {
//            planner = staffRepository.findByIdWithAccount(firstRequest.getStaffId())
//                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
//        } else {
//            planner = staffRepository.findByUsernameWithAccount(username)
//                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
//        }
//
//        final Integer targetStaffId = planner.getStaffId();
//
//        List<DailyPlan> plansToSave = requests.stream().map(request -> {
//            if (request.getStaffId() != null && !request.getStaffId().equals(targetStaffId)) {
//                throw new AppException(ErrorCode.INVALID_REQUEST);
//            }
//
//            dailyPlanRepository.findByItemIdAndItemTypeAndPlanDate(
//                    request.getItemId(), request.getItemType(), request.getPlanDate()
//            ).ifPresent(plan -> {
//                throw new AppException(ErrorCode.PLAN_ALREADY_EXISTS_BATCH);
//            });
//
//            validateItemExists(request.getItemId(), request.getItemType());
//
//            DailyPlan dailyPlan = dailyPlanMapper.toDailyPlan(request);
//            dailyPlan.setPlannerStaff(planner);
//            dailyPlan.setRemainingQuantity(request.getPlannedQuantity());
//            dailyPlan.setStatus(false);
//            dailyPlan.setPlanDate(request.getPlanDate());
//            return dailyPlan;
//        }).collect(Collectors.toList());
//
//        List<DailyPlan> savedPlans = dailyPlanRepository.saveAll(plansToSave);
//        return savedPlans.stream()
//                .map(this::mapToResponseWithItemName)
//                .collect(Collectors.toList());
//    }

    @Transactional
    public List<DailyPlanResponse> createDailyPlansBatch(List<DailyPlanCreationRequest> requests, Authentication authentication) {
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Xác định người lập kế hoạch cho cả lô
        Staff planner = getPlannerForBatch(requests.get(0), authentication);
        final Integer targetStaffId = planner.getStaffId();

        List<DailyPlan> plansToSave = new ArrayList<>();

        // 2. Xử lý từng yêu cầu
        for (DailyPlanCreationRequest request : requests) {
            // 3. Xử lý logic cho một plan (bao gồm validation)
            // Phương thức này sẽ trả về null nếu plan không cần được save (ví dụ: không thay đổi)
            DailyPlan planToSave = processSinglePlanRequest(request, planner, targetStaffId);

            if (planToSave != null) {
                plansToSave.add(planToSave);
            }
        }

        // 4. Lưu tất cả thay đổi
        List<DailyPlan> savedPlans = dailyPlanRepository.saveAll(plansToSave);

        // 5. Chuyển đổi sang DTO
        return savedPlans.stream()
                .map(this::mapToResponseWithItemName)
                .collect(Collectors.toList());
    }

    public List<DailyPlanResponse> getAllDailyPlans() {
        List<DailyPlan> plans = dailyPlanRepository.findAll();
        if (plans.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> dishIds = plans.stream().filter(p -> p.getItemType() == ItemType.DISH).map(DailyPlan::getItemId).distinct().toList();
        List<Integer> toppingIds = plans.stream().filter(p -> p.getItemType() == ItemType.TOPPING).map(DailyPlan::getItemId).distinct().toList();

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

        DailyPlan dailyPlan = dailyPlanRepository.findByIdWithPlannerDetails(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        if (!isManagerOrAdmin && !dailyPlan.getPlannerStaff().getAccount().getUsername().equals(username)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        if (!isManagerOrAdmin && dailyPlan.getStatus()) {
            // ✅ Nếu đã duyệt mà vẫn còn hàng thì chặn
            if (dailyPlan.getRemainingQuantity() > 0) {
                throw new AppException(ErrorCode.PLAN_ALREADY_APPROVED);
            }
            // ✅ Còn nếu đã hết hàng (remaining = 0) thì cho phép Chef gửi lại
        }


        // --- BẮT ĐẦU LOGIC SỬA LẠI ---
        if (isManagerOrAdmin) {
            if (request.getPlannedQuantity() != null) {
                dailyPlan.setPlannedQuantity(request.getPlannedQuantity());
            }
            if (request.getRemainingQuantity() != null) {
                dailyPlan.setRemainingQuantity(request.getRemainingQuantity());
            }
            // Logic xử lý status cho Manager/Admin
            if (request.getStatus() != null) {
                if (request.getStatus()) { // Nếu đang duyệt (true)
                    Staff approver = staffRepository.findByUsernameWithAccount(username)
                            .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
                    dailyPlan.setApproverStaff(approver);
                } else { // Nếu đang hủy duyệt (false)
                    dailyPlan.setApproverStaff(null);
                }
                dailyPlan.setStatus(request.getStatus());
            }
        } else { // Là Chef
            if (request.getPlannedQuantity() != null) {
                dailyPlan.setPlannedQuantity(request.getPlannedQuantity());
                dailyPlan.setRemainingQuantity(request.getPlannedQuantity());

                // ✅ Cho phép Chef gửi lại nếu plan đã duyệt nhưng đã hết hàng
                if (dailyPlan.getStatus() && dailyPlan.getRemainingQuantity() == 0) {
                    dailyPlan.setStatus(false);
                }
            }
            // Chef không thể thay đổi status, nên không có logic ở đây
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

    @Transactional
    public List<DailyPlanResponse> approveDailyPlansBatch(DailyPlanBatchApproveRequest request, Authentication authentication) {
        String username = authentication.getName();

        // 1. Lấy thông tin người duyệt (Manager/Admin)
        Staff approver = staffRepository.findByUsernameWithAccount(username)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        // 2. Lấy tất cả các kế hoạch cần duyệt
        List<DailyPlan> plansToApprove = dailyPlanRepository.findAllById(request.getPlanIds());

        // 3. Lặp qua và cập nhật từng kế hoạch
        for (DailyPlan plan : plansToApprove) {
            // Chỉ duyệt nếu kế hoạch đang ở trạng thái "chưa duyệt"
            if (!plan.getStatus()) {
                plan.setStatus(true);
                plan.setApproverStaff(approver);
            }
            // (Bạn có thể thêm logic báo lỗi nếu plan không tìm thấy hoặc đã được duyệt)
        }

        // 4. Lưu tất cả thay đổi xuống DB
        List<DailyPlan> savedPlans = dailyPlanRepository.saveAll(plansToApprove);

        // 5. Trả về danh sách các kế hoạch đã được cập nhật
        return savedPlans.stream()
                .map(this::mapToResponseWithItemName)
                .collect(Collectors.toList());
    }


    // HELPER METHODS

    /**
     * (HELPER) Xác định Staff lập kế hoạch dựa trên request đầu tiên của batch.
     */
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

    /**
     * (HELPER) Xử lý logic tạo mới hoặc cập nhật cho một yêu cầu DailyPlan.
     * Trả về entity DailyPlan đã sẵn sàng để save, hoặc null nếu không cần save.
     */
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

    /**
     * (HELPER) Cập nhật một DailyPlan đã tồn tại.
     * Trả về null nếu không có gì thay đổi (plannedQuantity bằng nhau).
     */
    private DailyPlan prepareUpdatedPlan(DailyPlan existingPlan, DailyPlanCreationRequest request, Staff planner) {
        int oldPlanned = existingPlan.getPlannedQuantity();
        int newPlanned = request.getPlannedQuantity();

        // Nếu planned không thay đổi thì bỏ qua (không cần save)
        if (newPlanned == oldPlanned) {
            return null;
        }

        int oldRemaining = existingPlan.getRemainingQuantity();

        // Công thức tính: remaining_new = remaining_old + (newPlanned - oldPlanned)
        int newRemaining = oldRemaining + (newPlanned - oldPlanned);
        if (newRemaining < 0) {
            newRemaining = 0; // Đảm bảo số lượng còn lại không bị âm
        }

        // Cập nhật lại plan cũ
        existingPlan.setPlannedQuantity(newPlanned);
        existingPlan.setRemainingQuantity(newRemaining);
        existingPlan.setStatus(false);           // reset chờ duyệt lại
        existingPlan.setApproverStaff(null);     // xóa người duyệt cũ
        existingPlan.setPlannerStaff(planner);   // cập nhật người lập kế hoạch (có thể là người khác)

        return existingPlan;
    }

    /**
     * (HELPER) Tạo một entity DailyPlan mới (chưa save).
     */
    private DailyPlan prepareNewPlan(DailyPlanCreationRequest request, Staff planner) {
        DailyPlan newPlan = dailyPlanMapper.toDailyPlan(request);
        newPlan.setPlannerStaff(planner);
        newPlan.setRemainingQuantity(request.getPlannedQuantity()); // Số lượng còn lại = số lượng kế hoạch
        newPlan.setStatus(false); // Chờ duyệt
        newPlan.setPlanDate(request.getPlanDate());
        return newPlan;
    }

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
