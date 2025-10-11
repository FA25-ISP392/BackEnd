package com.isp392.repository;

import com.isp392.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {

    @Query("SELECT s FROM Staff s WHERE s.account.username = :username")
    Optional<Staff> findByUsername(@Param("username") String username);

    // Thêm method phân trang
    @Query("SELECT s FROM Staff s")
    Page<Staff> findAll(Pageable pageable);
}
