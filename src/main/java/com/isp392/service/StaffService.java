package com.isp392.service;

import com.isp392.dto.request.StaffCreationRequest;
import com.isp392.dto.request.StaffUpdateRequest;
import com.isp392.dto.response.StaffResponse;
import com.isp392.entity.Account;
import com.isp392.entity.Staff;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.StaffMapper;
import com.isp392.repository.AccountRepository;
import com.isp392.repository.StaffRepository;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaffService {

    StaffRepository staffRepository;
    StaffMapper staffMapper;
    AccountService accountService;



    @Transactional
    public StaffResponse createStaff(@Valid StaffCreationRequest request) {
        Account account = accountService.createAccount(request);
        Staff staff = staffMapper.toStaff(request);
        staff.setAccount(account);
        staff = staffRepository.save(staff);
        return staffMapper.toStaffResponse(staff);
    }

    public List<StaffResponse> getStaff(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("staffId").ascending());
        Page<Staff> staffPage = staffRepository.findAll(pageable);
        return staffPage.stream()
                .map(staffMapper::toStaffResponse)
                .toList();
    }

    public StaffResponse getStaff(Integer staffId, String usernameJwt) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        if (!staff.getAccount().getUsername().equals(usernameJwt)) {
            throw new AppException(ErrorCode.STAFF_ACCESS_FORBIDDEN);
        }
        return staffMapper.toStaffResponse(staff);
    }

    @Transactional
    public StaffResponse updateStaff(Integer staffId, StaffUpdateRequest request) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        // Update các field của staff (trừ Account)
        staffMapper.updateStaff(staff, request);

        // Update Account riêng
        Account account = staff.getAccount();
        accountService.updateAccount(account, request);

        return staffMapper.toStaffResponse(staff);
    }

    @Transactional
    public void deleteStaff(Integer staffIdToDelete) {
        // Lấy username của người đăng nhập hiện tại
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Lấy staff hiện tại (người đăng nhập)
        Staff currentStaff = staffRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.LOGIN_REQUIRED));

        // Kiểm tra không cho staff tự xoá bản thân
        if (currentStaff.getStaffId().equals(staffIdToDelete)) {
            throw new AppException(ErrorCode.CANNOT_DELETE_SELF);
        }

        // Xoá staff theo staffId được truyền vào
        staffRepository.deleteById(staffIdToDelete);
    }
}