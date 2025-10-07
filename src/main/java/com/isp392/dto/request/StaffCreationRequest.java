package com.isp392.dto.request;

import com.isp392.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffCreationRequest {
    @NotBlank(message = "NOT_BLANKED")
    @Size(min = 3, max = 30, message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "NOT_BLANKED")
    @Size(min = 8, max = 30, message = "PASSWORD_INVALID")
    String password;

    @NotBlank(message = "NOT_BLANKED")
    @Size(min = 2, max = 30, message = "STAFF_NAME_INVALID")
    String staffName;

    @Pattern(regexp = "^[0-9]{9,11}$", message = "PHONE_INVALID")
    String staffPhone;

    @Email(message = "STAFF_EMAIL_INVALID")
    String staffEmail;

    Role role;

}
