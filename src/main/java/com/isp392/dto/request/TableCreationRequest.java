package com.isp392.dto.request;

import com.isp392.enums.TableStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableCreationRequest {
    String tableName;
    int seatTable;
    TableStatus status;
}
