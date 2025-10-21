package com.isp392.mapper;

import com.isp392.dto.request.OrdersCreationRequest;
import com.isp392.dto.request.OrdersUpdateRequest;
import com.isp392.dto.response.OrdersResponse;
import com.isp392.entity.Orders;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {OrderDetailMapper.class})
public interface OrdersMapper {
    Orders toOrders(OrdersCreationRequest request);

    @Mapping(target = "customerId", source = "customer.customerId")
    @Mapping(target = "tableId", source = "table.tableId")
    OrdersResponse toOrdersResponse(Orders orders);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "table", ignore = true) // table xử lý riêng vì cần fetch từ DB
    void updateOrder(@MappingTarget Orders order, OrdersUpdateRequest request);

}
