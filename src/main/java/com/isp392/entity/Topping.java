package com.isp392.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Entity
@Table(name = "topping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Topping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int toppingId;

    @Column(name = "topping_name", nullable = false)
    String name;

    @Column(nullable = false)
    double calories;

    @Column(nullable = false)
    double price;

    @Column(nullable = false)
    double gram;

    @OneToMany(mappedBy = "topping")
    @JsonBackReference
    @ToString.Exclude // QUAN TRỌNG: Ngắt vòng lặp toString() tại đây
    private List<DishTopping> dishToppings;
}