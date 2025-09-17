package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
	List<Product> findByNameContainingIgnoreCase(String name);
	Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
	Page<Product> findByStatus(String status, Pageable pageable);
	Page<Product> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);
	List<Product> findByShopId(Integer shopId);
	
	/**
	 * Tìm kiếm sản phẩm có giảm giá theo mức giảm giá giảm dần
	 * Chỉ lấy sản phẩm có originalPrice > price (có giảm giá)
	 */
	@Query("SELECT p FROM Product p WHERE p.status = :status AND p.originalPrice IS NOT NULL AND p.originalPrice > p.price ORDER BY (p.originalPrice - p.price) DESC")
	Page<Product> findDiscountedProductsByDiscountAmount(@Param("status") String status, Pageable pageable);
	
	/**
	 * Tìm kiếm sản phẩm bán chạy nhất dựa trên bảng ProductSalesStats
	 * Hiệu quả hơn so với tính toán từ Order mỗi lần
	 */
	@Query("SELECT p FROM Product p " +
		   "INNER JOIN ProductSalesStats s ON p.id = s.productId " +
		   "WHERE p.status = :status " +
		   "ORDER BY s.totalQuantitySold DESC")
	Page<Product> findPopularProducts(@Param("status") String status, Pageable pageable);
}
