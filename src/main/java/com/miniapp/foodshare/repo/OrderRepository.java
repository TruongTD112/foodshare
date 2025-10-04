package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
	List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);
	List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Integer userId, String status);
	
	// Query đơn hàng theo shopId với phân trang
	Page<Order> findByShopIdOrderByCreatedAtDesc(Integer shopId, Pageable pageable);
	
	// Query tất cả đơn hàng với phân trang (đã có sẵn từ JpaRepository.findAll(Pageable))
	
	// Query đơn hàng với filter theo ngày, shopId và status
	@Query("SELECT o FROM Order o WHERE " +
		   "(:shopId IS NULL OR o.shopId = :shopId) AND " +
		   "(:status IS NULL OR o.status = :status) AND " +
		   "(:fromDate IS NULL OR o.createdAt >= :fromDate) AND " +
		   "(:toDate IS NULL OR o.createdAt <= :toDate)")
	Page<Order> findOrdersWithFilters(@Param("shopId") Integer shopId, 
									  @Param("status") String status,
									  @Param("fromDate") LocalDateTime fromDate, 
									  @Param("toDate") LocalDateTime toDate, 
									  Pageable pageable);
} 