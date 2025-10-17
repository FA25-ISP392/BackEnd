package com.isp392.service;

import com.isp392.dto.request.OrdersCreationRequest;
import com.isp392.dto.request.OrdersUpdateRequest;
import com.isp392.dto.response.OrdersResponse;
import com.isp392.entity.Customer;
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
        // 1. Kiểm tra customer và table có tồn tại không
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        TableEntity table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));

        // 2. Tạo order (map thủ công hoặc mapper chỉ map field đơn giản)
        Orders order = Orders.builder()
                .orderDate(request.getOrderDate())
                .customer(customer)
                .table(table)
                .build();

        // 3. Lưu DB
        Orders saved = ordersRepository.save(order);

        // 4. Trả response
        return ordersMapper.toOrdersResponse(saved);
    }


//    public List<OrdersResponse> getOrder() {
//        return ordersRepository.findAll()
//                .stream()
//                .map(ordersMapper::toOrdersResponse)
//                .toList();
//    }

    public OrdersResponse getOrder(Integer orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        Hibernate.initialize(order.getOrderDetails());
        return ordersMapper.toOrdersResponse(order);
    }

    public OrdersResponse updateOrder(Integer orderId, OrdersUpdateRequest request) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (request.getTableId() != null) {
            TableEntity table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));
            order.setTable(table);
        }

        if (request.getOrderDate() != null) {
            order.setOrderDate(request.getOrderDate());
        }

        Orders updated = ordersRepository.save(order);
        return ordersMapper.toOrdersResponse(updated);
    }

    public void deleteOrder(Integer orderId) {
        if (!ordersRepository.existsById(orderId)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        ordersRepository.deleteById(orderId);
    }
}
