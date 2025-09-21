package com.miniapp.foodshare.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.miniapp.foodshare.common.UserRole;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BackOfficeAuthResponse {
    Integer id;
    String name;
    String email;
    UserRole role;
    String accessToken;
    String tokenType;
    LocalDateTime expiresAt;
    String message;
}
