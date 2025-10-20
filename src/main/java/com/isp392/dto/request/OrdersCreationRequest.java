package com.isp392.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrdersCreationRequest {

    @NotNull(message = "NOT_BLANK")
    Integer customerId;

    @NotNull(message = "NOT_BLANK")
    Integer tableId;

    @NotNull(message = "NOT_BLANK")
    LocalDateTime orderDate;

}
