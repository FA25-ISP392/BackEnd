package com.isp392.repository;

import com.isp392.entity.Booking;
import com.isp392.entity.TableEntity;
import com.isp392.enums.BookingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    @Query("SELECT b FROM Booking b WHERE b.table = :table AND b.bookingDate BETWEEN :startTime AND :endTime AND b.status IN ('APPROVED', 'PENDING')")
    List<Booking> findByTableAndBookingDateBetween(TableEntity table, LocalDateTime startTime, LocalDateTime endTime
    );

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);
    Page<Booking> findByCustomer_CustomerId(int id, Pageable pageable);
}
