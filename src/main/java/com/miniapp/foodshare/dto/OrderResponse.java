package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class OrderResponse {
	Integer id;
	Integer userId;
	Integer shopId;
	Integer productId;
	Integer quantity;
	String status;
	LocalDateTime pickupTime;
	LocalDateTime expiresAt;
	LocalDateTime createdAt;
	LocalDateTime updatedAt;
}
