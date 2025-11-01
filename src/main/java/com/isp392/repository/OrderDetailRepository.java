package com.isp392.repository;

import com.isp392.entity.OrderDetail;
import com.isp392.enums.OrderDetailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    @Query("SELECT od FROM OrderDetail od " +
            "LEFT JOIN FETCH od.orderToppings ot " +
            "LEFT JOIN FETCH ot.topping " +
            "WHERE od.orderDetailId = :id")
    Optional<OrderDetail> findByIdWithToppings(@Param("id") Integer id);

    List<OrderDetail> findByStatus(OrderDetailStatus status);
    @Query("SELECT od FROM OrderDetail od JOIN FETCH od.order o WHERE od.status = :status ORDER BY o.orderDate DESC")
    List<OrderDetail> findByStatusWithOrder(@Param("status") OrderDetailStatus status);
}
