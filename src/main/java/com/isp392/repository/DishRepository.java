package com.isp392.repository;

import com.isp392.entity.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // ✅ Chỉ cần import này
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// ❌ Xóa 2 dòng import thừa
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Integer> {
    boolean existsByDishName(String dishName);

    // Phương thức cũ (dùng cho getAllDishes)
    @Query("SELECT DISTINCT d FROM Dish d LEFT JOIN FETCH d.dishToppings dt LEFT JOIN FETCH dt.topping WHERE d.isAvailable = true")
    List<Dish> findAllWithToppings();

    // Phương thức mới cho phân trang (dùng cho getAllDishesPaginated)
    @Query(value = "SELECT DISTINCT d FROM Dish d " +
            "LEFT JOIN FETCH d.dishToppings dt " +
            "LEFT JOIN FETCH dt.topping " +
            "WHERE d.isAvailable = true",
            countQuery = "SELECT COUNT(DISTINCT d) FROM Dish d WHERE d.isAvailable = true")
    Page<Dish> findAllWithToppings(Pageable pageable);

    // Phương thức cho getById
    @Query("SELECT d FROM Dish d LEFT JOIN FETCH d.dishToppings dt LEFT JOIN FETCH dt.topping WHERE d.dishId = :dishId AND d.isAvailable = true")
    Optional<Dish> findByIdWithToppings(@Param("dishId") int dishId);
}