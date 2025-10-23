package com.isp392.mapper;

import com.isp392.dto.request.OrdersCreationRequest;
import com.isp392.dto.request.OrdersUpdateRequest;
import com.isp392.dto.response.OrdersResponse;
import com.isp392.entity.Customer;
import com.isp392.entity.Orders;
import com.isp392.entity.TableEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {OrderDetailMapper.class})
public interface OrdersMapper {
    @Mapping(source = "request.orderDate", target = "orderDate")
    @Mapping(source = "customer", target = "customer") // Lấy từ tham số
    @Mapping(source = "table", target = "table")       // Lấy từ tham số
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "orderDetails", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "paid", constant = "false")
    Orders toOrders(OrdersCreationRequest request, Customer customer, TableEntity table);

    @Mapping(target = "customerId", source = "customer.customerId")
    @Mapping(target = "tableId", source = "table.tableId")
    OrdersResponse toOrdersResponse(Orders orders);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "table", ignore = true) // table xử lý riêng vì cần fetch từ DB
    void updateOrder(@MappingTarget Orders order, OrdersUpdateRequest request);

}
