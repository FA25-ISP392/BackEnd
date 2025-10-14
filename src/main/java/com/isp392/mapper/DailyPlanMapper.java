package com.isp392.mapper;

import com.isp392.dto.request.DailyPlanCreationRequest;
import com.isp392.dto.request.DailyPlanUpdateRequest;
import com.isp392.dto.response.DailyPlanResponse;
import com.isp392.entity.DailyPlan;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DailyPlanMapper {

    @Mapping(target = "plannerStaff", ignore = true)
    @Mapping(target = "approverStaff", ignore = true)
    DailyPlan toDailyPlan(DailyPlanCreationRequest request);

    @Mapping(source = "plannerStaff.staffId", target = "staffId")
    @Mapping(source = "plannerStaff.account.fullName", target = "staffName")
    @Mapping(source = "approverStaff.staffId", target = "approvedByStaffId", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    @Mapping(source = "approverStaff.account.fullName", target = "approverName", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    @Mapping(target = "itemName", ignore = true)
    DailyPlanResponse toDailyPlanResponse(DailyPlan dailyPlan);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "plannerStaff", ignore = true)
    @Mapping(target = "approverStaff", ignore = true)
    void updateDailyPlan(@MappingTarget DailyPlan dailyPlan, DailyPlanUpdateRequest request);
}