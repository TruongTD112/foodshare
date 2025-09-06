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
	String imageUrl;
	Integer shopId;
	String shopName;
	Double shopLatitude;
	Double shopLongitude;
	Double distanceKm;
} 