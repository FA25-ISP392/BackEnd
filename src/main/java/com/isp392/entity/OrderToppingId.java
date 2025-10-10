package com.isp392.entity;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderToppingId implements Serializable {
    int orderDetailId;
    int toppingId;
}