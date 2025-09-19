package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.dto.UpdateUserRequest;
import com.miniapp.foodshare.dto.UserInfoResponse;
import com.miniapp.foodshare.entity.CustomerUser;
import com.miniapp.foodshare.repo.CustomerUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .build();
        
        log.info("User info retrieved successfully: userId={}, name={}", userId, user.getName());
        return Result.success(response);
    }
}
