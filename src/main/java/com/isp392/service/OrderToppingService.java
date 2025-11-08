package com.isp392.service;

import com.isp392.dto.request.OrderToppingCreationRequest;
import com.isp392.dto.request.OrderToppingUpdateRequest;
import com.isp392.dto.response.OrderToppingResponse;
import com.isp392.entity.OrderDetail;
import com.isp392.entity.OrderTopping;
import com.isp392.entity.OrderToppingId;
import com.isp392.entity.Topping;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.OrderToppingMapper;
import com.isp392.repository.OrderDetailRepository;
import com.isp392.repository.OrderToppingRepository;
import com.isp392.repository.ToppingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderToppingService {
    OrderToppingRepository orderToppingRepository;
    OrderToppingMapper orderToppingMapper;
    OrderDetailRepository orderDetailRepository;
    ToppingRepository toppingRepository;


    public OrderToppingResponse getOrderTopping(int orderDetailId, int toppingId) {
        OrderToppingId id = new OrderToppingId(orderDetailId, toppingId);
        OrderTopping orderTopping = orderToppingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_TOPPING_NOT_FOUND));
        return orderToppingMapper.toOrderToppingResponse(orderTopping);
    }

    /**
     * Tạo mới OrderTopping (thường được thêm khi khách chọn topping cho món)
     */
    public OrderToppingResponse createOrderTopping(OrderToppingCreationRequest request) {
        // Lấy entity liên quan
        OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_DETAIL_NOT_FOUND));
        Topping topping = toppingRepository.findById(request.getToppingId())
                .orElseThrow(() -> new AppException(ErrorCode.TOPPING_NOT_FOUND));

        // Tạo EmbeddedId
        OrderToppingId id = new OrderToppingId(request.getOrderDetailId(), request.getToppingId());

        if (orderToppingRepository.existsById(id)) {
            throw new AppException(ErrorCode.ORDER_TOPPING_ALREADY_EXISTS);
        }

        // Mapping DTO → Entity
        OrderTopping orderTopping = orderToppingMapper.toOrderTopping(request);

        // Gán id + quan hệ
        orderTopping.setId(id);
        orderTopping.setOrderDetail(orderDetail);
        orderTopping.setTopping(topping);

        OrderTopping saved = orderToppingRepository.save(orderTopping);
        return orderToppingMapper.toOrderToppingResponse(saved);
    }


    /**
     * Cập nhật OrderTopping
     */
    public OrderToppingResponse updateOrderTopping(OrderToppingUpdateRequest request) {
        OrderToppingId id = new OrderToppingId(request.getOrderDetailId(), request.getToppingId());

        OrderTopping existing = orderToppingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_TOPPING_NOT_FOUND));

        orderToppingMapper.updateOrderToping(request, existing);

        OrderTopping updated = orderToppingRepository.save(existing);

        return orderToppingMapper.toOrderToppingResponse(updated);
    }

    /**
     * Xóa topping khỏi order detail (nếu khách bỏ chọn topping)
     */
    public void deleteOrderTopping(int orderDetailId, int toppingId) {
        OrderToppingId id = new OrderToppingId(orderDetailId, toppingId);
        if (!orderToppingRepository.existsById(id)) {
            throw new AppException(ErrorCode.ORDER_TOPPING_NOT_FOUND);
        }
        
        orderToppingRepository.deleteById(id);
    }
}

