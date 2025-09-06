package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
	List<Product> findByNameContainingIgnoreCase(String name);
	Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
	Page<Product> findByStatus(String status, Pageable pageable);
	Page<Product> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);
	List<Product> findByShopId(Integer shopId);
}
