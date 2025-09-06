package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class ShopDetailResponse {
	Integer id;
	String name;
	String address;
	Double latitude;
	Double longitude;
	String description;
	BigDecimal rating;
	String status;
	LocalDateTime createdAt;
	LocalDateTime updatedAt;
	List<ProductItem> products;

	@Value
	@Builder
	public static class ProductItem {
		Integer id;
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
	}
} 