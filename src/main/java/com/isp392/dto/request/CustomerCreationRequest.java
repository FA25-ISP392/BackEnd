package com.isp392.dto.request;

import com.isp392.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerCreationRequest {
    @NotBlank(message = "NOT_BLANKED")
    @Size(min = 3, max = 30, message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "NOT_BLANKED")
    @Size(min = 8, max = 30, message = "PASSWORD_INVALID")
    String password;

    @NotBlank(message = "NOT_BLANKED")
    @Pattern(regexp = "^(0|\\+84)\\d{9,10}$", message = "PHONE_INVALID")
    String customerPhone;

    @NotBlank(message = "NOT_BLANKED")
    @Size(min = 3, message = "CUSTOMER_FULLNAME_TOO_SHORT")
    String customerName;

    @Email(message = "STAFF_EMAIL_INVALID")
    String staffEmail;

    Role role;

    Double height;

    Double weight;

    Boolean sex;

    Integer age;

    Integer portion;
}
