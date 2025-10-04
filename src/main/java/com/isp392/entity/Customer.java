package com.isp392.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "Customer")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    long id;

    @Column(name = "phone")
    String phone;

    @Column(name = "fullName", nullable = false)
    String fullName;

    @Column(name = "height")
    Double height;

    @Column(name = "weight")
    Double weight;

    @Column(name = "sex")
    Boolean sex;

    @Column(name = "age")
    Integer age;

    @Column(name = "portion")
    Integer portion;
}
