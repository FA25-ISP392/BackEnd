package com.isp392.repository;

import com.isp392.entity.OrderDetail;
import com.isp392.entity.OrderTopping;
import com.isp392.entity.OrderToppingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderToppingRepository extends JpaRepository<OrderTopping, OrderToppingId> {
    List<OrderTopping> findByOrderDetail_OrderDetailId(Integer orderDetailId);

    void deleteByOrderDetail_OrderDetailId(Integer orderDetailId);

    List<OrderTopping> findByOrderDetail(OrderDetail orderDetail);

    void deleteByOrderDetail(OrderDetail orderDetail);
}
