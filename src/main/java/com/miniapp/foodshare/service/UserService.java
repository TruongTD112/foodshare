package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.dto.UpdateUserRequest;
import com.miniapp.foodshare.dto.UpdateUserLocationRequest;
import com.miniapp.foodshare.dto.UserInfoResponse;
import com.miniapp.foodshare.entity.CustomerUser;
import com.miniapp.foodshare.repo.CustomerUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final CustomerUserRepository customerUserRepository;
    
    /**
     * Cập nhật thông tin người dùng
     * 
     * @param userId ID của người dùng
     * @param request Thông tin cập nhật
     * @return Thông tin người dùng sau khi cập nhật
     */
    @Transactional
    public Result<UserInfoResponse> updateUser(Integer userId, UpdateUserRequest request) {
        // Tìm user theo ID
        CustomerUser user = customerUserRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found: userId={}", userId);
            return Result.error(ErrorCode.USER_NOT_FOUND, "User not found: " + userId);
        }
        
        // Kiểm tra email đã tồn tại chưa (trừ user hiện tại)
        if (!user.getEmail().equals(request.getEmail()) && 
            customerUserRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            log.warn("Email already exists: email={}, userId={}", request.getEmail(), userId);
            return Result.error(ErrorCode.EMAIL_ALREADY_EXISTS, "Email already exists: " + request.getEmail());
        }
        
        // Cập nhật thông tin
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setProfilePictureUrl(request.getProfilePictureUrl());
        
        // Lưu vào database
        CustomerUser savedUser = customerUserRepository.save(user);
        
        // Tạo response
        UserInfoResponse response = UserInfoResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .profilePictureUrl(savedUser.getProfilePictureUrl())
                .latitude(savedUser.getLatitude())
                .longitude(savedUser.getLongitude())
                .build();
        
        log.info("User updated successfully: userId={}, name={}, email={}", 
                userId, savedUser.getName(), savedUser.getEmail());
        
        return Result.success(response);
    }
    
    /**
     * Lấy thông tin người dùng theo ID
     * 
     * @param userId ID của người dùng
     * @return Thông tin người dùng
     */
    @Transactional(readOnly = true)
    public Result<UserInfoResponse> getUserInfo(Integer userId) {
        CustomerUser user = customerUserRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found: userId={}", userId);
            return Result.error(ErrorCode.USER_NOT_FOUND, "User not found: " + userId);
        }
        
        UserInfoResponse response = UserInfoResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .build();
        
        log.info("User info retrieved successfully: userId={}, name={}", userId, user.getName());
        return Result.success(response);
    }
    
    /**
     * Cập nhật tọa độ vị trí của người dùng
     * 
     * @param userId ID của người dùng
     * @param request Thông tin tọa độ cập nhật (latitude, longitude)
     * @return Thông tin người dùng sau khi cập nhật
     */
    @Transactional
    public Result<UserInfoResponse> updateUserLocation(Integer userId, UpdateUserLocationRequest request) {
        // Tìm user theo ID
        CustomerUser user = customerUserRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found: userId={}", userId);
            return Result.error(ErrorCode.USER_NOT_FOUND, "User not found: " + userId);
        }

        boolean isUpdate = false;
        // Cập nhật latitude nếu không null
        if (request.getLatitude() != null) {
            user.setLatitude(request.getLatitude());
            log.info("Updating latitude for userId={}: {}", userId, request.getLatitude());
            isUpdate = true;
        }
        
        // Cập nhật longitude nếu không null
        if (request.getLongitude() != null) {
            user.setLongitude(request.getLongitude());
            log.info("Updating longitude for userId={}: {}", userId, request.getLongitude());
            isUpdate = true;
        }

        // Cập nhật updateAt
        if (isUpdate) {
            user.setUpdatedAt(LocalDateTime.now());
        }

        // Lưu vào database
        CustomerUser savedUser = customerUserRepository.save(user);
        
        // Tạo response với tọa độ
        UserInfoResponse response = UserInfoResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .profilePictureUrl(savedUser.getProfilePictureUrl())
                .latitude(savedUser.getLatitude())
                .longitude(savedUser.getLongitude())
                .build();
        
        log.info("User location updated successfully: userId={}, latitude={}, longitude={}", 
                userId, savedUser.getLatitude(), savedUser.getLongitude());
        
        return Result.success(response);
    }
}
