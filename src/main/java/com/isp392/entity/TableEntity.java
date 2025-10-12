package com.isp392.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "restaurant_table") // ✅ Đổi tên để tránh lỗi
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int tableId;

    @Column(length = 255, nullable = false, unique = true)
    String tableName;

    @Column(nullable = false)
    int seatTable; // 🪑 số ghế của bàn

    @Column(nullable = false, columnDefinition = "bit default 1")
    Boolean isAvailable;
}