package com.isp392.repository;

import com.isp392.dto.response.BookingResponse;
import com.isp392.entity.Booking;
import com.isp392.entity.Payment;
import com.isp392.repository.projection.RevenueByMethodProjection;
import com.isp392.repository.projection.TotalRevenueProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByOrder_OrderId(int orderId);
    Optional<Payment> findByPayosOrderCode(long payosOrderCode);
    Payment findByPaymentLinkId(String paymentLinkId);

    @Query("SELECT SUM(p.total) as totalRevenue, COUNT(p) as totalOrders " +
            "FROM Payment p JOIN p.order o " +
            "WHERE p.status = com.isp392.enums.PaymentStatus.COMPLETED " +
            "AND o.orderDate BETWEEN :startDate AND :endDate")
    TotalRevenueProjection findTotalRevenueByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT p.method as method, SUM(p.total) as totalRevenue, COUNT(p) as totalOrders " +
            "FROM Payment p JOIN p.order o " +
            "WHERE p.status = com.isp392.enums.PaymentStatus.COMPLETED " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.method")
    List<RevenueByMethodProjection> findRevenueByMethodAndDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    Page<Payment> findAll(Pageable pageable);

    Page<Payment> findByCustomer_CustomerId(int customerId, Pageable pageable);
}
