package com.isp392.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ToppingResponse {
    int id;
    String name;
    double calories;
    double quantity;
    double price;
    double gram;
}
