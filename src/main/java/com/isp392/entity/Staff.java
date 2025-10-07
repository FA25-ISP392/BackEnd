package com.isp392.entity;

import com.isp392.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "Staff")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int staffId;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    Account account;

    @Column(name = "staffName", nullable = false, length = 100)
    String staffName;

    @Column(name = "staffEmail", nullable = true, length = 100, unique = true)
    String staffEmail;

    @Column(name = "staffPhone", nullable = false, length = 15, unique = true)
    String staffPhone;


}
