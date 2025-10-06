package com.isp392.service;

import com.isp392.dto.request.StaffCreationRequest;
import com.isp392.dto.request.StaffUpdateRequest;
import com.isp392.dto.response.StaffResponse;
import com.isp392.entity.Staff;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.StaffMapper;
import com.isp392.repository.StaffRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaffService {

    StaffRepository staffRepository;
    StaffMapper staffMapper;

    public Staff createStaff(StaffCreationRequest request) {

        if (staffRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.STAFF_EXISTED);
        }
        Staff staff = staffMapper.toStaff(request);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        staff.setPassword(passwordEncoder.encode(request.getPassword()));

        return staffRepository.save(staff);
    }

    public List<Staff> getStaffs() {
        return staffRepository.findAll();
    }

    public StaffResponse getStaff(long id, String usernameFromJwt) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        if (!staff.getUsername().equals(usernameFromJwt)) {
            throw new AppException(ErrorCode.STAFF_ACCESS_FORBIDDEN);
        }
        return staffMapper.toStaffResponse(staff);
    }

    public StaffResponse updateStaff(long staffId, StaffUpdateRequest request) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        staffMapper.updateUser(staff, request);


        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        staff.setPassword(passwordEncoder.encode(request.getPassword()));


        return staffMapper.toStaffResponse(staffRepository.save(staff));
    }

    public void deleteStaff(long staffId) {
        staffRepository.deleteById(staffId);
    }
}
