package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
} 