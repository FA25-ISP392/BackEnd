package com.isp392.dto.request;

import com.isp392.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountCreationRequest {

    @NotBlank(message = "NOT_BLANKED")
    @Size(min = 3, max = 30, message = "SIZE_INVALID")
    String username;

    @NotBlank(message = "NOT_BLANKED")
    @Size(min = 8, max = 30, message = "SIZE_INVALID")
    String password;

    @NotBlank(message = "NOT_BLANKED")
    @Size(min=2, max = 50, message = "SIZE_INVALID")
    String fullName;

    @Email(message = "EMAIL_INVALID")
    String email;

    @Pattern(regexp = "^(0)[1-9]\\d{8}$", message = "PHONE_INVALID")
    String phone;

    @Past(message = "DOB_MUST_BE_IN_THE_PAST")
    LocalDate dob;

    @NotNull(message = "ROLE_REQUIRED")
    Role role;
}
