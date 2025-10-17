package com.isp392.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "order_topping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderTopping {

    @EmbeddedId
    OrderToppingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderDetailId")  // ánh xạ với embedded id
    @JoinColumn(name = "order_detail_id", nullable = false)
    OrderDetail orderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("toppingId")      // ánh xạ với embedded id
    @JoinColumn(name = "topping_id", nullable = false)
    Topping topping;

    @Column(nullable = false)
    Integer quantity;

    @Column(nullable = false)
    Double toppingPrice;
}
