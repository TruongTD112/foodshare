package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class OrderResponse {
	Integer id;
	Integer userId;
	Integer shopId;
	String shopName;
	Integer productId;
	String productName;
	String productImage;
	Integer quantity;
	String status;
	LocalDateTime pickupTime;
	LocalDateTime expiresAt;
	BigDecimal unitPrice; // Giá trên từng sản phẩm tại thời điểm đặt
	BigDecimal totalPrice; // Tổng giá của đơn hàng
}
