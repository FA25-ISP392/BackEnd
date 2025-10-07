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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaffService {

    StaffRepository staffRepository;
    AccountRepository accountRepository;
    StaffMapper staffMapper;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    /**
     * ✅ Tạo mới Staff (tự động tạo Account trước)
     */
    @Transactional
    public Staff createStaff(StaffCreationRequest request) {
        // Kiểm tra username đã tồn tại trong Account chưa
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // 🔹 Tạo Account
        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole(request.getRole());
        accountRepository.save(account);

        // 🔹 Tạo Staff và gán Account
        Staff staff = staffMapper.toStaff(request);
        staff.setAccount(account);

        return staffRepository.save(staff);
    }

    /**
     * ✅ Lấy danh sách toàn bộ Staff
     */
    public List<Staff> getStaffs() {
        return staffRepository.findAll();
    }

    /**
     * ✅ Lấy 1 Staff theo id, chỉ cho phép chính họ hoặc admin truy cập
     */
    public StaffResponse getStaff(long id, String usernameFromJwt) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        if (!staff.getAccount().getUsername().equals(usernameFromJwt)) {
            throw new AppException(ErrorCode.STAFF_ACCESS_FORBIDDEN);
        }

        return staffMapper.toStaffResponse(staff);
    }

    /**
     * ✅ Cập nhật thông tin Staff (nếu có password thì update trong Account)
     */
    @Transactional
    public StaffResponse updateStaff(long staffId, StaffUpdateRequest request) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        // 🔹 Cập nhật thông tin Staff
        staffMapper.updateUser(staff, request);

        // 🔹 Cập nhật Account nếu có thay đổi password hoặc role
        Account account = staff.getAccount();
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            account.setRole(request.getRole());
        }

        accountRepository.save(account);
        staffRepository.save(staff);

        return staffMapper.toStaffResponse(staff);
    }

    /**
     * ✅ Xoá Staff (và cả Account đi kèm)
     */
    @Transactional
    public void deleteStaff(long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        // Xoá staff và account liên kết
        Account account = staff.getAccount();
        staffRepository.delete(staff);
        accountRepository.delete(account);
    }
}
