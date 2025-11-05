package com.isp392.service;

import com.isp392.dto.request.OrdersCreationRequest;
import com.isp392.dto.request.OrdersUpdateRequest;
import com.isp392.dto.response.OrdersResponse;
import com.isp392.entity.Customer;
import com.isp392.entity.OrderDetail;
import com.isp392.entity.Orders;
import com.isp392.entity.TableEntity;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.OrdersMapper;
import com.isp392.repository.CustomerRepository;
import com.isp392.repository.OrdersRepository;
import com.isp392.repository.TableRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrdersService {

    OrdersRepository ordersRepository;
    CustomerRepository customerRepository;
    TableRepository tableRepository;
    OrdersMapper ordersMapper;

    @Transactional
    public OrdersResponse createOrder(OrdersCreationRequest request) {
        Optional<Orders> existingOrderOpt = ordersRepository.findActiveOrderByCustomerAndTable(
                request.getCustomerId(),
                request.getTableId()
        );

        if (existingOrderOpt.isPresent()) {
            return handleExistingOrder(existingOrderOpt.get(), request.getCustomerId(), request.getTableId());
        }
        return createNewOrder(request);
    }

    public OrdersResponse getOrder(Integer orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        Hibernate.initialize(order.getOrderDetails());

        double totalOrderPrice = order.getOrderDetails().stream()
                .mapToDouble(OrderDetail::getTotalPrice)
                .sum();

        OrdersResponse response = ordersMapper.toOrdersResponse(order);
        response.setTotalPrice(totalOrderPrice);
        return response;
    }

    @Transactional
    public OrdersResponse updateOrder(Integer orderId, OrdersUpdateRequest request) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Nếu có tableId, fetch table và set
        if (request.getTableId() != null) {
            TableEntity table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));
            order.setTable(table);
        }

        ordersMapper.updateOrder(order, request);

        return ordersMapper.toOrdersResponse(order);
    }

    public void deleteOrder(Integer orderId) {
        if (!ordersRepository.existsById(orderId)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        ordersRepository.deleteById(orderId);
    }

    // HELPER METHODS

    /**
     * (HELPER) Xử lý khi tìm thấy một đơn hàng đang hoạt động.
     * @param existingOrder Đơn hàng đã tìm thấy.
     * @param customerId ID khách hàng (dùng để log).
     * @param tableId ID bàn (dùng để log).
     * @return OrdersResponse của đơn hàng hiện có.
     */
    private OrdersResponse handleExistingOrder(Orders existingOrder, Integer customerId, Integer tableId) {
        log.info("Found existing active order (ID: {}) for customer {} at table {}",
                existingOrder.getOrderId(), customerId, tableId);

        // Load chi tiết đơn hàng (nếu chưa load) và tính tổng tiền
        Hibernate.initialize(existingOrder.getOrderDetails());
        double totalOrderPrice = calculateTotalOrderPrice(existingOrder);

        OrdersResponse response = ordersMapper.toOrdersResponse(existingOrder);
        response.setTotalPrice(totalOrderPrice);
        return response;
    }

    /**
     * (HELPER) Tạo một đơn hàng mới khi không tìm thấy đơn hàng đang hoạt động.
     * @param request Dữ liệu yêu cầu tạo đơn hàng.
     * @return OrdersResponse của đơn hàng vừa tạo.
     */
    private OrdersResponse createNewOrder(OrdersCreationRequest request) {
        log.info("No active order found for customer {} at table {}. Creating a new order.",
                request.getCustomerId(), request.getTableId());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        TableEntity table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));

        if (table.isServing()) {
            log.warn("Failed to create new order: Table {} is already being served.", table.getTableId());
            throw new AppException(ErrorCode.TABLE_ALREADY_SERVING);
        }

        Orders newOrder = ordersMapper.toOrders(request, customer, table);

        Orders savedOrder = ordersRepository.save(newOrder);

        OrdersResponse response = ordersMapper.toOrdersResponse(savedOrder);
        response.setTotalPrice(0.0);
        return response;
    }

    /**
     * (HELPER) Tính tổng tiền của một đơn hàng dựa trên các chi tiết đơn hàng.
     * @param order Đơn hàng cần tính tổng tiền.
     * @return Tổng tiền của đơn hàng.
     */
    private double calculateTotalOrderPrice(Orders order) {
        if (order.getOrderDetails() == null) {
            return 0.0;
        }
        return order.getOrderDetails().stream()
                .mapToDouble(OrderDetail::getTotalPrice) // Giả sử OrderDetail có getter getTotalPrice()
                .sum();
    }

}