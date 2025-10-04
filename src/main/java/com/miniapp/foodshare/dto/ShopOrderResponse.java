package com.miniapp.foodshare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO response cho đơn hàng của shop
 * Bao gồm thông tin chi tiết về đơn hàng, sản phẩm và khách hàng
 */
@Value
@Builder
@Schema(description = "Thông tin đơn hàng của shop")
public class ShopOrderResponse {
    
    @Schema(description = "ID đơn hàng", example = "1")
    Integer id;
    
    @Schema(description = "ID khách hàng", example = "123")
    Integer userId;
    
    @Schema(description = "Tên khách hàng", example = "Nguyễn Văn A")
    String customerName;
    
    @Schema(description = "Email khách hàng", example = "customer@example.com")
    String customerEmail;
    
    @Schema(description = "Số điện thoại khách hàng", example = "0123456789")
    String customerPhone;
    
    @Schema(description = "ID shop", example = "1")
    Integer shopId;
    
    @Schema(description = "Tên shop", example = "Cửa hàng ABC")
    String shopName;
    
    @Schema(description = "ID sản phẩm", example = "1")
    Integer productId;
    
    @Schema(description = "Tên sản phẩm", example = "Bánh mì thịt nướng")
    String productName;
    
    @Schema(description = "Hình ảnh sản phẩm", example = "https://example.com/image.jpg")
    String productImage;
    
    @Schema(description = "Số lượng", example = "2")
    Integer quantity;
    
    @Schema(description = "Trạng thái đơn hàng", example = "1")
    String status;
    
    @Schema(description = "Mô tả trạng thái", example = "Đang chờ xác nhận")
    String statusDescription;
    
    @Schema(description = "Thời gian nhận hàng", example = "2024-01-15T10:30:00")
    LocalDateTime pickupTime;
    
    @Schema(description = "Thời gian hết hạn", example = "2024-01-15T10:45:00")
    LocalDateTime expiresAt;
    
    @Schema(description = "Giá đơn vị", example = "25000.00")
    BigDecimal unitPrice;
    
    @Schema(description = "Tổng giá", example = "50000.00")
    BigDecimal totalPrice;
    
    @Schema(description = "Thời gian tạo", example = "2024-01-15T10:00:00")
    LocalDateTime createdAt;
    
    @Schema(description = "Thời gian cập nhật", example = "2024-01-15T10:15:00")
    LocalDateTime updatedAt;
}
