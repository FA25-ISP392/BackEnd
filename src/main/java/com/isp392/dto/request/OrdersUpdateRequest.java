package com.isp392.dto.request;

import com.isp392.entity.Customer;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrdersUpdateRequest {

    @NotNull(message = "NOT_BLANK")
    Integer tableId;

    @NotNull(message = "NOT_BLANK")
    LocalDateTime orderDate;
}
