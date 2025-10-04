package com.isp392.repository;

import com.isp392.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient,Long> {
    boolean existsByname(String name);
    Ingredient findById(int id);
}
