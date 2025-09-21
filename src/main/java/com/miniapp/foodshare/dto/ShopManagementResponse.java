package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ShopManagementResponse {
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
}
