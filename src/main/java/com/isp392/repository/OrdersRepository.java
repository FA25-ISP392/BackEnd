package com.isp392.repository;

import com.isp392.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    /**
     * Tìm đơn hàng đang hoạt động (chưa thanh toán) cho một khách hàng tại một bàn cụ thể.
     *
     * @param customerId ID của khách hàng
     * @param tableId    ID của bàn
     * @return Optional chứa Orders nếu tìm thấy, ngược lại là Optional rỗng.
     */
    @Query("SELECT o FROM Orders o " +
            "WHERE o.customer.customerId = :customerId " +
            "AND o.table.tableId = :tableId " +
            "AND (o.paid = false OR o.paid IS NULL)")
    // Thêm điều kiện chưa thanh toán
    Optional<Orders> findActiveOrderByCustomerAndTable(
            @Param("customerId") Integer customerId,
            @Param("tableId") Integer tableId);
}
