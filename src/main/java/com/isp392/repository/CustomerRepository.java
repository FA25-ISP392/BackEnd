package com.isp392.repository;

import com.isp392.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface CustomerRepository extends JpaRepository<Customer, Integer> {
//    @Query("SELECT c FROM Customer c WHERE c.account.username = :username")
//    Optional<Customer> findByUsername(@Param("username") String username);

    @Query("SELECT c FROM Customer c JOIN FETCH c.account a WHERE a.username = :username")
    Optional<Customer> findByUsername(@Param("username") String username);

    // Thêm method phân trang
    @Query("SELECT s FROM Customer s")
    Page<Customer> findAll(Pageable pageable);
}
