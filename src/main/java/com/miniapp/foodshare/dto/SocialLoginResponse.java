package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SocialLoginResponse {
	String token; // JWT
	Integer userId;
	String name;
	String email;
	String provider;
	String providerId;
	String profilePictureUrl;
}
