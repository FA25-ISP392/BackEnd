package com.isp392.repository;

import com.isp392.entity.Booking;
import com.isp392.entity.TableEntity;
import com.isp392.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByTableAndBookingDateBetween(
            TableEntity table,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    List<Booking> findByStatus(BookingStatus status);
}
