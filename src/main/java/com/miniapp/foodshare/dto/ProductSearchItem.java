package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ProductSearchItem {
	Integer productId;
	String name;
	BigDecimal price;
	BigDecimal originalPrice;
	BigDecimal discountPercentage; // Tính toán từ originalPrice và price
	String imageUrl;
	String detailImageUrl; // Ảnh chi tiết sản phẩm
	Integer shopId;
	String shopName;
	BigDecimal shopLatitude;
	BigDecimal shopLongitude;
	Double distanceKm;
	Integer totalOrders; // Số lượng đơn đã đặt (cho tất cả API)
} 