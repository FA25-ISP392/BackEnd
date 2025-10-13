package com.isp392.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ToppingResponse {
    int toppingId;
    String name;
    double price;
    double calories;
    double gram;
}
