package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class OrderCreateRequest {
	Integer userId;
	Integer shopId;
	Integer productId;
	Integer quantity;
	LocalDateTime pickupTime; // Ngày và giờ đặt hàng
	BigDecimal unitPrice; // Giá trên từng sản phẩm tại thời điểm đặt
	BigDecimal totalPrice; // Tổng giá của đơn hàng
}
