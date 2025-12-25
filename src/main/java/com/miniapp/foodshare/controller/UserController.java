package com.miniapp.foodshare.controller;

import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.FirebaseTokenRequest;
import com.miniapp.foodshare.dto.FirebaseTokenResponse;
import com.miniapp.foodshare.dto.UpdateUserRequest;
import com.miniapp.foodshare.dto.UpdateUserLocationRequest;
import com.miniapp.foodshare.dto.UserInfoResponse;
import com.miniapp.foodshare.service.UserFirebaseTokenService;
import com.miniapp.foodshare.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API quản lý thông tin người dùng")
public class UserController {
    
    private final UserService userService;
    private final UserFirebaseTokenService userFirebaseTokenService;
    
    /**
     * Cập nhật thông tin người dùng
     * Cần xác thực: Lấy userId từ JWT token hoặc session
     */
    @PutMapping("/{userId}")
    @Operation(
        summary = "Cập nhật thông tin người dùng",
        description = "API cập nhật thông tin cá nhân của người dùng. Cần xác thực để lấy userId."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
            content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng"),
        @ApiResponse(responseCode = "409", description = "Email đã tồn tại"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<UserInfoResponse> updateUser(
            @Parameter(description = "ID của người dùng", example = "1", required = true)
            @PathVariable Integer userId,
            @Parameter(description = "Thông tin cập nhật", required = true)
            @Valid @RequestBody UpdateUserRequest request
    ) {
        log.info("Update user request: userId={}, name={}, email={}", 
                userId, request.getName(), request.getEmail());
        
        Result<UserInfoResponse> result = userService.updateUser(userId, request);
        
        if (result.isSuccess()) {
            log.info("User updated successfully: userId={}", userId);
        } else {
            log.warn("User update failed: userId={}, code={}, message={}", 
                    userId, result.getCode(), result.getMessage());
        }
        return result;
    }
    
    /**
     * Lấy thông tin người dùng
     * Cần xác thực: Lấy userId từ JWT token hoặc session
     */
    @GetMapping("/{userId}")
    @Operation(
        summary = "Lấy thông tin người dùng",
        description = "API lấy thông tin cá nhân của người dùng. Cần xác thực để lấy userId."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
            content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<UserInfoResponse> getUserInfo(
            @Parameter(description = "ID của người dùng", example = "1", required = true)
            @PathVariable Integer userId
    ) {
        log.info("Get user info request: userId={}", userId);
        
        Result<UserInfoResponse> result = userService.getUserInfo(userId);
        
        if (result.isSuccess()) {
            log.info("User info retrieved successfully: userId={}", userId);
        } else {
            log.warn("Get user info failed: userId={}, code={}, message={}", 
                    userId, result.getCode(), result.getMessage());
        }
        return result;
    }
    
    /**
     * Lưu Firebase token cho người dùng
     * Cần xác thực: Lấy userId từ JWT token sau khi đi qua filter
     */
    @PostMapping("/firebase-token")
    @Operation(
        summary = "Lưu Firebase token",
        description = "API lưu Firebase token của người dùng. Cần xác thực để lấy userId từ JWT token. Trạng thái token luôn là 1 (active)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lưu token thành công",
            content = @Content(schema = @Schema(implementation = FirebaseTokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<FirebaseTokenResponse> saveFirebaseToken(
            @Parameter(description = "Thông tin Firebase token", required = true)
            @Valid @RequestBody FirebaseTokenRequest request
    ) {
        // Lấy userId từ SecurityContext sau khi đã đi qua filter xác thực
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;
        
        if (userId == null) {
            log.warn("Unauthorized access attempt to save Firebase token");
            return Result.error(ErrorCode.UNAUTHORIZED, "Unauthorized");
        }
        
        log.info("Save Firebase token request: userId={}", userId);
        
        Result<FirebaseTokenResponse> result = userFirebaseTokenService.saveFirebaseToken(userId, request);
        
        if (result.isSuccess()) {
            log.info("Firebase token saved successfully: userId={}, tokenId={}", 
                    userId, result.getData().getId());
        } else {
            log.warn("Firebase token save failed: userId={}, code={}, message={}", 
                    userId, result.getCode(), result.getMessage());
        }
        return result;
    }
    
    /**
     * Cập nhật tọa độ vị trí của người dùng
     * Cần xác thực: Lấy userId từ JWT token hoặc session
     */
    @PutMapping("/{userId}/location")
    @Operation(
        summary = "Cập nhật tọa độ vị trí người dùng",
        description = "API cập nhật tọa độ vị trí (latitude, longitude) của người dùng. Chỉ cập nhật các giá trị không null. Cần xác thực để lấy userId."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
            content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<UserInfoResponse> updateUserLocation(
            @Parameter(description = "ID của người dùng", example = "1", required = true)
            @PathVariable Integer userId,
            @Parameter(description = "Thông tin tọa độ cập nhật", required = true)
            @Valid @RequestBody UpdateUserLocationRequest request
    ) {
        log.info("Update user location request: userId={}, latitude={}, longitude={}", 
                userId, request.getLatitude(), request.getLongitude());
        
        Result<UserInfoResponse> result = userService.updateUserLocation(userId, request);
        
        if (result.isSuccess()) {
            log.info("User location updated successfully: userId={}", userId);
        } else {
            log.warn("User location update failed: userId={}, code={}, message={}", 
                    userId, result.getCode(), result.getMessage());
        }
        return result;
    }
    
}
