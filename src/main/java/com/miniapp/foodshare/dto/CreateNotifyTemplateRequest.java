package com.miniapp.foodshare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotifyTemplateRequest {

    @NotNull(message = "shopId is required")
    @Positive(message = "shopId must be positive")
    private Integer shopId;

    @NotNull(message = "productId is required")
    @Positive(message = "productId must be positive")
    private Integer productId;

    @Positive(message = "radius must be positive")
    private Double radius; // Optional, default 5.0 km
}

