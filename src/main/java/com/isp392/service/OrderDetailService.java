package com.isp392.service;

import com.isp392.dto.request.OrderDetailCreationRequest;
import com.isp392.dto.request.OrderDetailUpdateRequest;
import com.isp392.dto.response.OrderDetailResponse;
import com.isp392.dto.response.OrderToppingResponse;
import com.isp392.entity.*;
import com.isp392.enums.OrderDetailStatus;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.OrderDetailMapper;
import com.isp392.mapper.OrderToppingMapper;
import com.isp392.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class OrderDetailService {

    OrderDetailRepository orderDetailRepository;
    OrdersRepository ordersRepository;
    ToppingRepository toppingRepository;
    DishRepository dishRepository;
    OrderDetailMapper orderDetailMapper;

    @Transactional
    public OrderDetailResponse createOrderDetail(OrderDetailCreationRequest request) {
        Orders order = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        Dish dish = dishRepository.findById(request.getDishId())
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));

        double totalPrice = dish.getPrice();

        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .dish(dish)
                .status(OrderDetailStatus.PENDING)
                .note(request.getNote())
                .build();

        List<OrderTopping> orderToppings = new ArrayList<>();

        if (request.getToppings() != null) {
            for (OrderDetailCreationRequest.ToppingSelection t : request.getToppings()) {
                Topping topping = toppingRepository.findById(t.getToppingId())
                        .orElseThrow(() -> new AppException(ErrorCode.TOPPING_NOT_FOUND));

                double toppingPrice = topping.getPrice() * t.getQuantity();
                totalPrice += toppingPrice;

                OrderTopping orderTopping = OrderTopping.builder()
                        .id(new OrderToppingId(null, t.getToppingId())) // orderDetailId sẽ được Hibernate set tự động
                        .orderDetail(orderDetail)
                        .topping(topping)
                        .quantity(t.getQuantity())
                        .toppingPrice(toppingPrice)
                        .build();

                orderToppings.add(orderTopping);
            }
        }

        orderDetail.setOrderToppings(orderToppings);
        orderDetail.setTotalPrice(totalPrice);

        // Save một lần, cascade sẽ tự insert orderDetail trước rồi insert orderTopping
        orderDetailRepository.save(orderDetail);

        List<OrderToppingResponse> toppingResponses = orderDetailMapper.toToppingResponseList(orderToppings);
        return orderDetailMapper.toResponse(orderDetail, toppingResponses);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(int orderDetailId) {
        OrderDetail orderDetail = orderDetailRepository
                .findByIdWithToppings(orderDetailId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_DETAIL_NOT_FOUND));

        List<OrderToppingResponse> toppings = orderDetail.getOrderToppings().stream()
                .map(ot -> OrderToppingResponse.builder()
                        .toppingId(ot.getTopping().getToppingId())
                        .toppingName(ot.getTopping().getName())
                        .quantity(ot.getQuantity())
                        .toppingPrice(ot.getToppingPrice())
                        .build())
                .toList();

        return orderDetailMapper.toResponse(orderDetail, toppings);
    }

}

