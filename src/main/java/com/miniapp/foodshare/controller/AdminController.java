package com.miniapp.foodshare.controller;

import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.*;
import com.miniapp.foodshare.service.AdminProductService;
import com.miniapp.foodshare.service.AdminShopService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "API quản lý toàn bộ hệ thống dành cho Admin")
public class AdminController {

    private final BackOfficeAuthService backOfficeAuthService;
    private final AdminShopService adminShopService;
    private final AdminProductService adminProductService;

    // ==================== ADMIN MANAGEMENT ====================

    /**
     * Admin tạo admin mới (chỉ admin mới có thể tạo)
     */
    @PostMapping("/create-admin")
    @Operation(
            summary = "Tạo admin mới",
            description = "Admin tạo admin mới"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo admin thành công",
                    content = @Content(schema = @Schema(implementation = BackOfficeAuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ admin mới có thể tạo admin"),
            @ApiResponse(responseCode = "409", description = "Email đã tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<BackOfficeAuthResponse> createAdmin(
            @Parameter(description = "Thông tin admin mới", required = true)
            @Valid @RequestBody CreateUserRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer adminId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;

        if (adminId == null) {
            log.warn("Unauthorized access to create admin");
            return Result.error(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập");
        }

        log.info("Create admin request: email={}, role={}, adminId={}", request.getEmail(), request.getRole(), adminId);

        Result<BackOfficeAuthResponse> result = backOfficeAuthService.createAdmin(request, adminId);

        if (result.isSuccess()) {
            log.info("Admin created successfully: userId={}, email={}, role={}, adminId={}",
                    result.getData().getId(), result.getData().getEmail(), result.getData().getRole(), adminId);
        } else {
            log.warn("Admin creation failed: email={}, adminId={}, code={}, message={}",
                    request.getEmail(), adminId, result.getCode(), result.getMessage());
        }
        return result;
    }

    // ==================== SHOP MANAGEMENT ====================

    /**
     * Lấy danh sách tất cả cửa hàng
     */
    @GetMapping("/shops")
    @Operation(
            summary = "Lấy danh sách tất cả cửa hàng",
            description = "Admin xem danh sách tất cả cửa hàng trong hệ thống"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = AdminShopListResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ admin mới có quyền truy cập"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<Page<AdminShopListResponse>> getAllShops(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer adminId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;

        if (adminId == null) {
            log.warn("Unauthorized access to get all shops");
            return Result.error(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập");
        }

        log.info("Get all shops request: adminId={}, page={}, size={}", adminId, pageable.getPageNumber(), pageable.getPageSize());

        Result<Page<AdminShopListResponse>> result = adminShopService.getAllShops(pageable);

        if (result.isSuccess()) {
            log.info("All shops retrieved successfully: adminId={}, totalElements={}",
                    adminId, result.getData().getTotalElements());
        } else {
            log.warn("Get all shops failed: adminId={}, code={}, message={}",
                    adminId, result.getCode(), result.getMessage());
        }
        return result;
    }

    /**
     * Lấy chi tiết cửa hàng theo ID
     */
    @GetMapping("/shops/{shopId}")
    @Operation(
            summary = "Lấy chi tiết cửa hàng",
            description = "Admin xem chi tiết thông tin cửa hàng theo ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công",
                    content = @Content(schema = @Schema(implementation = AdminShopDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ admin mới có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy cửa hàng"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<AdminShopDetailResponse> getShopDetail(
            @Parameter(description = "ID của cửa hàng", required = true)
            @PathVariable Integer shopId
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer adminId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;

        if (adminId == null) {
            log.warn("Unauthorized access to get shop detail");
            return Result.error(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập");
        }

        log.info("Get shop detail request: adminId={}, shopId={}", adminId, shopId);

        Result<AdminShopDetailResponse> result = adminShopService.getShopDetail(shopId);

        if (result.isSuccess()) {
            log.info("Shop detail retrieved successfully: adminId={}, shopId={}", adminId, shopId);
        } else {
            log.warn("Get shop detail failed: adminId={}, shopId={}, code={}, message={}",
                    adminId, shopId, result.getCode(), result.getMessage());
        }
        return result;
    }

    /**
     * Cập nhật cửa hàng
     */
    @PutMapping("/shops/{shopId}")
    @Operation(
            summary = "Cập nhật cửa hàng",
            description = "Admin cập nhật thông tin cửa hàng"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = AdminShopDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ admin mới có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy cửa hàng"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<AdminShopDetailResponse> updateShop(
            @Parameter(description = "ID của cửa hàng", required = true)
            @PathVariable Integer shopId,
            @Parameter(description = "Thông tin cập nhật", required = true)
            @Valid @RequestBody UpdateShopRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer adminId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;

        if (adminId == null) {
            log.warn("Unauthorized access to update shop");
            return Result.error(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập");
        }

        log.info("Update shop request: adminId={}, shopId={}", adminId, shopId);

        Result<AdminShopDetailResponse> result = adminShopService.updateShop(shopId, request);

        if (result.isSuccess()) {
            log.info("Shop updated successfully: adminId={}, shopId={}", adminId, shopId);
        } else {
            log.warn("Update shop failed: adminId={}, shopId={}, code={}, message={}",
                    adminId, shopId, result.getCode(), result.getMessage());
        }
        return result;
    }

    /**
     * Xóa cửa hàng
     */
    @DeleteMapping("/shops/{shopId}")
    @Operation(
            summary = "Xóa cửa hàng",
            description = "Admin xóa cửa hàng"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ admin mới có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy cửa hàng"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<String> deleteShop(
            @Parameter(description = "ID của cửa hàng", required = true)
            @PathVariable Integer shopId
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer adminId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;

        if (adminId == null) {
            log.warn("Unauthorized access to delete shop");
            return Result.error(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập");
        }

        log.info("Delete shop request: adminId={}, shopId={}", adminId, shopId);

        Result<String> result = adminShopService.deleteShop(shopId);

        if (result.isSuccess()) {
            log.info("Shop deleted successfully: adminId={}, shopId={}", adminId, shopId);
        } else {
            log.warn("Delete shop failed: adminId={}, shopId={}, code={}, message={}",
                    adminId, shopId, result.getCode(), result.getMessage());
        }
        return result;
    }

    // ==================== PRODUCT MANAGEMENT ====================

    /**
     * Lấy danh sách tất cả sản phẩm
     */
    @GetMapping("/products")
    @Operation(
            summary = "Lấy danh sách tất cả sản phẩm",
            description = "Admin xem danh sách tất cả sản phẩm trong hệ thống"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = AdminProductListResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ admin mới có quyền truy cập"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<Page<AdminProductListResponse>> getAllProducts(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer adminId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;

        if (adminId == null) {
            log.warn("Unauthorized access to get all products");
            return Result.error(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập");
        }

        log.info("Get all products request: adminId={}, page={}, size={}", adminId, pageable.getPageNumber(), pageable.getPageSize());

        Result<Page<AdminProductListResponse>> result = adminProductService.getAllProducts(pageable);

        if (result.isSuccess()) {
            log.info("All products retrieved successfully: adminId={}, totalElements={}",
                    adminId, result.getData().getTotalElements());
        } else {
            log.warn("Get all products failed: adminId={}, code={}, message={}",
                    adminId, result.getCode(), result.getMessage());
        }
        return result;
    }

    /**
     * Lấy chi tiết sản phẩm theo ID
     */
    @GetMapping("/products/{productId}")
    @Operation(
            summary = "Lấy chi tiết sản phẩm",
            description = "Admin xem chi tiết thông tin sản phẩm theo ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công",
                    content = @Content(schema = @Schema(implementation = AdminProductDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ admin mới có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<AdminProductDetailResponse> getProductDetail(
            @Parameter(description = "ID của sản phẩm", required = true)
            @PathVariable Integer productId
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer adminId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;

        if (adminId == null) {
            log.warn("Unauthorized access to get product detail");
            return Result.error(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập");
        }

        log.info("Get product detail request: adminId={}, productId={}", adminId, productId);

        Result<AdminProductDetailResponse> result = adminProductService.getProductDetail(productId);

        if (result.isSuccess()) {
            log.info("Product detail retrieved successfully: adminId={}, productId={}", adminId, productId);
        } else {
            log.warn("Get product detail failed: adminId={}, productId={}, code={}, message={}",
                    adminId, productId, result.getCode(), result.getMessage());
        }
        return result;
    }

    /**
     * Lấy danh sách sản phẩm trong cửa hàng
     */
    @GetMapping("/products/shop/{shopId}")
    @Operation(
            summary = "Lấy danh sách sản phẩm trong cửa hàng",
            description = "Admin xem danh sách sản phẩm của một cửa hàng cụ thể"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = AdminProductListResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ admin mới có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy cửa hàng"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<Page<AdminProductListResponse>> getProductsByShop(
            @Parameter(description = "ID của cửa hàng", required = true)
            @PathVariable Integer shopId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer adminId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;

        if (adminId == null) {
            log.warn("Unauthorized access to get products by shop");
            return Result.error(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập");
        }

        log.info("Get products by shop request: adminId={}, shopId={}, page={}, size={}",
                adminId, shopId, pageable.getPageNumber(), pageable.getPageSize());

        Result<Page<AdminProductListResponse>> result = adminProductService.getProductsByShop(shopId, pageable);

        if (result.isSuccess()) {
            log.info("Products by shop retrieved successfully: adminId={}, shopId={}, totalElements={}",
                    adminId, shopId, result.getData().getTotalElements());
        } else {
            log.warn("Get products by shop failed: adminId={}, shopId={}, code={}, message={}",
                    adminId, shopId, result.getCode(), result.getMessage());
        }
        return result;
    }

    /**
     * Cập nhật sản phẩm
     */
    @PutMapping("/products/{productId}")
    @Operation(
            summary = "Cập nhật sản phẩm",
            description = "Admin cập nhật thông tin sản phẩm"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = AdminProductDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ admin mới có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<AdminProductDetailResponse> updateProduct(
            @Parameter(description = "ID của sản phẩm", required = true)
            @PathVariable Integer productId,
            @Parameter(description = "Thông tin cập nhật", required = true)
            @Valid @RequestBody UpdateProductRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer adminId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;

        if (adminId == null) {
            log.warn("Unauthorized access to update product");
            return Result.error(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập");
        }

        log.info("Update product request: adminId={}, productId={}", adminId, productId);

        Result<AdminProductDetailResponse> result = adminProductService.updateProduct(productId, request);

        if (result.isSuccess()) {
            log.info("Product updated successfully: adminId={}, productId={}", adminId, productId);
        } else {
            log.warn("Update product failed: adminId={}, productId={}, code={}, message={}",
                    adminId, productId, result.getCode(), result.getMessage());
        }
        return result;
    }

    /**
     * Xóa sản phẩm
     */
    @DeleteMapping("/products/{productId}")
    @Operation(
            summary = "Xóa sản phẩm",
            description = "Admin xóa sản phẩm"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ admin mới có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<String> deleteProduct(
            @Parameter(description = "ID của sản phẩm", required = true)
            @PathVariable Integer productId
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer adminId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;

        if (adminId == null) {
            log.warn("Unauthorized access to delete product");
            return Result.error(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập");
        }

        log.info("Delete product request: adminId={}, productId={}", adminId, productId);

        Result<String> result = adminProductService.deleteProduct(productId);

        if (result.isSuccess()) {
            log.info("Product deleted successfully: adminId={}, productId={}", adminId, productId);
        } else {
            log.warn("Delete product failed: adminId={}, productId={}, code={}, message={}",
                    adminId, productId, result.getCode(), result.getMessage());
        }
        return result;
    }

    // ==================== ADMIN MANAGEMENT (CRUD) ====================

    /**
     * Cập nhật admin
     */
    @PutMapping("/admins/{adminId}")
    @Operation(
            summary = "Cập nhật admin",
            description = "Admin cập nhật thông tin admin khác"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = BackOfficeAuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ admin mới có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy admin"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<BackOfficeAuthResponse> updateAdmin(
            @Parameter(description = "ID của admin", required = true)
            @PathVariable Integer adminId,
            @Parameter(description = "Thông tin cập nhật", required = true)
            @Valid @RequestBody UpdateAdminRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer currentAdminId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;

        if (currentAdminId == null) {
            log.warn("Unauthorized access to update admin");
            return Result.error(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập");
        }

        log.info("Update admin request: currentAdminId={}, targetAdminId={}", currentAdminId, adminId);

        Result<BackOfficeAuthResponse> result = backOfficeAuthService.updateAdmin(adminId, request, currentAdminId);

        if (result.isSuccess()) {
            log.info("Admin updated successfully: currentAdminId={}, targetAdminId={}", currentAdminId, adminId);
        } else {
            log.warn("Update admin failed: currentAdminId={}, targetAdminId={}, code={}, message={}",
                    currentAdminId, adminId, result.getCode(), result.getMessage());
        }
        return result;
    }

    /**
     * Xóa admin
     */
    @DeleteMapping("/admins/{adminId}")
    @Operation(
            summary = "Xóa admin",
            description = "Admin xóa admin khác"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ admin mới có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy admin"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<String> deleteAdmin(
            @Parameter(description = "ID của admin", required = true)
            @PathVariable Integer adminId
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer currentAdminId = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;

        if (currentAdminId == null) {
            log.warn("Unauthorized access to delete admin");
            return Result.error(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập");
        }

        log.info("Delete admin request: currentAdminId={}, targetAdminId={}", currentAdminId, adminId);

        Result<String> result = backOfficeAuthService.deleteAdmin(adminId, currentAdminId);

        if (result.isSuccess()) {
            log.info("Admin deleted successfully: currentAdminId={}, targetAdminId={}", currentAdminId, adminId);
        } else {
            log.warn("Delete admin failed: currentAdminId={}, targetAdminId={}, code={}, message={}",
                    currentAdminId, adminId, result.getCode(), result.getMessage());
        }
        return result;
    }
}
