package com.miniapp.foodshare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
@Schema(description = "Thông tin sản phẩm")
public class ProductResponse {
    
    @Schema(description = "ID sản phẩm", example = "1")
    Integer id;
    
    @Schema(description = "ID cửa hàng", example = "1")
    Integer shopId;
    
    @Schema(description = "ID danh mục", example = "1")
    Integer categoryId;
    
    @Schema(description = "Tên sản phẩm", example = "Bánh mì thịt nướng")
    String name;
    
    @Schema(description = "Mô tả sản phẩm", example = "Bánh mì thịt nướng thơm ngon")
    String description;
    
    @Schema(description = "Giá hiện tại", example = "25000.00")
    BigDecimal price;
    
    @Schema(description = "Giá gốc", example = "30000.00")
    BigDecimal originalPrice;
    
    @Schema(description = "URL hình ảnh", example = "https://example.com/image.jpg")
    String imageUrl;
    
    @Schema(description = "URL hình ảnh chi tiết", example = "https://example.com/detail.jpg")
    String detailImageUrl;
    
    @Schema(description = "Số lượng có sẵn", example = "50")
    Integer quantityAvailable;
    
    @Schema(description = "Số lượng đang chờ", example = "5")
    Integer quantityPending;
    
    @Schema(description = "Trạng thái", example = "1")
    String status;
    
    @Schema(description = "Thời gian tạo", example = "2024-01-15T10:00:00")
    LocalDateTime createdAt;
    
    @Schema(description = "Thời gian cập nhật", example = "2024-01-15T10:15:00")
    LocalDateTime updatedAt;
}
