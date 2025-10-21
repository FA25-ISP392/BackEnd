package com.isp392.dto.response;

import com.isp392.entity.Account;
import com.isp392.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponse {
    Integer accountId;
    String username;
    String fullName;
    String email;
    String phone;
    LocalDate dob;
    Role role;
    Boolean isVerified;

    public AccountResponse(Account account) {
    }
}