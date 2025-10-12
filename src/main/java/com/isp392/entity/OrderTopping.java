package com.isp392.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "OrderTopping")
@IdClass(OrderToppingId.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderTopping {

    @Id
    @Column(name = "orderDetailId")
    int orderDetailId;

    @Id
    @Column(name = "toppingId")
    int toppingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toppingId", insertable = false, updatable = false)
    Topping topping;

    @Column(nullable = false)
    int quantity;

    @Column(nullable = false)
    double toppingPrice;
}
