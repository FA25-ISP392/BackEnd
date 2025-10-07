package com.isp392.dto.request;

import com.isp392.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AccountUpdateRequest {
    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 8, max = 30, message = "PASSWORD_INVALID")
    String password;

    Role role;
}
