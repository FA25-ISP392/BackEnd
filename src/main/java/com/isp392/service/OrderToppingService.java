package com.isp392.service;

import com.isp392.dto.request.OrderToppingCreationRequest;
import com.isp392.dto.request.OrderToppingUpdateRequest;
import com.isp392.dto.response.OrderToppingResponse;
import com.isp392.entity.OrderTopping;
import com.isp392.entity.OrderToppingId;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.OrderToppingMapper;
import com.isp392.repository.OrderToppingRepository;
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

    //Lấy thông tin một OrderTopping theo id kép

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
        OrderToppingId id = new OrderToppingId(request.getOrderDetailId(), request.getToppingId());

        if (orderToppingRepository.existsById(id)) {
            throw new AppException(ErrorCode.ORDER_TOPPING_ALREADY_EXISTS);
        }
        OrderTopping orderTopping = orderToppingMapper.toOrderTopping(request);
        orderTopping.setOrderDetailId(id.getOrderDetailId());
        orderTopping.setToppingId(id.getToppingId());

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

