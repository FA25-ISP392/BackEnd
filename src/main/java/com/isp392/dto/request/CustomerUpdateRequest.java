package com.isp392.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerUpdateRequest {
    @Pattern(regexp = "^(0|\\+84)\\d{9,10}$", message = "CUSTOMER_PHONE_INVALID")
    String customerPhone;
    @Size(min = 3,message = "CUSTOMER_FULLNAME_TOO_SHORT")
    String customerName;

    String password;

    Double height;

    Double weight;

    Boolean sex;

    Integer age;

    Integer portion;
}
