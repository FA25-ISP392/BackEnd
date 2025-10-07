package com.isp392.mapper;

import com.isp392.dto.request.StaffCreationRequest;
import com.isp392.dto.request.StaffUpdateRequest;
import com.isp392.dto.response.StaffResponse;
import com.isp392.entity.Staff;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StaffMapper {
    Staff toStaff(StaffCreationRequest request);

    @Mapping(source = "account.role", target = "role")
    StaffResponse toStaffResponse(Staff staff);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget Staff staff, StaffUpdateRequest request);
}
