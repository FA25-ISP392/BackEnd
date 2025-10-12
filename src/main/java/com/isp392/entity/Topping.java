package com.isp392.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "topping")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Topping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "toppingId")
    int id;

    @Column(name = "toppingName", nullable = false)
    String name;

    @Column(nullable = false)
    double calories;

    @Column(name = "stockQuantity", nullable = false)
    double quantity;

    @Column(nullable = false)
    double price;

    @Column(nullable = false)
    double gram;

    @OneToMany(mappedBy = "topping")
    List<OrderTopping> orderToppings;
}
