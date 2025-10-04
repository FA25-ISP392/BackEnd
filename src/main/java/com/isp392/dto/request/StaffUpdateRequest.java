package com.isp392.dto.request;

import com.isp392.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffUpdateRequest {

    String password;

    String staffEmail;

    String staffPhone;

    String staffName;

    Role role;
}
