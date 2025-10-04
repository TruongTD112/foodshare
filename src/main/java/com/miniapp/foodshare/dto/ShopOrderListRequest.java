package com.miniapp.foodshare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * DTO request cho việc lấy danh sách đơn hàng của shop
 */
@Value
@Builder
@Schema(description = "Tham số lấy danh sách đơn hàng của shop")
public class ShopOrderListRequest {
    
    @Schema(description = "ID shop (bắt buộc cho seller, không cần cho admin)", example = "1")
    Integer shopId;
    
    @Schema(description = "Trạng thái đơn hàng", example = "1", allowableValues = {"1", "2", "3", "4"})
    String status;
    
    @Schema(description = "Từ ngày (yyyy-MM-ddTHH:mm:ss)", example = "2024-01-01T00:00:00")
    LocalDateTime fromDate;
    
    @Schema(description = "Đến ngày (yyyy-MM-ddTHH:mm:ss)", example = "2024-01-31T23:59:59")
    LocalDateTime toDate;
    
    @Schema(description = "Số trang (bắt đầu từ 0)", example = "0", minimum = "0")
    @Min(value = 0, message = "Số trang phải >= 0")
    Integer page;
    
    @Schema(description = "Số lượng mỗi trang", example = "20", minimum = "1", maximum = "100")
    @Min(value = 1, message = "Số lượng mỗi trang phải >= 1")
    @Max(value = 100, message = "Số lượng mỗi trang phải <= 100")
    Integer size;
    
    @Schema(description = "Sắp xếp theo", example = "createdAt", allowableValues = {"createdAt", "pickupTime", "totalPrice"})
    String sortBy;
    
    @Schema(description = "Thứ tự sắp xếp", example = "desc", allowableValues = {"asc", "desc"})
    String sortDirection;
}
