package com.isp392.repository;

import com.isp392.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByFullNameContainingIgnoreCase(String fullName);
    boolean existsByPhone(String phone);
    boolean existsByPhoneAndIdNot(String phone, long customerId);

}
