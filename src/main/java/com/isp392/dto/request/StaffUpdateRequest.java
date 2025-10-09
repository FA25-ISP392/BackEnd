package com.isp392.dto.request;

import com.isp392.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffUpdateRequest {

    @Size(min = 8, max = 30, message = "PASSWORD_INVALID")
    String password;

    @Email(message = "STAFF_EMAIL_INVALID")
    String staffEmail;

    @Pattern(regexp = "^(0)[1-9]\\d{8}$", message = "STAFF_PHONE_INVALID")
    String staffPhone;

    @Size(min = 2, max = 30, message = "STAFF_NAME_INVALID")
    String staffName;

    Role role;
}
