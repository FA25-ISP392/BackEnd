package com.isp392.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "payment")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentId")
    int id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    Orders order;

    @Column(name = "method",  nullable = false)
    String method;

    @Column(name = "total", nullable = false)
    double total;
}
