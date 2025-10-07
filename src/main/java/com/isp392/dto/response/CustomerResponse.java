package com.isp392.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerResponse {
    long customerId;

    String customerPhone;

    String customerName ;

    String customerEmail;

    Double height;

    Double weight;

    Boolean sex;

    Integer age;

    Integer portion;
}
