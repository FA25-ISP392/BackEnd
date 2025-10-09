package com.isp392.repository;

import com.isp392.entity.Account;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByUsername(String username);
    boolean existsByUsername(String username);

    boolean existsByEmail(@Email(message = "EMAIL_INVALID") String email);
}
