package com.isp392.dto.request;

import com.isp392.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountCreationRequest {

    @NotBlank(message = "USERNAME_REQUIRED")
    @Size(min = 3, max = 30, message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 8, max = 30, message = "PASSWORD_INVALID")
    String password;

    Role role;
}
