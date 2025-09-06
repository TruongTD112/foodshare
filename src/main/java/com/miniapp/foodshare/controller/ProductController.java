package com.miniapp.foodshare.controller;

import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.common.Constants;
import com.miniapp.foodshare.dto.ProductDetailResponse;
import com.miniapp.foodshare.dto.ProductSearchItem;
import com.miniapp.foodshare.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * REST endpoints for product search.
 * Supports case-insensitive name search, optional distance filtering by customer location,
 * optional price range (minPrice/maxPrice), price sorting (asc/desc), and pagination.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Tag(name = "Products", description = "Product management and search APIs")
public class ProductController {
	private final ProductService productService;

	/**
	 * Search products with pagination support.
	 *
	 * @param name case-insensitive substring to match on product name
	 * @param latitude customer latitude in decimal degrees
	 * @param longitude customer longitude in decimal degrees
	 * @param maxDistanceKm optional maximum distance in kilometers; items farther are excluded
	 * @param minPrice optional minimum price filter
	 * @param maxPrice optional maximum price filter
	 * @param priceSort optional price sorting: "asc" or "desc"
	 * @param page page number (0-based, default: 0)
	 * @param size page size (default: 20, max: 100)
	 * @return paginated list of products with shop info and distance (if coords are provided)
	 */
	@GetMapping
	@Operation(
		summary = "Search products",
		description = "Search products with various filters including name, location, price range, and pagination support"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Products found successfully",
			content = @Content(schema = @Schema(implementation = ProductSearchItem.class))),
		@ApiResponse(responseCode = "400", description = "Invalid request parameters"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public Result<Page<ProductSearchItem>> searchProducts(
			@Parameter(description = "Product name search query (case-insensitive)", example = "pizza")
			@RequestParam(name = "q", required = false) String name,
			@Parameter(description = "Customer latitude in decimal degrees", example = "10.762622")
			@RequestParam(name = "lat", required = false) Double latitude,
			@Parameter(description = "Customer longitude in decimal degrees", example = "106.660172")
			@RequestParam(name = "lon", required = false) Double longitude,
			@Parameter(description = "Maximum distance in kilometers", example = "5.0")
			@RequestParam(name = "maxDistanceKm", required = false) Double maxDistanceKm,
			@Parameter(description = "Minimum price filter", example = "10000")
			@RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
			@Parameter(description = "Maximum price filter", example = "50000")
			@RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
			@Parameter(description = "Price sorting: 'asc' or 'desc'", example = "asc")
			@RequestParam(name = "priceSort", required = false) String priceSort,
			@Parameter(description = "Page number (0-based)", example = "0")
			@RequestParam(name = "page", defaultValue = "0") int page,
			@Parameter(description = "Page size (max: 100)", example = "20")
			@RequestParam(name = "size", defaultValue = "20") int size
	) {
		// Use constants for default values
		if (size == 0) {
			size = Constants.Pagination.DEFAULT_PAGE_SIZE;
		}
		if (page < 0) {
			page = Constants.Pagination.DEFAULT_PAGE_NUMBER;
		}
		
		Result<Page<ProductSearchItem>> result = productService.searchProducts(name, latitude, longitude, maxDistanceKm, minPrice, maxPrice, priceSort, page, size);
		if (result.isSuccess()) {
			Page<ProductSearchItem> data = result.getData();
			log.info("Product search completed successfully: name={}, found={}, hasCoords={}, page={}, size={}, totalElements={}, totalPages={}", 
				name, data.getContent().size(), latitude != null && longitude != null, page, size, data.getTotalElements(), data.getTotalPages());
		} else {
			log.warn("Product search failed: code={}, message={}", result.getCode(), result.getMessage());
		}
		return result;
	}

	/**
	 * Get product detail by id, including owning shop info.
	 */
	@GetMapping("/{id}")
	@Operation(
		summary = "Get product detail",
		description = "Retrieve detailed information about a specific product including shop details"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Product found successfully",
			content = @Content(schema = @Schema(implementation = ProductDetailResponse.class))),
		@ApiResponse(responseCode = "404", description = "Product not found"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public Result<ProductDetailResponse> getProductDetail(
		@Parameter(description = "Product ID", example = "1")
		@PathVariable("id") Integer id
	) {
		Result<ProductDetailResponse> result = productService.getProductDetail(id);
		if (result.isSuccess()) {
			log.info("Product detail retrieved successfully: productId={}, shopId={}", id, result.getData().getShopId());
		} else {
			log.warn("Product detail retrieval failed: productId={}, code={}, message={}", id, result.getCode(), result.getMessage());
		}
		return result;
	}
} 