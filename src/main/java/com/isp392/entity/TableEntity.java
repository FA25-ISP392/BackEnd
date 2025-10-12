package com.isp392.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurant_table")
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
    int seatTable;

    @Column(nullable = false, columnDefinition = "bit default 1")
    Boolean isAvailable;

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // tránh vòng lặp khi in log
    @EqualsAndHashCode.Exclude
    List<Booking> bookings = new ArrayList<>();
}
