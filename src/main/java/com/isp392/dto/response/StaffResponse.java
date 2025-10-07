package com.isp392.dto.response;

import com.isp392.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffResponse {
    String staffName;
    String staffPhone;
    String staffEmail;
    Role role;
}
