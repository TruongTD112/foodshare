package com.miniapp.foodshare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@Schema(description = "Response sau khi lưu Firebase token")
public class FirebaseTokenResponse {
    
    @Schema(description = "ID của bản ghi", example = "1")
    Integer id;
    
    @Schema(description = "ID của người dùng", example = "123")
    Integer userId;
    
    @Schema(description = "Firebase token", example = "fGhJkLmNoPqRsTuVwXyZ123456789")
    String firebaseToken;
    
    @Schema(description = "Trạng thái token", example = "1")
    String status;
    
    @Schema(description = "Thời gian tạo")
    LocalDateTime createdAt;
    
    @Schema(description = "Thời gian cập nhật")
    LocalDateTime updatedAt;
}

