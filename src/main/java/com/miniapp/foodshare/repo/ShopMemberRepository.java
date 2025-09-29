package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.ShopMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopMemberRepository extends JpaRepository<ShopMember, Integer> {
    
    /**
     * Lấy danh sách shop ID mà seller có quyền truy cập
     */
    @Query("SELECT sm.shopId FROM ShopMember sm WHERE sm.backofficeUserId = :sellerId")
    List<Integer> findShopIdsBySellerId(@Param("sellerId") Integer sellerId);
    
    /**
     * Kiểm tra seller có quyền truy cập shop không
     */
    boolean existsByBackofficeUserIdAndShopId(Integer sellerId, Integer shopId);
} 