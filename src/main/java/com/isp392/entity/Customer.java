package com.isp392.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int customerId;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    Account account;

    @Column(name = "customerName", nullable = false, length = 100)
    String customerName;

    @Column(name = "customerPhone", nullable = false, length = 15, unique = true)
    String customerPhone;

    @Column(name = "customerEmail", nullable = true, length = 100, unique = true)
    String customerEmail;

    Double height;
    Double weight;
    Boolean sex;
    Integer age;
    Integer portion;
}
