package com.isp392.mapper;

import com.isp392.dto.request.OrderDetailUpdateRequest;
import com.isp392.dto.response.OrderDetailResponse;
import com.isp392.dto.response.OrderToppingResponse;
import com.isp392.entity.OrderDetail;
import com.isp392.entity.OrderTopping;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderDetailMapper {

    OrderDetailMapper INSTANCE = Mappers.getMapper(OrderDetailMapper.class);

    @Mapping(source = "dish.dishId", target = "dishId")
    @Mapping(source = "dish.dishName", target = "dishName")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "orderToppings", target = "toppings")
    OrderDetailResponse toOrderDetailResponse(OrderDetail orderDetail);

    @Mapping(source = "orderDetail.dish.dishId", target = "dishId")
    @Mapping(source = "orderDetail.dish.dishName", target = "dishName")
    @Mapping(source = "orderDetail.status", target = "status")
    OrderDetailResponse toResponse(OrderDetail orderDetail, List<OrderToppingResponse> toppings);


    @Mapping(source = "topping.toppingId", target = "toppingId")
    @Mapping(source = "topping.name", target = "toppingName")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "toppingPrice", target = "toppingPrice")
    OrderToppingResponse toToppingResponse(OrderTopping orderTopping);

    List<OrderToppingResponse> toToppingResponseList(List<OrderTopping> orderToppings);

    void updateOrderDetail(@Valid OrderDetailUpdateRequest request, @MappingTarget OrderDetail orderDetail);

//    Boolean updateOrderDetail(@Valid OrderDetailUpdateRequest request, OrderDetail orderDetail);

}
