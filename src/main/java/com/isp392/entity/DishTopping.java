package com.isp392.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "DishTopping")
@IdClass(DishToppingId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishTopping {

    @Id
    @Column(name = "dishId")
    int dishId;

    @Id
    @Column(name = "toppingId")
    int toppingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dishId", insertable = false, updatable = false)
    Dish dish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toppingId", insertable = false, updatable = false)
    Topping topping;
}