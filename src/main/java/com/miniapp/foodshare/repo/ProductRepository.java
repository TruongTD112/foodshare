package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
	List<Product> findByNameContainingIgnoreCase(String name);
	Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
	Page<Product> findByStatus(String status, Pageable pageable);
	Page<Product> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);
	List<Product> findByShopId(Integer shopId);
	Page<Product> findByShopId(Integer shopId, Pageable pageable);
	List<Product> findByShopIdAndStatus(Integer shopId, String status);
	Page<Product> findByShopIdAndStatus(Integer shopId, String status, Pageable pageable);
	
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
	
	/**
	 * Tìm kiếm sản phẩm gần nhất với tính toán khoảng cách trực tiếp trong database
	 * Sử dụng công thức Haversine để tính khoảng cách chính xác
	 * Chỉ lấy sản phẩm và cửa hàng active, sắp xếp theo khoảng cách tăng dần
	 */
	@Query(value = """
		SELECT 
		    p.id,
		    p.name,
		    p.price,
		    p.original_price,
		    p.image_url,
		    p.shop_id,
		    s.name as shop_name,
		    s.address as shop_address,
		    s.phone as shop_phone,
		    s.image_url as shop_image_url,
		    s.latitude as shop_latitude,
		    s.longitude as shop_longitude,
		    s.description as shop_description,
		    s.rating as shop_rating,
		    s.status as shop_status,
		    ROUND(
		        6371 * ACOS(
		            COS(RADIANS(:latitude)) * 
		            COS(RADIANS(s.latitude)) * 
		            COS(RADIANS(s.longitude) - RADIANS(:longitude)) + 
		            SIN(RADIANS(:latitude)) * 
		            SIN(RADIANS(s.latitude))
		        ), 0
		    ) as distance_km
		FROM Product p
		INNER JOIN Shop s ON p.shop_id = s.id
		WHERE p.status = '1'
		  AND s.status = '1'
		  AND s.latitude IS NOT NULL 
		  AND s.longitude IS NOT NULL
		  AND (
		    6371 * ACOS(
		        COS(RADIANS(:latitude)) * 
		        COS(RADIANS(s.latitude)) * 
		        COS(RADIANS(s.longitude) - RADIANS(:longitude)) + 
		        SIN(RADIANS(:latitude)) * 
		        SIN(RADIANS(s.latitude))
		    )
		  ) <= :maxDistanceKm
		ORDER BY distance_km ASC
		""", nativeQuery = true)
	List<Object[]> findNearbyProductsWithDistance(
		@Param("latitude") Double latitude,
		@Param("longitude") Double longitude,
		@Param("maxDistanceKm") Double maxDistanceKm,
		Pageable pageable
	);
	
	/**
	 * Tìm kiếm sản phẩm tổng quát với thứ tự ưu tiên: name → discount → distance
	 * Sử dụng native query để tối ưu hiệu suất và sắp xếp thông minh
	 */
	@Query(value = """
		SELECT 
		    p.id,
		    p.name,
		    p.price,
		    p.original_price,
		    p.image_url,
		    p.shop_id,
		    s.name as shop_name,
		    s.address as shop_address,
		    s.phone as shop_phone,
		    s.image_url as shop_image_url,
		    s.latitude as shop_latitude,
		    s.longitude as shop_longitude,
		    s.description as shop_description,
		    s.rating as shop_rating,
		    s.status as shop_status,
		    -- Tính khoảng cách (nếu có tọa độ)
		    CASE 
		        WHEN :latitude IS NOT NULL AND :longitude IS NOT NULL 
		             AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL
		        THEN ROUND(
		            6371 * ACOS(
		                COS(RADIANS(:latitude)) * 
		                COS(RADIANS(s.latitude)) * 
		                COS(RADIANS(s.longitude) - RADIANS(:longitude)) + 
		                SIN(RADIANS(:latitude)) * 
		                SIN(RADIANS(s.latitude))
		            ), 0
		        )
		        ELSE NULL
		    END as distance_km,
		    -- Tính điểm ưu tiên: name → discount → distance
		    (
		        -- Ưu tiên 1: Tên sản phẩm (1000 điểm nếu match)
		        CASE 
		            WHEN :nameQuery IS NOT NULL AND :nameQuery != '' 
		                 AND LOWER(p.name) LIKE LOWER(CONCAT('%', :nameQuery, '%'))
		            THEN 1000
		            ELSE 0
		        END +
		        -- Ưu tiên 2: Có giảm giá (500 điểm)
		        CASE 
		            WHEN p.original_price IS NOT NULL 
		                 AND p.price IS NOT NULL 
		                 AND p.original_price > p.price
		            THEN 500
		            ELSE 0
		        END +
		        -- Ưu tiên 3: Khoảng cách gần (1000 - distance_km, tối đa 1000 điểm)
		        CASE 
		            WHEN :latitude IS NOT NULL AND :longitude IS NOT NULL 
		                 AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL
		            THEN GREATEST(0, 1000 - ROUND(
		                6371 * ACOS(
		                    COS(RADIANS(:latitude)) * 
		                    COS(RADIANS(s.latitude)) * 
		                    COS(RADIANS(s.longitude) - RADIANS(:longitude)) + 
		                    SIN(RADIANS(:latitude)) * 
		                    SIN(RADIANS(s.latitude))
		                ), 0
		            ))
		            ELSE 0
		        END
		    ) as priority_score
		FROM Product p
		INNER JOIN Shop s ON p.shop_id = s.id
		WHERE p.status = '1'
		  AND s.status = '1'
		  AND s.latitude IS NOT NULL 
		  AND s.longitude IS NOT NULL
		  -- Lọc theo tên (nếu có)
		  AND (:nameQuery IS NULL OR :nameQuery = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :nameQuery, '%')))
		  -- Lọc theo khoảng cách (nếu có)
		  AND (:latitude IS NULL OR :longitude IS NULL OR :maxDistanceKm IS NULL OR (
		    6371 * ACOS(
		        COS(RADIANS(:latitude)) * 
		        COS(RADIANS(s.latitude)) * 
		        COS(RADIANS(s.longitude) - RADIANS(:longitude)) + 
		        SIN(RADIANS(:latitude)) * 
		        SIN(RADIANS(s.latitude))
		    )
		  ) <= :maxDistanceKm)
		  -- Lọc theo giá (nếu có)
		  AND (:minPrice IS NULL OR p.price >= :minPrice)
		  AND (:maxPrice IS NULL OR p.price <= :maxPrice)
		ORDER BY 
		    priority_score DESC,
		    distance_km ASC
		""", nativeQuery = true)
	List<Object[]> searchProductsWithPriority(
		@Param("nameQuery") String nameQuery,
		@Param("latitude") Double latitude,
		@Param("longitude") Double longitude,
		@Param("maxDistanceKm") Double maxDistanceKm,
		@Param("minPrice") BigDecimal minPrice,
		@Param("maxPrice") BigDecimal maxPrice,
		Pageable pageable
	);
}
