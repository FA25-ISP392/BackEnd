package com.isp392.repository;

import com.isp392.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Integer> {
    boolean existsByDishName(String dishName);

    @Query("SELECT DISTINCT d FROM Dish d LEFT JOIN FETCH d.dishToppings dt LEFT JOIN FETCH dt.topping WHERE d.isAvailable = true")
    List<Dish> findAllWithToppings();


    @Query("SELECT d FROM Dish d LEFT JOIN FETCH d.dishToppings dt LEFT JOIN FETCH dt.topping WHERE d.dishId = :dishId AND d.isAvailable = true")
    Optional<Dish> findByIdWithToppings(@Param("dishId") int dishId);
}
