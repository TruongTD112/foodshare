package com.miniapp.foodshare.controller;

import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.SocialLoginRequest;
import com.miniapp.foodshare.dto.SocialLoginResponse;
import com.miniapp.foodshare.service.SocialAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
	private final SocialAuthService socialAuthService;

	@PostMapping("/social")
	public Result<SocialLoginResponse> socialLogin(@RequestBody SocialLoginRequest request) {
		Result<SocialLoginResponse> result = socialAuthService.login(request);
		if (result.isSuccess()) {
			log.info("Social login successful: userId={}, provider={}", 
				result.getData().getUserId(), result.getData().getProvider());
		} else {
			log.warn("Social login failed: code={}, message={}", result.getCode(), result.getMessage());
		}
		return result;
	}
}
