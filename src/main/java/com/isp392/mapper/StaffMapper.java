package com.isp392.mapper;

import com.isp392.dto.request.StaffCreationRequest;
import com.isp392.dto.request.StaffUpdateRequest;
import com.isp392.dto.response.StaffResponse;
import com.isp392.entity.Staff;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = AccountMapper.class)
public interface StaffMapper {

    Staff toStaff(StaffCreationRequest request);

    @Mapping(target = ".", source = "account")
    StaffResponse toStaffResponse(Staff staff);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStaff(@MappingTarget Staff staff, StaffUpdateRequest request);

    List<StaffResponse> toStaffResponseList(List<Staff> staffList);
}


