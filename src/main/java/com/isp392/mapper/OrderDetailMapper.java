package com.isp392.mapper;

import com.isp392.dto.request.OrderDetailUpdateRequest;
import com.isp392.dto.response.OrderDetailResponse;
import com.isp392.dto.response.OrderToppingResponse;
import com.isp392.entity.OrderDetail;
import com.isp392.entity.OrderTopping;
import jakarta.validation.Valid;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderDetailMapper {

    OrderDetailMapper INSTANCE = Mappers.getMapper(OrderDetailMapper.class);

    @Mapping(source = "dish.dishId", target = "dishId")
    @Mapping(source = "dish.dishName", target = "dishName")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "orderToppings", target = "toppings")
    @Mapping(source = "order.orderDate", target = "orderDate")
    @Mapping(source = "order.table.tableName", target = "tableName")
    @Mapping(source = "order.table.tableId", target = "tableId")
    @Mapping(source = "order.orderId", target = "orderId")
    @Mapping(source = "servingStaff.staffId", target = "staffId", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    @Mapping(source = "servingStaff.account.fullName", target = "staffName", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    OrderDetailResponse toOrderDetailResponse(OrderDetail orderDetail);

    @Mapping(source = "orderDetail.dish.dishId", target = "dishId")
    @Mapping(source = "orderDetail.dish.dishName", target = "dishName")
    @Mapping(source = "orderDetail.status", target = "status")
    @Mapping(source = "orderDetail.order.orderDate", target = "orderDate")
    @Mapping(source = "orderDetail.order.table.tableName", target = "tableName")
    @Mapping(source = "orderDetail.order.orderId", target = "orderId")
    @Mapping(source = "orderDetail.order.table.tableId", target = "tableId")
    @Mapping(source = "orderDetail.servingStaff.staffId", target = "staffId", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    @Mapping(source = "orderDetail.servingStaff.account.fullName", target = "staffName", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    OrderDetailResponse toResponse(OrderDetail orderDetail, List<OrderToppingResponse> toppings);


    @Mapping(source = "topping.toppingId", target = "toppingId")
    @Mapping(source = "topping.name", target = "toppingName")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "toppingPrice", target = "toppingPrice")
    OrderToppingResponse toToppingResponse(OrderTopping orderTopping);

    List<OrderToppingResponse> toToppingResponseList(List<OrderTopping> orderToppings);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "dish", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "orderToppings", ignore = true)
    @Mapping(target = "servingStaff", ignore = true)
    void updateOrderDetail(@MappingTarget OrderDetail entity, OrderDetailUpdateRequest request);

}
