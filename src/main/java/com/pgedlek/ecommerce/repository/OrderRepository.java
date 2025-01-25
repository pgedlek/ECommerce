package com.pgedlek.ecommerce.repository;

import com.pgedlek.ecommerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
