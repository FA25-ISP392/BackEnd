package com.isp392.repository;

import com.isp392.entity.Dish;
import com.isp392.enums.Category; // ✅ Thêm import
import com.isp392.enums.DishType; // ✅ Thêm import
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Integer> {
    boolean existsByDishName(String dishName);

    // ✅ SỬA LẠI: Thêm tham số filter
    @Query("SELECT DISTINCT d FROM Dish d " +
            "LEFT JOIN FETCH d.dishToppings dt LEFT JOIN FETCH dt.topping " +
            "WHERE d.isAvailable = true " +
            "AND (:category IS NULL OR d.category = :category) " +
            "AND (:type IS NULL OR d.type = :type)")
    List<Dish> findAllWithToppings(@Param("category") Category category, @Param("type") DishType type);


    // ✅ SỬA LẠI: Thêm tham số filter
    @Query(value = "SELECT DISTINCT d FROM Dish d " +
            "LEFT JOIN FETCH d.dishToppings dt " +
            "LEFT JOIN FETCH dt.topping " +
            "WHERE d.isAvailable = true " +
            "AND (:category IS NULL OR d.category = :category) " +
            "AND (:type IS NULL OR d.type = :type)",
            countQuery = "SELECT COUNT(DISTINCT d) FROM Dish d " +
                    "WHERE d.isAvailable = true " +
                    "AND (:category IS NULL OR d.category = :category) " +
                    "AND (:type IS NULL OR d.type = :type)")
    Page<Dish> findAllWithToppings(Pageable pageable, @Param("category") Category category, @Param("type") DishType type);


    @Query("SELECT d FROM Dish d LEFT JOIN FETCH d.dishToppings dt LEFT JOIN FETCH dt.topping WHERE d.dishId = :dishId AND d.isAvailable = true")
    Optional<Dish> findByIdWithToppings(@Param("dishId") int dishId);
}