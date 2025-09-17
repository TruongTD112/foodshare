package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
	Integer quantityAvailable;
	Integer quantityPending;
	String status;
	LocalDateTime createdAt;
	LocalDateTime updatedAt;
	ShopInfo shop;

	@Value
	@Builder
	public static class ShopInfo {
		Integer id;
		String name;
		String address;
		BigDecimal latitude;
		BigDecimal longitude;
		String description;
		BigDecimal rating;
		String status;
	}
} 