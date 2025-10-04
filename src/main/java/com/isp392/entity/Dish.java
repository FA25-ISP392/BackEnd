package com.isp392.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "Dish")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer dishId;

    @Column(length = 255, unique = true, nullable = false)
    String dishName;

    @Column(length = 255, nullable = false)
    String description;

    @Column(precision = 10, scale = 2, nullable = false)
    BigDecimal price;

    @Column(precision = 10, scale = 2, nullable = false)
    BigDecimal calo;

    @Column
    Boolean isAvailable;

    @Column(length = 255, nullable = false, columnDefinition = "varchar(255) default 'loading'")
    String picture;
}
