package com.isp392.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Ingredent")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredentId")
    int id;

    @Column(name = "ingredentName", nullable = false, length = 255)
    String name;
    @Column(nullable = false)
    double calories;
    @Column(nullable = false)
    double quantity;
    @Column(nullable = false)
    double price;
}
