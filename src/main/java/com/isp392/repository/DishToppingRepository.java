package com.isp392.repository;

import com.isp392.entity.DishTopping;
import com.isp392.entity.DishToppingId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishToppingRepository extends JpaRepository<DishTopping, DishToppingId> {}
