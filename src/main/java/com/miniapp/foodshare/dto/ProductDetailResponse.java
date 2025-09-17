package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ProductDetailResponse {
	Integer id;
	Integer shopId;
	Integer categoryId;
	String name;
	String description;
	BigDecimal price;
	String imageUrl;
	String detailImageUrl; // Ảnh chi tiết sản phẩm
	Integer quantityAvailable;
	Integer quantityPending;
	String status;
	ShopInfo shop;

	@Value
	@Builder
	public static class ShopInfo {
		Integer id;
		String name;
		String address;
		String phone;
		String imageUrl;
		BigDecimal latitude;
		BigDecimal longitude;
		String description;
		BigDecimal rating;
		String status;
	}
} 