package com.miniapp.foodshare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
@Schema(description = "Request để lưu Firebase token")
public class FirebaseTokenRequest {
    
    @NotBlank(message = "Firebase token không được để trống")
    @Schema(description = "Firebase token từ thiết bị", example = "fGhJkLmNoPqRsTuVwXyZ123456789")
    String firebaseToken;
}

