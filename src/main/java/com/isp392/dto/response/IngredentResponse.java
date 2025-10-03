package com.isp392.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IngredentResponse {
    int id;
    String name;
    double calories;
    double quantity;
    double price;
}
