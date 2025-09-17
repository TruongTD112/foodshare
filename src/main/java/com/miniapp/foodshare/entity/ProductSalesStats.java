package com.miniapp.foodshare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Bảng thống kê số lượng bán của sản phẩm
 * Được cập nhật mỗi khi có order completed
 */
@Entity
@Table(name = "Product_Sales_Stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSalesStats {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "product_id", nullable = false, unique = true)
	private Integer productId;

	@Column(name = "total_quantity_sold", nullable = false)
	private Integer totalQuantitySold;

	@Column(name = "total_orders", nullable = false)
	private Integer totalOrders;

	@Column(name = "last_sold_at")
	private LocalDateTime lastSoldAt;

	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private LocalDateTime updatedAt;
}
