package com.isp392.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerResponse {
    long id;

    String phone;

    String fullName;

    Double height;

    Double weight;

    Boolean sex;

    Integer age;

    Integer portion;
}
