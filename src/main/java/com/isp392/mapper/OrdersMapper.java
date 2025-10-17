package com.isp392.mapper;

import com.isp392.dto.request.OrdersCreationRequest;
import com.isp392.dto.response.OrdersResponse;
import com.isp392.entity.Orders;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderDetailMapper.class})
public interface OrdersMapper {
    Orders toOrders(OrdersCreationRequest request);

    @Mapping(target = "customerId", source = "customer.customerId")
    @Mapping(target = "tableId", source = "table.tableId")
    OrdersResponse toOrdersResponse(Orders orders);

}
