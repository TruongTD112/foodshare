package com.miniapp.foodshare.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class UpdateProductRequest {
    Integer shopId;

    Integer categoryId;

    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    String name;

    String description;

    BigDecimal price;

    BigDecimal originalPrice;

    @Size(max = 255, message = "URL ảnh không được vượt quá 255 ký tự")
    String imageUrl;

    String detailImageUrl;

    Integer quantityAvailable;

    Integer quantityPending;

    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Trạng thái sản phẩm: 1 (ACTIVE), 0 (INACTIVE)",
        example = "1",
        allowableValues = {"1", "0"}
    )
    String status;
}