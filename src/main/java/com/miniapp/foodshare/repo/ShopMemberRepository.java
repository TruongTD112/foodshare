package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.ShopMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopMemberRepository extends JpaRepository<ShopMember, Integer> {
} 