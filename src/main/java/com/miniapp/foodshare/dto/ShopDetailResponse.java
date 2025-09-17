package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class ShopDetailResponse {
	Integer id;
	String name;
	String address;
	String phone;
	String imageUrl;
	Double latitude;
	Double longitude;
	String description;
	BigDecimal rating;
	String status;
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
	}
} 