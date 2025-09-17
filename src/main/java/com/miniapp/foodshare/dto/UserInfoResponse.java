package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class UserInfoResponse {
    Integer id;
    String name;
    String email;
    String provider;
    String providerId;
    String profilePictureUrl;
    String phoneNumber;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
