package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class AdminProductDetailResponse {
    Integer id;
    Integer shopId;
    String shopName; // Tên cửa hàng
    String shopAddress; // Địa chỉ cửa hàng
    String shopPhone; // Số điện thoại cửa hàng
    Integer categoryId;
    String name;
    String description;
    BigDecimal price;
    BigDecimal originalPrice;
    String imageUrl;
    String detailImageUrl;
    Integer quantityAvailable;
    Integer quantityPending;
    String status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
