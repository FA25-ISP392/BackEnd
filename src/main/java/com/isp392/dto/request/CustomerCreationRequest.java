package com.isp392.dto.request;

import com.isp392.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerCreationRequest extends AccountCreationRequest {

    @PositiveOrZero(message = "HEIGHT_INVALID")
    Double height;

    @PositiveOrZero(message = "WEIGHT_INVALID")
    Double weight;

    Boolean sex;

    Integer portion;
}
