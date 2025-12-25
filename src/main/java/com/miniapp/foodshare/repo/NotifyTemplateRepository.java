package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.NotifyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotifyTemplateRepository extends JpaRepository<NotifyTemplate, Integer> {

    /**
     * Đếm số bản ghi notify template của shop trong ngày (từ startOfDay đến endOfDay)
     */
    @Query("SELECT COUNT(nt) FROM NotifyTemplate nt WHERE nt.shopId = :shopId " +
           "AND nt.createdAt >= :startOfDay AND nt.createdAt < :endOfDay")
    long countByShopIdAndDateRange(@Param("shopId") Integer shopId, 
                                   @Param("startOfDay") LocalDateTime startOfDay,
                                   @Param("endOfDay") LocalDateTime endOfDay);
}

