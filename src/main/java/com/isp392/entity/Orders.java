package com.isp392.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer orderId;

    // 🔗 Mối quan hệ với Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId", nullable = false)
    Customer customer;

    // 🔗 Mối quan hệ với TableEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tableId", nullable = false)
    TableEntity table;

    // 📅 Ngày đặt hàng
    @Column(nullable = false)
    LocalDateTime orderDate;
}
