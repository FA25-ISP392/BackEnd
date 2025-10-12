package com.isp392.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableResponse {
    int tableId;
    String tableName;
    int seatTable;
    Boolean isAvailable;
}
