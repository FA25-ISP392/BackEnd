package com.isp392.entity;

import com.isp392.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Booking")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookingId")
    int bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId", nullable = true)
    Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tableId")
    TableEntity table;

    @Column(nullable = false)
    int seat;

    @Enumerated(EnumType.STRING)
    BookingStatus status = BookingStatus.PENDING;

    @Column(name = "bookingDate", nullable = false)
    LocalDateTime bookingDate;

    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;
}
