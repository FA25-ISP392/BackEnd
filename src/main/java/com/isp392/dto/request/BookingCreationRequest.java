package com.isp392.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingCreationRequest {
    String customerName;
    String customerPhone;
    LocalDateTime bookingTime;
    int seat;
    LocalDateTime bookingDate;
}
