package com.isp392.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "paymet")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payemtId")
    int id;

    @Column(name = "orderId",  nullable = false)
    int orderId;

    @Column(name = "method",  nullable = false)
    String method;

    @Column(name = "total", nullable = false)
    double total;
}
