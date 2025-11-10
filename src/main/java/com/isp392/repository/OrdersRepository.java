// [fa25-isp392/backend/BackEnd-7faba5248a65f8474421bb8690b3aee5ef1b1750/src/main/java/com/isp392/repository/OrdersRepository.java]
package com.isp392.repository;

import com.isp392.entity.Orders;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    /**
     * Tìm đơn hàng đang hoạt động (chưa thanh toán) cho một khách hàng tại một bàn cụ thể.
     * (Hàm này dùng để cho Khách A vào lại)
     */
    @Query("SELECT o FROM Orders o " +
            "WHERE o.customer.customerId = :customerId " +
            "AND o.table.tableId = :tableId " +
            "AND (o.paid = false OR o.paid IS NULL)")
    Optional<Orders> findActiveOrderByCustomerAndTable(
            @Param("customerId") Integer customerId,
            @Param("tableId") Integer tableId);

    // === START SỬA: THÊM HÀM NÀY ===
    /**
     * Tìm BẤT KỲ đơn hàng đang hoạt động (chưa thanh toán) tại một bàn.
     * (Hàm này dùng để chặn Khách B)
     */
    @Query("SELECT o FROM Orders o " +
            "WHERE o.table.tableId = :tableId " +
            "AND (o.paid = false OR o.paid IS NULL)")
    Optional<Orders> findActiveOrderByTable(@Param("tableId") Integer tableId);
    // === END SỬA ===


    @Query(value = "SELECT o FROM Orders o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.dish " +
            "WHERE o.customer.customerId = :customerId " +
            "AND o.paid = true",
            countQuery = "SELECT count(o) FROM Orders o " +
                    "WHERE o.customer.customerId = :customerId AND o.paid = true")
    Page<Orders> findAllPaidByCustomer_CustomerId(@Param("customerId") Integer customerId, Pageable pageable);
}