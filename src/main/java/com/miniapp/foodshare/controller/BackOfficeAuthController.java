package com.miniapp.foodshare.controller;

import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.BackOfficeAuthResponse;
import com.miniapp.foodshare.dto.SellerRegisterRequest;
import com.miniapp.foodshare.dto.UnifiedLoginRequest;
import com.miniapp.foodshare.service.BackOfficeAuthService;
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
@RequestMapping("/api/back-office/auth/")
@RequiredArgsConstructor
@Tag(name = "BackOffice Authentication", description = "API đăng nhập cho Admin và Seller")
public class BackOfficeAuthController {

    private final BackOfficeAuthService backOfficeAuthService;

    /**
     * Đăng nhập (Admin/Seller)
     */
    @PostMapping("/login")
    @Operation(
        summary = "Đăng nhập",
        description = "Đăng nhập cho cả Admin và Seller, trả về role tương ứng"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
            content = @Content(schema = @Schema(implementation = BackOfficeAuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Email hoặc mật khẩu không đúng"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<BackOfficeAuthResponse> login(
            @Parameter(description = "Thông tin đăng nhập", required = true)
            @Valid @RequestBody UnifiedLoginRequest request
    ) {
        log.info("Login request: email={}", request.getEmail());
        
        Result<BackOfficeAuthResponse> result = backOfficeAuthService.login(request);
        
        if (result.isSuccess()) {
            log.info("User logged in successfully: userId={}, email={}, role={}", 
                    result.getData().getId(), result.getData().getEmail(), result.getData().getRole());
        } else {
            log.warn("Login failed: email={}, code={}, message={}", 
                    request.getEmail(), result.getCode(), result.getMessage());
        }
        return result;
    }

    /**
     * Seller đăng ký
     */
    @PostMapping("/register")
    @Operation(
        summary = "Đăng ký Seller",
        description = "Seller đăng ký tài khoản mới"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đăng ký thành công",
            content = @Content(schema = @Schema(implementation = BackOfficeAuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "409", description = "Email đã tồn tại"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<BackOfficeAuthResponse> registerSeller(
            @Parameter(description = "Thông tin đăng ký seller", required = true)
            @Valid @RequestBody SellerRegisterRequest request
    ) {
        log.info("Seller register request: email={}", request.getEmail());
        
        Result<BackOfficeAuthResponse> result = backOfficeAuthService.registerSeller(request);
        
        if (result.isSuccess()) {
            log.info("Seller registered successfully: userId={}, email={}, role={}", 
                    result.getData().getId(), result.getData().getEmail(), result.getData().getRole());
        } else {
            log.warn("Seller registration failed: email={}, code={}, message={}", 
                    request.getEmail(), result.getCode(), result.getMessage());
        }
        return result;
    }

    /**
     * Lấy thông tin user hiện tại
     */
    @GetMapping("/me")
    @Operation(
        summary = "Lấy thông tin user hiện tại",
        description = "Lấy thông tin của user đang đăng nhập (Admin hoặc Seller)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
            content = @Content(schema = @Schema(implementation = BackOfficeAuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy user"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<BackOfficeAuthResponse> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;
        
        if (userId == null) {
            log.warn("Unauthorized access to get current user");
            return Result.error(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập");
        }

        log.info("Get current user request: userId={}", userId);
        
        Result<BackOfficeAuthResponse> result = backOfficeAuthService.getCurrentUser(userId);
        
        if (result.isSuccess()) {
            log.info("Current user info retrieved successfully: userId={}, email={}, role={}", 
                    result.getData().getId(), result.getData().getEmail(), result.getData().getRole());
        } else {
            log.warn("Get current user failed: userId={}, code={}, message={}", 
                    userId, result.getCode(), result.getMessage());
        }
        return result;
    }

    /**
     * Đăng xuất
     */
    @PostMapping("/logout")
    @Operation(
        summary = "Đăng xuất",
        description = "Đăng xuất khỏi hệ thống"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đăng xuất thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<String> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;
        
        if (userId == null) {
            log.warn("Unauthorized access to logout");
            return Result.error(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập");
        }

        log.info("Logout request: userId={}", userId);
        
        // Clear security context
        SecurityContextHolder.clearContext();
        
        log.info("User logged out successfully: userId={}", userId);
        return Result.success("Đăng xuất thành công");
    }

}
