package com.isp392.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrdersUpdateRequest {

    Integer tableId;

    LocalDateTime orderDate;

    boolean isPaid;
}
