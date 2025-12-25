package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.FirebaseTokenRequest;
import com.miniapp.foodshare.dto.FirebaseTokenResponse;
import com.miniapp.foodshare.entity.UserFirebaseToken;
import com.miniapp.foodshare.repo.UserFirebaseTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFirebaseTokenService {
    
    private final UserFirebaseTokenRepository userFirebaseTokenRepository;
    
    /**
     * Lưu Firebase token cho người dùng
     * 
     * @param userId ID của người dùng (lấy từ JWT token sau khi xác thực)
     * @param request Thông tin Firebase token
     * @return Thông tin token đã lưu
     */
    @Transactional
    public Result<FirebaseTokenResponse> saveFirebaseToken(Integer userId, FirebaseTokenRequest request) {
        try {
            // Tạo entity mới
            UserFirebaseToken token = UserFirebaseToken.builder()
                    .userId(userId)
                    .firebaseToken(request.getFirebaseToken())
                    .status("1") // Luôn là 1 (active) theo yêu cầu
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            // Lưu vào database
            UserFirebaseToken savedToken = userFirebaseTokenRepository.save(token);
            
            // Tạo response
            FirebaseTokenResponse response = FirebaseTokenResponse.builder()
                    .id(savedToken.getId())
                    .userId(savedToken.getUserId())
                    .firebaseToken(savedToken.getFirebaseToken())
                    .status(savedToken.getStatus())
                    .createdAt(savedToken.getCreatedAt())
                    .updatedAt(savedToken.getUpdatedAt())
                    .build();
            
            log.info("Firebase token saved successfully: id={}, userId={}", savedToken.getId(), userId);
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("Error saving Firebase token for userId={}", userId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to save Firebase token: " + e.getMessage());
        }
    }
}

