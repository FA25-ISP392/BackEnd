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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final AccountRepository accountRepository;


    @Transactional
    public StaffResponse createStaff(@Valid StaffCreationRequest request) {
        Account account = accountService.createAccount(request);
        Staff staff = staffMapper.toStaff(request);
        staff.setAccount(account);
        staff = staffRepository.save(staff);
        return staffMapper.toStaffResponse(staff);
    }

    public List<StaffResponse> getStaff() {
        List<Staff> staffList = staffRepository.findAll();
        return staffMapper.toStaffResponseList(staffList);
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
        staffMapper.updateStaff(staff, request);

        Account account = staff.getAccount();
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            account.setPassword(new BCryptPasswordEncoder().encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            account.setRole(request.getRole());
        }

        accountRepository.save(account);
        staff = staffRepository.save(staff);

        return staffMapper.toStaffResponse(staff);
    }

    @Transactional
    public void deleteStaff(Integer staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        Account account = staff.getAccount();
        staffRepository.delete(staff);
        accountRepository.delete(account);
    }
}