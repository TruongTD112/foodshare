package com.miniapp.foodshare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@Schema(description = "Response feedback")
public class FeedbackResponse {
    
    @Schema(description = "ID feedback", example = "1")
    Integer id;
    
    @Schema(description = "Nội dung feedback", example = "Ứng dụng rất tốt, giao diện thân thiện")
    String content;
    
    @Schema(description = "ID người dùng", example = "123")
    Integer userId;
    
    @Schema(description = "Thời gian tạo", example = "2024-01-15T10:00:00")
    LocalDateTime createdAt;
}
