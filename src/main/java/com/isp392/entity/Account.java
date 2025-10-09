package com.isp392.entity;

import com.isp392.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int accountId;

    @Column(name = "username", nullable = false, length = 50,unique = true)
    String username;

    @Column(name = "password",nullable = false, length = 100)
    String password;

    @Column(name = "full_name", length = 100)
    String fullName;

    @Column(name = "email", length = 100,unique = true)
    String email;

    @Column(name = "phone", length = 15,unique = true)
    String phone;

    @Column(name = "dob")
    LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    Role role;
}
