package com.isp392.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishTopping {

    @EmbeddedId
    DishToppingId id;

    @ManyToOne
    @MapsId("dishId")
    @JoinColumn(name = "dish_id")
    @JsonBackReference // serialize từ Dish → DishTopping, DishTopping bỏ Dish
    Dish dish;

    @ManyToOne
    @MapsId("toppingId")
    @JoinColumn(name = "topping_id")
    // bỏ annotation tránh vòng lặp
    Topping topping;


}
