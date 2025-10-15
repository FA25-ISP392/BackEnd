package com.isp392.dto.response;

import com.isp392.enums.TableStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableResponse {
    int tableId;
    String tableName;
    int seatTable;
    TableStatus status;
}
