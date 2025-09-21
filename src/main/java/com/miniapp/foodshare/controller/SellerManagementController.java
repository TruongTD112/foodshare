package com.miniapp.foodshare.controller;

import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.*;
import com.miniapp.foodshare.service.ProductManagementService;
import com.miniapp.foodshare.service.ShopManagementService;
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

import java.util.List;

import static com.miniapp.foodshare.common.ErrorCode.INVALID_CREDENTIALS;

@Slf4j
@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
@Tag(name = "Seller Management", description = "API quản lý cho Seller - chỉ quản lý sản phẩm và cửa hàng của mình")
public class SellerManagementController {

    private final ProductManagementService productManagementService;
    private final ShopManagementService shopManagementService;

    /**
     * Lấy thông tin seller hiện tại
     */
    private Integer getCurrentSellerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Integer) {
            return (Integer) auth.getPrincipal();
        }
        return null;
    }

    // =====================================================
    // SHOP MANAGEMENT APIs
    // =====================================================

    /**
     * Tạo cửa hàng mới (chỉ seller mới tạo được)
     */
    @PostMapping("/shops")
    @Operation(
            summary = "Tạo cửa hàng mới",
            description = "Seller tạo cửa hàng mới cho mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo cửa hàng thành công",
                    content = @Content(schema = @Schema(implementation = ShopManagementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ShopManagementResponse> createShop(
            @Parameter(description = "Thông tin cửa hàng mới", required = true)
            @Valid @RequestBody CreateShopRequest request
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Seller create shop request: sellerId={}, name={}", sellerId, request.getName());
        return shopManagementService.createShop(request);
    }

    /**
     * Cập nhật cửa hàng của seller
     */
    @PutMapping("/shops/{shopId}")
    @Operation(
            summary = "Cập nhật cửa hàng",
            description = "Seller cập nhật thông tin cửa hàng của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = ShopManagementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập cửa hàng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy cửa hàng"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ShopManagementResponse> updateShop(
            @Parameter(description = "ID của cửa hàng", example = "1", required = true)
            @PathVariable Integer shopId,
            @Parameter(description = "Thông tin cập nhật", required = true)
            @Valid @RequestBody UpdateShopRequest request
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        // TODO: Kiểm tra seller có quyền truy cập shop này không
        // Cần implement logic kiểm tra ShopMember table

        log.info("Seller update shop request: sellerId={}, shopId={}", sellerId, shopId);
        return shopManagementService.updateShop(shopId, request);
    }

    /**
     * Lấy thông tin cửa hàng của seller
     */
    @GetMapping("/shops/{shopId}")
    @Operation(
            summary = "Lấy thông tin cửa hàng",
            description = "Seller xem thông tin cửa hàng của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
                    content = @Content(schema = @Schema(implementation = ShopManagementResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập cửa hàng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy cửa hàng"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ShopManagementResponse> getShop(
            @Parameter(description = "ID của cửa hàng", example = "1", required = true)
            @PathVariable Integer shopId
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        // TODO: Kiểm tra seller có quyền truy cập shop này không

        log.info("Seller get shop request: sellerId={}, shopId={}", sellerId, shopId);
        return shopManagementService.getShopById(shopId);
    }

    // =====================================================
    // PRODUCT MANAGEMENT APIs
    // =====================================================

    /**
     * Tạo sản phẩm mới cho shop của seller
     */
    @PostMapping("/products")
    @Operation(
            summary = "Tạo sản phẩm mới",
            description = "Seller tạo sản phẩm mới cho shop của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo sản phẩm thành công",
                    content = @Content(schema = @Schema(implementation = ProductManagementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập shop này"),
            @ApiResponse(responseCode = "404", description = "Shop không tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ProductManagementResponse> createProduct(
            @Parameter(description = "Thông tin sản phẩm mới", required = true)
            @Valid @RequestBody CreateProductRequest request
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        // TODO: Kiểm tra seller có quyền truy cập shop này không

        log.info("Seller create product request: sellerId={}, shopId={}, name={}",
                sellerId, request.getShopId(), request.getName());
        return productManagementService.createProduct(request);
    }

    /**
     * Cập nhật sản phẩm của seller
     */
    @PutMapping("/products/{productId}")
    @Operation(
            summary = "Cập nhật sản phẩm",
            description = "Seller cập nhật thông tin sản phẩm của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = ProductManagementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập sản phẩm này"),
            @ApiResponse(responseCode = "404", description = "Sản phẩm không tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ProductManagementResponse> updateProduct(
            @Parameter(description = "ID của sản phẩm", example = "1", required = true)
            @PathVariable Integer productId,
            @Parameter(description = "Thông tin cập nhật", required = true)
            @Valid @RequestBody UpdateProductRequest request
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        // TODO: Kiểm tra seller có quyền truy cập sản phẩm này không

        log.info("Seller update product request: sellerId={}, productId={}", sellerId, productId);
        return productManagementService.updateProduct(productId, request);
    }

    /**
     * Xóa sản phẩm của seller
     */
    @DeleteMapping("/products/{productId}")
    @Operation(
            summary = "Xóa sản phẩm",
            description = "Seller xóa sản phẩm của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập sản phẩm này"),
            @ApiResponse(responseCode = "404", description = "Sản phẩm không tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<Void> deleteProduct(
            @Parameter(description = "ID của sản phẩm", example = "1", required = true)
            @PathVariable Integer productId
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        // TODO: Kiểm tra seller có quyền truy cập sản phẩm này không

        log.info("Seller delete product request: sellerId={}, productId={}", sellerId, productId);
        return productManagementService.deleteProduct(productId);
    }

    /**
     * Lấy thông tin sản phẩm của seller
     */
    @GetMapping("/products/{productId}")
    @Operation(
            summary = "Lấy thông tin sản phẩm",
            description = "Seller xem thông tin sản phẩm của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
                    content = @Content(schema = @Schema(implementation = ProductManagementResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập sản phẩm này"),
            @ApiResponse(responseCode = "404", description = "Sản phẩm không tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ProductManagementResponse> getProduct(
            @Parameter(description = "ID của sản phẩm", example = "1", required = true)
            @PathVariable Integer productId
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        // TODO: Kiểm tra seller có quyền truy cập sản phẩm này không

        log.info("Seller get product request: sellerId={}, productId={}", sellerId, productId);
        return productManagementService.getProductById(productId);
    }

    /**
     * Lấy danh sách sản phẩm của shop
     */
    @GetMapping("/shops/{shopId}/products")
    @Operation(
            summary = "Lấy danh sách sản phẩm của shop",
            description = "Seller xem danh sách sản phẩm của shop của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = ProductManagementResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập shop này"),
            @ApiResponse(responseCode = "404", description = "Shop không tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<List<ProductManagementResponse>> getProductsByShop(
            @Parameter(description = "ID của shop", example = "1", required = true)
            @PathVariable Integer shopId
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        // TODO: Kiểm tra seller có quyền truy cập shop này không

        log.info("Seller get products by shop request: sellerId={}, shopId={}", sellerId, shopId);
        return productManagementService.getProductsByShopId(shopId);
    }

}
