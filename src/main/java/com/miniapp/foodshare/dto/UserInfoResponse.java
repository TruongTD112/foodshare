package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserInfoResponse {
    Integer id;
    String name;
    String email;
    String phoneNumber;
    String profilePictureUrl;
}
