package com.isp392.mapper;

import com.isp392.dto.request.OrderToppingCreationRequest;
import com.isp392.dto.request.OrderToppingUpdateRequest;
import com.isp392.dto.response.OrderToppingResponse;
import com.isp392.entity.OrderTopping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface OrderToppingMapper {
    OrderTopping toOrderTopping(OrderToppingCreationRequest request);

    OrderToppingResponse toOrderToppingResponse(OrderTopping orderTopping);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOrderToping(OrderToppingUpdateRequest request, @MappingTarget OrderTopping updatedOrderTopping);
}
