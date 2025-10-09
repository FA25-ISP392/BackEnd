package com.isp392.dto.request;

import com.isp392.enums.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AccountUpdateRequest {

    @Size(min = 8, max = 30, message = "PASSWORD_INVALID")
    String password;

    @Size(min = 2, max = 50, message = "FULLNAME_INVALID")
    String fullName;

    @Email(message = "EMAIL_INVALID")
    String email;

    @Pattern(regexp = "^(0)[1-9]\\d{8}$", message = "PHONE_INVALID")
    String phone;

    @Past(message = "DOB_MUST_BE_IN_THE_PAST")
    LocalDate dob;

    Role role;
}
