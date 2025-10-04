package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.ShopMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopMemberRepository extends JpaRepository<ShopMember, Integer> {
    
    /**
     * Tìm shop member theo backofficeUserId và shopId
     */
    List<ShopMember> findByBackofficeUserIdAndShopId(Integer backofficeUserId, Integer shopId);
} 