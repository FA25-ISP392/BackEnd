package com.isp392.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerUpdateRequest extends AccountUpdateRequest {

    @PositiveOrZero(message = "HEIGHT_INVALID")
    Double height;

    @PositiveOrZero(message = "WEIGHT_INVALID")
    Double weight;

    Boolean sex;

    Integer portion;
}
