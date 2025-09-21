package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class AdminShopListResponse {
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
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Integer totalProducts; // Tổng số sản phẩm trong cửa hàng
}
