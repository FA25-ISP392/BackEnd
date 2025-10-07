package com.isp392.entity;

import com.isp392.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    Role role; // CUSTOMER, STAFF, MANAGER, ADMIN, CHEF
}
