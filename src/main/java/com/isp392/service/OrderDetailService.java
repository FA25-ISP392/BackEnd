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
    DailyPlanService dailyPlanService;
    OrderToppingRepository orderToppingRepository;
    EntityManager entityManager;
    TableRepository tableRepository;

    // ===================================================================
    // CÁC HÀM PUBLIC (CHỈ ĐIỀU PHỐI)
    // ===================================================================

    @Transactional
    public OrderDetailResponse createOrderDetail(OrderDetailCreationRequest request) {
        // VIỆC 1: Lấy các entity gốc và kiểm tra
        Orders order = findOrderById(request.getOrderId());
        Dish dish = findDishById(request.getDishId());
        validateAndSetTableServing(order); // Kiểm tra và set bàn "đang phục vụ"

        // VIỆC 2: Cập nhật kho (trừ 1 món)
        updateDishInventory(dish.getDishId(), -1);

        // VIỆC 3: Tạo OrderDetail
        OrderDetail orderDetail = buildOrderDetail(order, dish, request.getNote());

        // VIỆC 4: Xử lý Topping (gộp, trừ kho, build)
        if (request.getToppings() != null && !request.getToppings().isEmpty()) {
            Map<Integer, Integer> mergedToppings = mergeCreateToppings(request.getToppings());
            List<OrderTopping> orderToppings = buildNewOrderToppings(orderDetail, mergedToppings);
            orderDetail.setOrderToppings(orderToppings);
        }

        // VIỆC 5: Tính tổng tiền
        recalculateTotalPrice(orderDetail);

        // VIỆC 6: Lưu và Map
        OrderDetail savedDetail = orderDetailRepository.save(orderDetail);
        return mapToResponse(savedDetail);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(int orderDetailId) {
        // VIỆC 1: Lấy entity
        OrderDetail orderDetail = findOrderDetailWithToppings(orderDetailId);
        // VIỆC 2: Map sang response
        return mapToResponse(orderDetail);
    }

    @Transactional(readOnly = true)
    public List<OrderDetailResponse> getOrderDetailsByStatus(OrderDetailStatus status) {
        // VIỆC 1: Lấy list entity
        List<OrderDetail> details = orderDetailRepository.findByStatusWithOrder(status);
        // VIỆC 2: Map list
        return details.stream()
                .map(orderDetailMapper::toOrderDetailResponse) // Dùng mapper đơn giản vì không cần topping
                .toList();
    }

    @Transactional
    public OrderDetailResponse updateOrderDetail(OrderDetailUpdateRequest request) {
        // VIỆC 1: Lấy entity
        OrderDetail detail = findOrderDetailWithToppings(request.getOrderDetailId());

        // VIỆC 2: Map các trường đơn giản (note, status)
        orderDetailMapper.updateOrderDetail(detail, request);

        // VIỆC 3: Xử lý cập nhật topping (Nếu có)
        if (request.getToppings() != null) {
            processToppingUpdate(detail, request.getToppings());
        }

        // VIỆC 4: Tính lại tổng tiền
        recalculateTotalPrice(detail);

        // VIỆC 5: Map và trả về
        return mapToResponse(detail);
    }

    @Transactional
    public void deleteOrderDetail(Integer orderDetailId) {
        // VIỆC 1: Lấy entity
        OrderDetail orderDetail = findOrderDetailWithToppings(orderDetailId);

        // VIỆC 2: Kiểm tra nghiệp vụ
        validateDeletableStatus(orderDetail);

        // VIỆC 3: Hoàn kho (Món ăn và Topping)
        revertAllInventory(orderDetail);

        // VIỆC 4: Xóa
        orderDetailRepository.delete(orderDetail);
    }

    // ===================================================================
    // HELPER: LẤY DỮ LIỆU (FINDERS)
    // ===================================================================

    private Orders findOrderById(Integer orderId) {
        return ordersRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    private Dish findDishById(Integer dishId) {
        return dishRepository.findById(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));
    }

    private Topping findToppingById(Integer toppingId) {
        return toppingRepository.findById(toppingId)
                .orElseThrow(() -> new AppException(ErrorCode.TOPPING_NOT_FOUND));
    }

    private OrderDetail findOrderDetailWithToppings(Integer orderDetailId) {
        return orderDetailRepository.findByIdWithToppings(orderDetailId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_DETAIL_NOT_FOUND));
    }

    // ===================================================================
    // HELPER: KIỂM TRA (VALIDATORS)
    // ===================================================================

    private void validateAndSetTableServing(Orders order) {
        TableEntity table = order.getTable();
        if (table == null || !table.isAvailable()) {
            throw new RuntimeException("Table is not available for ordering.");
        }
        if (!table.isServing()) {
            table.setServing(true);
            tableRepository.save(table);
        }
    }

    private void validateDeletableStatus(OrderDetail orderDetail) {
        if (orderDetail.getStatus() != OrderDetailStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_DETAIL_CANNOT_BE_CANCELLED);
        }
    }

    // ===================================================================
    // HELPER: XÂY DỰNG (BUILDERS & MAPPERS)
    // ===================================================================

    private OrderDetail buildOrderDetail(Orders order, Dish dish, String note) {
        return OrderDetail.builder()
                .order(order)
                .dish(dish)
                .status(OrderDetailStatus.PENDING)
                .note(note)
                .orderToppings(new ArrayList<>()) // Khởi tạo list rỗng
                .build();
    }

    private OrderTopping buildOrderTopping(OrderDetail orderDetail, Topping topping, int quantity, double toppingPrice) {
        return OrderTopping.builder()
                .id(new OrderToppingId())
                .orderDetail(orderDetail)
                .topping(topping)
                .quantity(quantity)
                .toppingPrice(toppingPrice)
                .build();
    }

    /**
     * Helper map OrderDetail sang Response (kèm Topping)
     */
    private OrderDetailResponse mapToResponse(OrderDetail orderDetail) {
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

    // ===================================================================
    // HELPER: LOGIC NGHIỆP VỤ (PROCESSORS)
    // ===================================================================

    /**
     * Tính toán lại tổng tiền cho 1 OrderDetail
     */
    private void recalculateTotalPrice(OrderDetail detail) {
        double toppingsPrice = detail.getOrderToppings().stream()
                .mapToDouble(OrderTopping::getToppingPrice)
                .sum();

        // Đảm bảo dish không null trước khi lấy giá
        double dishPrice = (detail.getDish() != null && detail.getDish().getPrice() != null)
                ? detail.getDish().getPrice()
                : 0.0;

        detail.setTotalPrice(dishPrice + toppingsPrice);
    }

    /**
     * Gộp danh sách ToppingSelection (dùng cho Create)
     */
    private Map<Integer, Integer> mergeCreateToppings(List<OrderDetailCreationRequest.ToppingSelection> toppingRequests) {
        return toppingRequests.stream()
                .collect(Collectors.groupingBy(
                        OrderDetailCreationRequest.ToppingSelection::getToppingId,
                        Collectors.summingInt(OrderDetailCreationRequest.ToppingSelection::getQuantity)
                ));
    }

    /**
     * Gộp danh sách ToppingSelection (dùng cho Update)
     */
    private Map<Integer, Integer> mergeUpdateToppings(List<OrderDetailUpdateRequest.ToppingSelection> toppingRequests) {
        return toppingRequests.stream()
                .collect(Collectors.groupingBy(
                        OrderDetailUpdateRequest.ToppingSelection::getToppingId,
                        Collectors.summingInt(OrderDetailUpdateRequest.ToppingSelection::getQuantity)
                ));
    }

    /**
     * Lặp qua Map topping đã gộp, trừ kho và build List<OrderTopping>
     */
    private List<OrderTopping> buildNewOrderToppings(OrderDetail orderDetail, Map<Integer, Integer> mergedToppings) {
        List<OrderTopping> newToppings = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : mergedToppings.entrySet()) {
            Integer toppingId = entry.getKey();
            Integer quantity = entry.getValue();

            Topping topping = findToppingById(toppingId);

            // Trừ kho topping
            updateToppingInventory(topping.getToppingId(), -quantity);

            double toppingPrice = topping.getPrice() * quantity;
            newToppings.add(buildOrderTopping(orderDetail, topping, quantity, toppingPrice));
        }
        return newToppings;
    }

    /**
     * Điều phối toàn bộ logic cập nhật topping
     */
    private void processToppingUpdate(OrderDetail detail, List<OrderDetailUpdateRequest.ToppingSelection> newToppingRequests) {
        // 1. Hoàn kho topping cũ
        revertToppingInventory(detail.getOrderToppings());

        // 2. Xóa topping cũ (khỏi DB và cache)
        clearOldToppings(detail);

        // 3. Gộp topping mới
        Map<Integer, Integer> mergedNewToppings = mergeUpdateToppings(newToppingRequests);

        // 4. Trừ kho và build topping mới
        List<OrderTopping> newToppings = buildNewOrderToppings(detail, mergedNewToppings);

        // 5. Thêm topping mới vào collection
        detail.getOrderToppings().addAll(newToppings);
    }

    /**
     * Xóa topping cũ khỏi DB và evict khỏi Hibernate cache
     */
    private void clearOldToppings(OrderDetail detail) {
        // Xóa TỨC THÌ tất cả topping cũ khỏi DB
        orderToppingRepository.deleteAllInBatch(detail.getOrderToppings());

        // Lấy Hibernate Session gốc từ EntityManager
        Session session = entityManager.unwrap(Session.class);

        // EVICT (ĐUỔI) topping cũ khỏi cache của Hibernate
        for (OrderTopping ot : detail.getOrderToppings()) {
            session.evict(ot);
        }

        // Xóa chúng khỏi collection trong bộ nhớ
        detail.getOrderToppings().clear();
    }


    // ===================================================================
    // HELPER: CẬP NHẬT KHO (INVENTORY)
    // ===================================================================

    /**
     * Cập nhật kho (cộng/trừ) cho MÓN ĂN
     */
    private void updateDishInventory(int dishId, int quantityChange) {
        dailyPlanService.updateRemainingQuantity(dishId, ItemType.DISH, LocalDate.now(), quantityChange);
    }

    /**
     * Cập nhật kho (cộng/trừ) cho TOPPING
     */
    private void updateToppingInventory(int toppingId, int quantityChange) {
        dailyPlanService.updateRemainingQuantity(toppingId, ItemType.TOPPING, LocalDate.now(), quantityChange);
    }

    /**
     * Hoàn lại kho cho tất cả topping trong 1 list
     */
    private void revertToppingInventory(List<OrderTopping> toppings) {
        for (OrderTopping ot : toppings) {
            updateToppingInventory(ot.getTopping().getToppingId(), ot.getQuantity());
        }
    }

    /**
     * Hoàn lại kho cho 1 món và tất cả topping của nó
     */
    private void revertAllInventory(OrderDetail orderDetail) {
        // Hoàn kho món ăn
        updateDishInventory(orderDetail.getDish().getDishId(), 1);
        // Hoàn kho topping
        revertToppingInventory(orderDetail.getOrderToppings());
    }
}