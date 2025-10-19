package com.isp392.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.isp392.enums.Category;
import com.isp392.enums.DishType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dish")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int dishId;

    @Column(length = 255, unique = true, nullable = false)
    String dishName;

    @Column(length = 255, nullable = false)
    String description;

    @Column(nullable = false)
    Double price;

    @Column(nullable = false)
    Double calo;

    @Column(nullable = false, columnDefinition = "bit default 1")
    Boolean isAvailable;

    @Column(length = 255, nullable = false, columnDefinition = "varchar(255) default 'loading'")
    String picture;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    Category category;

    @Enumerated(EnumType.STRING)
    @Column(length = 20) // Không cần 'nullable = false' nữa
    private DishType type;

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude // QUAN TRỌNG: Ngắt vòng lặp toString() tại đây
    private List<DishTopping> dishToppings = new ArrayList<>();
}