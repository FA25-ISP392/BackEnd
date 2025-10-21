package com.isp392.service;

import com.isp392.dto.request.OrderDetailCreationRequest;
import com.isp392.dto.request.OrderDetailUpdateRequest;
import com.isp392.dto.response.OrderDetailResponse;
import com.isp392.dto.response.OrderToppingResponse;
import com.isp392.entity.*;
import com.isp392.enums.ItemType;
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

import java.time.LocalDate;
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
    DailyPlanRepository dailyPlanRepository;

    @Transactional
    public OrderDetailResponse createOrderDetail(OrderDetailCreationRequest request) {
        Orders order = getOrderById(request.getOrderId());
        Dish dish = getDishById(request.getDishId());

        // 1️⃣ Trừ số lượng món trong daily plan
        decrementDishDailyPlan(dish, 1);

        // 2️⃣ Tạo order detail
        OrderDetail orderDetail = buildOrderDetail(order, dish, request.getNote());
        double totalPrice = dish.getPrice();

        // 3️⃣ Xử lý topping nếu có
        List<OrderTopping> orderToppings = new ArrayList<>();
        if (request.getToppings() != null) {
            for (OrderDetailCreationRequest.ToppingSelection t : request.getToppings()) {
                Topping topping = getToppingById(t.getToppingId());

                // Trừ số lượng topping
                decrementToppingDailyPlan(topping, t.getQuantity());

                double toppingPrice = topping.getPrice() * t.getQuantity();
                totalPrice += toppingPrice;

                OrderTopping orderTopping = buildOrderTopping(orderDetail, topping, t.getQuantity(), toppingPrice);
                orderToppings.add(orderTopping);
            }
        }

        orderDetail.setOrderToppings(orderToppings);
        orderDetail.setTotalPrice(totalPrice);

        // Lưu order detail và topping
        orderDetailRepository.save(orderDetail);

        List<OrderToppingResponse> toppingResponses = orderDetailMapper.toToppingResponseList(orderToppings);
        return orderDetailMapper.toResponse(orderDetail, toppingResponses);
    }

// ================== Helper methods ==================

    private Orders getOrderById(Integer orderId) {
        return ordersRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    private Dish getDishById(Integer dishId) {
        return dishRepository.findById(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));
    }

    private Topping getToppingById(Integer toppingId) {
        return toppingRepository.findById(toppingId)
                .orElseThrow(() -> new AppException(ErrorCode.TOPPING_NOT_FOUND));
    }

    private void decrementDishDailyPlan(Dish dish, int quantity) {
        DailyPlan dailyPlan = dailyPlanRepository.findByItemIdAndItemTypeAndPlanDate(dish.getDishId(), ItemType.DISH, LocalDate.now())
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));

        if (dailyPlan.getRemainingQuantity() < quantity) {
            throw new AppException(ErrorCode.NOT_ENOUGH_QUANTITY);
        }

        dailyPlan.setRemainingQuantity(dailyPlan.getRemainingQuantity() - quantity);
        dailyPlanRepository.save(dailyPlan);
    }

    private void decrementToppingDailyPlan(Topping topping, int quantity) {
        DailyPlan dailyPlan = dailyPlanRepository.findByItemIdAndItemTypeAndPlanDate(topping.getToppingId(),ItemType.TOPPING, LocalDate.now())
                .orElseThrow(() -> new AppException(ErrorCode.TOPPING_NOT_FOUND));

        if (dailyPlan.getRemainingQuantity() < quantity) {
            throw new AppException(ErrorCode.NOT_ENOUGH_QUANTITY);
        }

        dailyPlan.setRemainingQuantity(dailyPlan.getRemainingQuantity() - quantity);
        dailyPlanRepository.save(dailyPlan);
    }

    private OrderDetail buildOrderDetail(Orders order, Dish dish, String note) {
        return OrderDetail.builder()
                .order(order)
                .dish(dish)
                .status(OrderDetailStatus.PENDING)
                .note(note)
                .build();
    }

    private OrderTopping buildOrderTopping(OrderDetail orderDetail, Topping topping, int quantity, double toppingPrice) {
        return OrderTopping.builder()
                .id(new OrderToppingId(null, topping.getToppingId()))
                .orderDetail(orderDetail)
                .topping(topping)
                .quantity(quantity)
                .toppingPrice(toppingPrice)
                .build();
    }

// =========================

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

    @Transactional(readOnly = true)
    public List<OrderDetailResponse> getOrderDetailsByStatus(OrderDetailStatus status) {
        List<OrderDetail> details = orderDetailRepository.findByStatus(status);
        return details.stream()
                .map(orderDetailMapper::toOrderDetailResponse)
                .toList();
    }

    @Transactional
    public OrderDetailResponse updateOrderDetail(OrderDetailUpdateRequest request) {
        OrderDetail detail = orderDetailRepository.findById(request.getOrderDetailId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_DETAIL_NOT_FOUND));

        orderDetailMapper.updateOrderDetail(detail, request);

        // Cập nhật topping nếu FE gửi
        if (request.getToppings() != null) {
            detail.getOrderToppings().clear();
            List<OrderTopping> newToppings = request.getToppings().stream().map(t -> {
                OrderTopping ot = new OrderTopping();
                ot.setOrderDetail(detail);
                ot.setTopping(toppingRepository.findById(t.getToppingId())
                        .orElseThrow(() -> new AppException(ErrorCode.TOPPING_NOT_FOUND)));
                ot.setQuantity(t.getQuantity());
                return ot;
            }).toList();
            detail.getOrderToppings().addAll(newToppings);
        }

        double toppingsPrice = detail.getOrderToppings().stream()
                .mapToDouble(ot -> ot.getQuantity() * ot.getTopping().getPrice())
                .sum();
        detail.setTotalPrice(detail.getDish().getPrice() + toppingsPrice);

        return orderDetailMapper.toOrderDetailResponse(detail);
    }

}

