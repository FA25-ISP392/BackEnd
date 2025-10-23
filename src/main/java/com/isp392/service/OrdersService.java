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
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrdersService {

    OrdersRepository ordersRepository;
    CustomerRepository customerRepository;
    TableRepository tableRepository;
    OrdersMapper ordersMapper;

    public OrdersResponse createOrder(OrdersCreationRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        TableEntity table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));

        Orders order = ordersMapper.toOrders(request, customer, table);

        Orders saved = ordersRepository.save(order);

        return ordersMapper.toOrdersResponse(saved);
    }

public OrdersResponse getOrder(Integer orderId) {
    Orders order = ordersRepository.findById(orderId)
            .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

    // Load chi tiết order
    Hibernate.initialize(order.getOrderDetails());

    // Tính tổng giá đơn hàng = tổng totalPrice của từng orderDetail
    double totalOrderPrice = order.getOrderDetails().stream()
            .mapToDouble(OrderDetail::getTotalPrice)
            .sum();

    // Map sang response
    OrdersResponse response = ordersMapper.toOrdersResponse(order);
    response.setTotalPrice(totalOrderPrice); // thêm trường này trong OrdersResponse
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
}
