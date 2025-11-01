package com.isp392.service;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
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
import com.isp392.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    OrderToppingRepository orderToppingRepository;

    // ⭐ THÊM DÒNG NÀY ĐỂ SỬA LỖI StaleObjectStateException
    EntityManager entityManager;
    TableRepository tableRepository;

    @Transactional
    public OrderDetailResponse createOrderDetail(OrderDetailCreationRequest request) {
        Orders order = getOrderById(request.getOrderId());
        Dish dish = getDishById(request.getDishId());

        TableEntity table = order.getTable();
        if (table == null || !table.isAvailable()) {
            throw new RuntimeException("Table is not available for ordering.");
        }
        table.setServing(true);
        tableRepository.save(table);
        // 1️⃣ Trừ số lượng món trong daily plan
        decrementDishDailyPlan(dish, 1);

        // 2️⃣ Tạo order detail
        OrderDetail orderDetail = buildOrderDetail(order, dish, request.getNote());

        // 3️⃣ Xử lý topping (Đã cập nhật hàm này để chống trùng lặp)
        List<OrderTopping> orderToppings = processToppingsForCreate(orderDetail, request.getToppings());
        orderDetail.setOrderToppings(orderToppings);

        // 4️⃣ Tính tổng tiền
        recalculateTotalPrice(orderDetail); // Dùng hàm helper cho nhất quán

        // 5️⃣ Lưu order detail và topping
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

    @Transactional(readOnly = true)
    public List<OrderDetailResponse> getOrderDetailsByStatus(OrderDetailStatus status) {
        List<OrderDetail> details = orderDetailRepository.findByStatusWithOrder(status);
        return details.stream()
                .map(orderDetailMapper::toOrderDetailResponse)
                .toList();
    }

    @Transactional
    public OrderDetailResponse updateOrderDetail(OrderDetailUpdateRequest request) {
        // 1. Lấy entity VÀ topping CŨ
        OrderDetail detail = orderDetailRepository
                .findByIdWithToppings(request.getOrderDetailId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_DETAIL_NOT_FOUND));

//        // 2. KIỂM TRA STATUS PENDING
//        if (detail.getStatus() != OrderDetailStatus.PENDING) {
//            throw new AppException(ErrorCode.ORDER_DETAIL_CANNOT_BE_UPDATED);
//        }

        // 3. Map các trường đơn giản (note...)
        orderDetailMapper.updateOrderDetail(detail, request);

        // 4. Xử lý topping và cập nhật kho (nếu có)
        if (request.getToppings() != null) {
            updateToppingsAndInventory(detail, request.getToppings());
        }

        // 5. Tính lại tổng tiền
        recalculateTotalPrice(detail);

        // 6. Trả về response
        return orderDetailMapper.toOrderDetailResponse(detail);
    }

    @Transactional
    public void deleteOrderDetail(Integer orderDetailId) {
        // 1. Lấy OrderDetail cùng với các topping
        OrderDetail orderDetail = orderDetailRepository
                .findByIdWithToppings(orderDetailId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_DETAIL_NOT_FOUND));

        // 2. Kiểm tra trạng thái
        if (orderDetail.getStatus() != OrderDetailStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_DETAIL_CANNOT_BE_CANCELLED);
        }

        // 3. Hoàn lại số lượng cho Món ăn (Dish)
        incrementDishDailyPlan(orderDetail.getDish(), 1);

        // 4. Hoàn lại số lượng cho từng Topping
        for (OrderTopping ot : orderDetail.getOrderToppings()) {
            incrementToppingDailyPlan(ot.getTopping(), ot.getQuantity());
        }

        // 5. Xoá OrderDetail
        orderDetailRepository.delete(orderDetail);
    }


// ================== Helper methods ==================

    // ====== Helpers: Get Entity ======
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

    // ====== Helpers: Build Entity ======
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
                .id(new OrderToppingId()) // Fix lỗi NullPointerException
                .orderDetail(orderDetail) // @MapsId sẽ tự lấy ID từ đây
                .topping(topping)         // @MapsId sẽ tự lấy ID từ đây
                .quantity(quantity)
                .toppingPrice(toppingPrice)
                .build();
    }

    // ====== Helpers: Logic nghiệp vụ ======

    /**
     * ⭐ SỬA LỖI (BUG TIỀM ẨN): Gộp các topping trùng lặp khi TẠO MỚI
     */
    private List<OrderTopping> processToppingsForCreate(OrderDetail orderDetail, List<OrderDetailCreationRequest.ToppingSelection> toppingRequests) {
        if (toppingRequests == null || toppingRequests.isEmpty()) {
            return new ArrayList<>();
        }

        // Gộp các toppingId trùng lặp và cộng dồn số lượng
        Map<Integer, Integer> mergedToppings = toppingRequests.stream()
                .collect(Collectors.groupingBy(
                        OrderDetailCreationRequest.ToppingSelection::getToppingId,
                        Collectors.summingInt(OrderDetailCreationRequest.ToppingSelection::getQuantity)
                ));

        // Tạo OrderTopping từ danh sách đã gộp
        List<OrderTopping> orderToppings = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : mergedToppings.entrySet()) {
            Integer toppingId = entry.getKey();
            Integer quantity = entry.getValue();

            Topping topping = getToppingById(toppingId);

            // Trừ số lượng topping
            decrementToppingDailyPlan(topping, quantity);

            double toppingPrice = topping.getPrice() * quantity;
            OrderTopping orderTopping = buildOrderTopping(orderDetail, topping, quantity, toppingPrice);
            orderToppings.add(orderTopping);
        }
        return orderToppings;
    }


    /**
     * Hàm helper chung MỚI để cập nhật số lượng DailyPlan
     */
    private void updateDailyPlanQuantity(Integer itemId, ItemType itemType, int quantityChange) {
        if (quantityChange == 0) {
            return; // Không làm gì nếu không thay đổi
        }

        // 1. Tìm plan. Nếu không tìm thấy -> BÁO LỖI (nghiệp vụ nghiêm ngặt)
        DailyPlan dailyPlan = dailyPlanRepository.findByItemIdAndItemTypeAndPlanDate(itemId, itemType, LocalDate.now())
                .orElseThrow(() -> new AppException(
                        itemType == ItemType.DISH ? ErrorCode.DISH_NOT_FOUND : ErrorCode.TOPPING_NOT_FOUND
                ));

        // 2. Nếu tìm thấy, tiếp tục xử lý như cũ
        int newRemaining = dailyPlan.getRemainingQuantity() + quantityChange;

        // Nếu là trừ kho (quantityChange < 0) thì phải kiểm tra
        if (quantityChange < 0 && newRemaining < 0) {
            throw new AppException(ErrorCode.NOT_ENOUGH_QUANTITY);
        }

        dailyPlan.setRemainingQuantity(newRemaining);
        dailyPlanRepository.save(dailyPlan);
    }

    // 4 hàm cũ gọi tới hàm chung
    private void decrementDishDailyPlan(Dish dish, int quantity) {
        updateDailyPlanQuantity(dish.getDishId(), ItemType.DISH, -quantity);
    }
    private void decrementToppingDailyPlan(Topping topping, int quantity) {
        updateDailyPlanQuantity(topping.getToppingId(), ItemType.TOPPING, -quantity);
    }
    private void incrementDishDailyPlan(Dish dish, int quantity) {
        updateDailyPlanQuantity(dish.getDishId(), ItemType.DISH, quantity);
    }
    private void incrementToppingDailyPlan(Topping topping, int quantity) {
        updateDailyPlanQuantity(topping.getToppingId(), ItemType.TOPPING, quantity);
    }

    /**
     * ⭐ SỬA LỖI (StaleObjectState): Thêm entityManager.evict()
     */
    private void updateToppingsAndInventory(OrderDetail detail, List<OrderDetailUpdateRequest.ToppingSelection> newToppingRequests) {

        // 1. CỘNG LẠI SỐ LƯỢNG (Hoàn trả kho)
        // ... (giữ nguyên)
        for (OrderTopping ot : detail.getOrderToppings()) {
            incrementToppingDailyPlan(ot.getTopping(), ot.getQuantity());
        }

        // 2. Xóa TỨC THÌ tất cả topping cũ khỏi DB
        // ... (giữ nguyên)
        orderToppingRepository.deleteAllInBatch(detail.getOrderToppings());

        // ================== START SỬA LỖI ==================

        // 3. Lấy Hibernate Session gốc từ EntityManager
        Session session = entityManager.unwrap(Session.class);

        // 4. ⭐ EVICT (ĐUỔI) topping cũ khỏi cache của Hibernate
        //    Dùng session.evict() thay vì entityManager.evict()
        for (OrderTopping ot : detail.getOrderToppings()) {
            session.evict(ot); // <-- Sửa ở đây
        }

        // 5. Xóa chúng khỏi collection trong bộ nhớ (giờ đã an toàn)
        detail.getOrderToppings().clear();

        // =================== END SỬA LỖI ===================

        // 6. Gộp topping MỚI
        // ... (giữ nguyên)
        Map<Integer, Integer> mergedToppings = newToppingRequests.stream()
                .collect(Collectors.groupingBy(
                        OrderDetailUpdateRequest.ToppingSelection::getToppingId,
                        Collectors.summingInt(OrderDetailUpdateRequest.ToppingSelection::getQuantity)
                ));

        // 7. TRỪ KHO và tạo topping MỚI
        // ... (giữ nguyên)
        List<OrderTopping> newToppings = mergedToppings.entrySet().stream().map(entry -> {
            // ... (code bên trong giữ nguyên)
            Integer toppingId = entry.getKey();
            Integer quantity = entry.getValue();

            Topping topping = getToppingById(toppingId);
            decrementToppingDailyPlan(topping, quantity);
            double toppingPrice = topping.getPrice() * quantity;

            return buildOrderTopping(detail, topping, quantity, toppingPrice);
        }).toList();

        // 8. Thêm topping mới vào collection
        detail.getOrderToppings().addAll(newToppings);
    }

    /**
     * Hàm helper tính tổng tiền
     */
    private void recalculateTotalPrice(OrderDetail detail) {
        double toppingsPrice = detail.getOrderToppings().stream()
                .mapToDouble(OrderTopping::getToppingPrice)
                .sum();
        detail.setTotalPrice(detail.getDish().getPrice() + toppingsPrice);
    }

}