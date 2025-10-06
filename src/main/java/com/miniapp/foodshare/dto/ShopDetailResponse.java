package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

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
} 