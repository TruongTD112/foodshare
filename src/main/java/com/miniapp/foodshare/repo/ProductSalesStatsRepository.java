package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.ProductSalesStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ProductSalesStatsRepository extends JpaRepository<ProductSalesStats, Integer> {
	
	/**
	 * Tìm thống kê bán hàng theo productId
	 */
	Optional<ProductSalesStats> findByProductId(Integer productId);
	
	/**
	 * Lấy danh sách sản phẩm bán chạy nhất theo tổng số lượng đã bán
	 * Join với Product để lấy thông tin sản phẩm
	 */
	@Query("SELECT p FROM Product p " +
		   "INNER JOIN ProductSalesStats s ON p.id = s.productId " +
		   "WHERE p.status = :status " +
		   "ORDER BY s.totalQuantitySold DESC")
	Page<ProductSalesStats> findPopularProducts(@Param("status") String status, Pageable pageable);
	
	/**
	 * Cập nhật số lượng bán khi có order completed
	 * Sử dụng UPSERT (INSERT ... ON DUPLICATE KEY UPDATE)
	 */
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO ProductSalesStats (product_id, total_quantity_sold, total_orders, last_sold_at) " +
		   "VALUES (:productId, :quantity, 1, NOW()) " +
		   "ON DUPLICATE KEY UPDATE " +
		   "total_quantity_sold = total_quantity_sold + :quantity, " +
		   "total_orders = total_orders + 1, " +
		   "last_sold_at = NOW()", nativeQuery = true)
	void updateSalesStats(@Param("productId") Integer productId, @Param("quantity") Integer quantity);
}
