package com.isp392.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@ToString // Thêm @ToString để có thể Exclude
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishTopping {

    @EmbeddedId
    DishToppingId id;

    @ManyToOne
    @MapsId("dishId")
    @JoinColumn(name = "dish_id")
    @JsonBackReference
    @ToString.Exclude // QUAN TRỌNG: Ngắt vòng lặp toString() tại đây
    Dish dish;

    @ManyToOne
    @MapsId("toppingId")
    @JoinColumn(name = "topping_id")
    @ToString.Exclude // QUAN TRỌNG: Ngắt vòng lặp toString() tại đây
    Topping topping;
}