package com.isp392.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingUpdateRequest {
    int tableId;
    int seat;
    LocalDateTime bookingDate;
}
