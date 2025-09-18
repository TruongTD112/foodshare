//package com.miniapp.foodshare.service;
//
//import com.miniapp.foodshare.common.Result;
//import com.miniapp.foodshare.common.ErrorCode;
//import com.miniapp.foodshare.dto.SocialLoginRequest;
//import com.miniapp.foodshare.dto.SocialLoginResponse;
//import com.miniapp.foodshare.entity.CustomerUser;
//import com.miniapp.foodshare.repo.CustomerUserRepository;
//import com.miniapp.foodshare.security.JwtService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SocialAuthService {
//	private final CustomerUserRepository customerUserRepository;
//	private final JwtService jwtService;
//
//	@Transactional
//	public Result<SocialLoginResponse> login(SocialLoginRequest request) {
//		if (request == null || request.getProvider() == null || request.getToken() == null) {
//			log.warn("Invalid social login request: request={}, provider={}, token={}",
//				request != null, request != null ? request.getProvider() : "null", request != null ? request.getToken() : "null");
//			return Result.error(ErrorCode.INVALID_REQUEST, "Invalid request");
//		}
//		String provider = request.getProvider().trim().toLowerCase();
//
//		// TODO: Replace with real verification against Google/Facebook SDKs
//		// For now, treat token as providerId and demo email/name derivation
//		String providerId = request.getToken().trim();
//		String email = provider + "+" + providerId + "@example.com";
//		String name = provider.substring(0, 1).toUpperCase() + provider.substring(1) + " User";
//		String avatar = null;
//
//		Optional<CustomerUser> existing = customerUserRepository.findByProviderId(providerId);
//		CustomerUser user = existing.orElseGet(() -> CustomerUser.builder()
//			.name(name)
//			.email(email)
//			.provider(provider)
//			.providerId(providerId)
//			.profilePictureUrl(avatar)
//			.build());
//
//		// Update profile if needed
//		user.setName(name);
//		user.setEmail(email);
//		user.setProvider(provider);
//		user.setProviderId(providerId);
//		user.setProfilePictureUrl(avatar);
//
//		CustomerUser saved = customerUserRepository.save(user);
//
//		Map<String, Object> claims = new HashMap<>();
//		claims.put("uid", saved.getId());
//		claims.put("email", saved.getEmail());
//		claims.put("provider", saved.getProvider());
//		String jwt = jwtService.generateToken("user:" + saved.getId(), claims);
//
//		SocialLoginResponse response = SocialLoginResponse.builder()
//			.token(jwt)
//			.userId(saved.getId())
//			.name(saved.getName())
//			.email(saved.getEmail())
//			.provider(saved.getProvider())
//			.providerId(saved.getProviderId())
//			.profilePictureUrl(saved.getProfilePictureUrl())
//			.build();
//
//		log.info("Social login successful: userId={}, provider={}, isNewUser={}",
//			saved.getId(), provider, !existing.isPresent());
//		return Result.success(response);
//	}
//}
