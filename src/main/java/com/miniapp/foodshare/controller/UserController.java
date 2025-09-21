package com.miniapp.foodshare.controller;

import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.UpdateUserRequest;
import com.miniapp.foodshare.dto.UserInfoResponse;
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
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API quản lý thông tin người dùng")
public class UserController {
    
    private final UserService userService;
    
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
    
}
