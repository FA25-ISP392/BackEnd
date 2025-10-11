package com.isp392.repository;

import com.isp392.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Integer> {
    boolean existsByDishName(String dishName);
    Optional<Dish> findByDishName(String dishName);
}
