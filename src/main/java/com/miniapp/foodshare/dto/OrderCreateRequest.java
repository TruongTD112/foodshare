package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderCreateRequest {
	Integer userId;
	Integer shopId;
	Integer productId;
	Integer quantity;
	// optional pickupTime minutes from now; if null, use default window
	Integer pickupInMinutes;
}
