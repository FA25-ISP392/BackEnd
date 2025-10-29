package com.isp392.repository;

import com.isp392.entity.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List; // ⭐ Thêm import List

@Repository
public interface ToppingRepository extends JpaRepository<Topping,Integer> {
    boolean existsByname(String name);

    // ⭐ SỬA LẠI: Tìm các topping chứa tên (sử dụng derived query method)
    List<Topping> findByNameContaining(String name); // Đổi tên và kiểu trả về

    // ⭐ MỚI (hoặc có thể đã có sẵn từ JpaRepository): Tìm tất cả và phân trang
    Page<Topping> findAll(Pageable pageable);
}