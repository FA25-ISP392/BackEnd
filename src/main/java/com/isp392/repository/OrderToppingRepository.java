package com.isp392.repository;

import com.isp392.entity.OrderTopping;
import com.isp392.entity.OrderToppingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderToppingRepository extends JpaRepository<OrderTopping, OrderToppingId> {

}
