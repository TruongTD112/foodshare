package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Integer> {
} 