package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class AdminShopDetailResponse {
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
    List<AdminProductItem> products; // Danh sách sản phẩm trong cửa hàng

    @Value
    @Builder
    public static class AdminProductItem {
        Integer id;
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
}
