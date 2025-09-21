package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ProductManagementResponse {
    Integer id;
    Integer shopId;
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
}
