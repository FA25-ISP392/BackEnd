package com.isp392.repository;

import com.isp392.entity.Ingredent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredentRepository extends JpaRepository<Ingredent,Long> {
    boolean existsByname(String name);
    Ingredent findById(int id);
}
