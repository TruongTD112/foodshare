package com.miniapp.foodshare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
@Schema(description = "Request tạo feedback")
public class FeedbackRequest {
    
    @NotBlank(message = "Nội dung feedback không được để trống")
    @Size(max = 2000, message = "Nội dung feedback không được vượt quá 2000 ký tự")
    @Schema(description = "Nội dung feedback", example = "Ứng dụng rất tốt, giao diện thân thiện", required = true)
    String content;
    
    @Schema(description = "ID người dùng (tùy chọn)", example = "123")
    Integer userId;
}
