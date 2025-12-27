package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class NotifyTemplateResponse {
    Integer id;
    String status;
    Double radius;
    Integer shopId;
    Integer productId;
    String title;
    String content;
    String metadata;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

