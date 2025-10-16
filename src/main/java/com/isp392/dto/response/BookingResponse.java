    package com.isp392.dto.response;

    import com.isp392.enums.BookingStatus;
    import lombok.*;
    import lombok.experimental.FieldDefaults;
    import java.time.LocalDateTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public class BookingResponse {
        int bookingId;
        String customerName;
        String customerPhone;
        String customerEmail;
        int tableId;
        int seat;
        LocalDateTime createdAt;
        LocalDateTime bookingDate;
        String wantTable;
        BookingStatus status;
    }
