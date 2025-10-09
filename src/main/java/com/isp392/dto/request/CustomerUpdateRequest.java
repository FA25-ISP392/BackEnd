package com.isp392.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerUpdateRequest extends AccountUpdateRequest {

    @PositiveOrZero(message = "HEIGHT_INVALID")
    Double height;

    @PositiveOrZero(message = "WEIGHT_INVALID")
    Double weight;

    Boolean sex;

    Integer portion;
}
