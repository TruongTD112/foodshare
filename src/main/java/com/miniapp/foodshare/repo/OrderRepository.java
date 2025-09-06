package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
	List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);
	List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Integer userId, String status);
} 