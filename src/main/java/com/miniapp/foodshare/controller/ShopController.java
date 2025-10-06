package com.miniapp.foodshare.controller;

import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.PagedResult;
import com.miniapp.foodshare.dto.ProductResponse;
import com.miniapp.foodshare.dto.ShopDetailResponse;
import com.miniapp.foodshare.service.ProductService;
import com.miniapp.foodshare.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Shop", description = "API quản lý cửa hàng cho khách hàng")

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/shops")
public class ShopController {
	private final ShopService shopService;
	private final ProductService productService;

	@GetMapping("/{id}")
	@Operation(
			summary = "Lấy thông tin cửa hàng",
			description = "Khách hàng xem thông tin chi tiết cửa hàng"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
					content = @Content(schema = @Schema(implementation = ShopDetailResponse.class))),
			@ApiResponse(responseCode = "404", description = "Không tìm thấy cửa hàng"),
			@ApiResponse(responseCode = "500", description = "Lỗi server")
	})
	public Result<ShopDetailResponse> getShop(
			@Parameter(description = "ID của cửa hàng", example = "1", required = true)
			@PathVariable("id") Integer id
	) {
		Result<ShopDetailResponse> result = shopService.getShopDetail(id);
		if (result.isSuccess()) {
			log.info("Shop detail retrieved successfully: shopId={}", id);
		} else {
			log.warn("Shop detail retrieval failed: shopId={}, code={}, message={}", id, result.getCode(), result.getMessage());
		}
		return result;
	}

	@GetMapping("/{id}/products")
	@Operation(
			summary = "Lấy danh sách sản phẩm của cửa hàng",
			description = "Khách hàng xem danh sách sản phẩm của cửa hàng với phân trang"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
					content = @Content(schema = @Schema(implementation = ProductResponse.class))),
			@ApiResponse(responseCode = "404", description = "Không tìm thấy cửa hàng"),
			@ApiResponse(responseCode = "500", description = "Lỗi server")
	})
	public Result<PagedResult<ProductResponse>> getShopProducts(
			@Parameter(description = "ID của cửa hàng", example = "1", required = true)
			@PathVariable("id") Integer shopId,
			@Parameter(description = "Trang hiện tại (bắt đầu từ 0)")
			@RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
			@Parameter(description = "Kích thước trang")
			@RequestParam(name = "size", required = false, defaultValue = "20") Integer size,
			@Parameter(description = "Trường sắp xếp")
			@RequestParam(name = "sortBy", required = false, defaultValue = "createdAt") String sortBy,
			@Parameter(description = "Chiều sắp xếp: asc|desc")
			@RequestParam(name = "sortDirection", required = false, defaultValue = "desc") String sortDirection
	) {
		log.info("Get shop products request: shopId={}, page={}, size={}, sortBy={}, sortDirection={}", 
				shopId, page, size, sortBy, sortDirection);
		
		Result<PagedResult<ProductResponse>> result = productService.getProductsByShop(shopId, page, size, sortBy, sortDirection);
		
		if (result.isSuccess()) {
			log.info("Shop products retrieved successfully: shopId={}, totalElements={}", 
					shopId, result.getData().getTotalElements());
		} else {
			log.warn("Shop products retrieval failed: shopId={}, code={}, message={}", 
					shopId, result.getCode(), result.getMessage());
		}
		return result;
	}
} 