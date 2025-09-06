package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.Constants;
import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.ProductDetailResponse;
import com.miniapp.foodshare.dto.ProductSearchItem;
import com.miniapp.foodshare.entity.Product;
import com.miniapp.foodshare.entity.Shop;
import com.miniapp.foodshare.repo.ProductRepository;
import com.miniapp.foodshare.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides product search and filtering logic.
 * Computes distances using the Haversine formula (kilometers) when coordinates are provided.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    /**
     * Searches products by optional name (case-insensitive substring), optional price range,
     * and optional distance from a customer location with pagination support.
     * <p>
     * Behavior:
     * - If name is provided, performs case-insensitive contains on product name.
     * - If minPrice/maxPrice provided, filters products within the price range.
     * - If latitude/longitude provided, computes distance to shop and sorts by distance ascending.
     * - If maxDistanceKm provided, excludes items farther than the specified distance.
     * - If priceSort is provided ("asc" or "desc"), sorts by price after distance ordering.
     * - Only includes products with active status and shops with status = "1".
     * - Supports pagination with page number and size.
     *
     * @param nameQuery     optional name query for case-insensitive search
     * @param latitude      optional customer latitude (decimal degrees)
     * @param longitude     optional customer longitude (decimal degrees)
     * @param maxDistanceKm optional max distance (km) to include
     * @param minPrice      optional minimum price
     * @param maxPrice      optional maximum price
     * @param priceSort     optional price sort direction: "asc" or "desc"
     * @param page          page number (0-based, default: 0)
     * @param size          page size (default: 20)
     * @return paginated list of search results enriched with shop details and distance when applicable
     */
    @Transactional(readOnly = true)
    public Result<Page<ProductSearchItem>> searchProducts(
            String nameQuery,
            Double latitude,
            Double longitude,
            Double maxDistanceKm,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String priceSort,
            int page,
            int size
    ) {
        // Validate pagination parameters
        if (page < Constants.Pagination.DEFAULT_PAGE_NUMBER) {
            log.warn("Invalid page number: page={}", page);
            return Result.error(ErrorCode.INVALID_PAGE_NUMBER, "Page number must be >= 0");
        }
        if (size <= 0 || size > Constants.Pagination.MAX_PAGE_SIZE) {
            log.warn("Invalid page size: size={}", size);
            return Result.error(ErrorCode.INVALID_PAGE_SIZE, "Page size must be between 1 and 100");
        }

        final boolean hasCoords = latitude != null && longitude != null;

        // Create pageable for database query
        Pageable pageable = PageRequest.of(page, size);

        // Get products with database-level filtering
        Page<Product> productPage;
        if (nameQuery == null || nameQuery.isBlank()) {
            // Get all active products with pagination
            productPage = productRepository.findByStatus(Constants.ProductStatus.ACTIVE, pageable);
        } else {
            // Get products by name and status with pagination
            productPage = productRepository.findByNameContainingIgnoreCaseAndStatus(nameQuery.trim(), Constants.ProductStatus.ACTIVE, pageable);
        }

        // Apply price filters at database level if possible
        List<Product> products = productPage.getContent();
        if (minPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() != null && p.getPrice().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
        }
        if (maxPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() != null && p.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }

        if (products.isEmpty()) {
            log.info("No products found for search: nameQuery={}, minPrice={}, maxPrice={}, page={}, size={}",
                    nameQuery, minPrice, maxPrice, page, size);
            return Result.success(Page.empty(pageable));
        }

        Set<Integer> shopIds = products.stream()
                .map(Product::getShopId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, Shop> shopById = new HashMap<>();
        if (!shopIds.isEmpty()) {
            shopById = shopRepository.findAllById(shopIds).stream()
                    .collect(Collectors.toMap(Shop::getId, s -> s));
        }

        List<ProductSearchItem> items = new ArrayList<>(products.size());
        for (Product product : products) {
            Shop shop = product.getShopId() == null ? null : shopById.get(product.getShopId());
            // Only active shops (status == "1")
            if (shop == null || shop.getStatus() == null || !shop.getStatus().trim().equals(Constants.ShopStatus.ACTIVE)) {
                continue;
            }

            Double distanceKm = null;
            if (hasCoords && shop.getLatitude() != null && shop.getLongitude() != null) {
                distanceKm = haversineKm(latitude, longitude, shop.getLatitude().doubleValue(), shop.getLongitude().doubleValue());
            }

            if (maxDistanceKm != null && distanceKm != null && distanceKm > maxDistanceKm) {
                continue;
            }

            items.add(ProductSearchItem.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .imageUrl(product.getImageUrl())
                    .shopId(product.getShopId())
                    .shopName(shop.getName())
                    .shopLatitude(shop.getLatitude() != null ? shop.getLatitude().doubleValue() : null)
                    .shopLongitude(shop.getLongitude() != null ? shop.getLongitude().doubleValue() : null)
                    .distanceKm(distanceKm)
                    .build());
        }

        // distance sort when coords present
        if (hasCoords) {
            items.sort(Comparator.comparing(item -> Optional.ofNullable(item.getDistanceKm()).orElse(Double.MAX_VALUE)));
        }
        // optional price sorting
        if (priceSort != null) {
            String norm = priceSort.trim().toLowerCase();
            if (norm.equals("asc")) {
                items.sort(Comparator.comparing(item -> Optional.ofNullable(item.getPrice()).orElse(BigDecimal.ZERO)));
            } else if (norm.equals("desc")) {
                items.sort(Comparator.comparing((ProductSearchItem item) -> Optional.ofNullable(item.getPrice()).orElse(BigDecimal.ZERO)).reversed());
            }
        }

        // Create paginated result
        int totalElements = (int) productPage.getTotalElements();
        Page<ProductSearchItem> resultPage = new org.springframework.data.domain.PageImpl<>(
                items, pageable, totalElements);

        log.info("Products search completed: nameQuery={}, found={}, hasCoords={}, priceSort={}, page={}, size={}, totalElements={}",
                nameQuery, items.size(), hasCoords, priceSort, page, size, totalElements);
        return Result.success(resultPage);
    }

    @Transactional(readOnly = true)
    public Result<ProductDetailResponse> getProductDetail(Integer productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            log.warn("Product not found: productId={}", productId);
            return Result.error(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + productId);
        }

        // Only active product
        if (!isProductActive(product)) {
            log.warn("Product is not active: productId={}, status={}", productId, product.getStatus());
            return Result.error(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + productId);
        }

        Shop shop = null;
        if (product.getShopId() != null) {
            shop = shopRepository.findById(product.getShopId()).orElse(null);
        }
        // Only active shop (status == "1")
        if (shop == null || shop.getStatus() == null || !shop.getStatus().trim().equals(Constants.ShopStatus.ACTIVE)) {
            log.warn("Shop is not active for product: productId={}, shopId={}, shopStatus={}",
                    productId, product.getShopId(), shop != null ? shop.getStatus() : "null");
            return Result.error(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + productId);
        }

        ProductDetailResponse.ShopInfo shopInfo = ProductDetailResponse.ShopInfo.builder()
                .id(shop.getId())
                .name(shop.getName())
                .address(shop.getAddress())
                .latitude(shop.getLatitude() != null ? shop.getLatitude().doubleValue() : null)
                .longitude(shop.getLongitude() != null ? shop.getLongitude().doubleValue() : null)
                .description(shop.getDescription())
                .rating(shop.getRating())
                .status(shop.getStatus())
                .build();

        ProductDetailResponse response = ProductDetailResponse.builder()
                .id(product.getId())
                .shopId(product.getShopId())
                .categoryId(product.getCategoryId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .quantityAvailable(product.getQuantityAvailable())
                .quantityPending(product.getQuantityPending())
                .status(product.getStatus())
                .shop(shopInfo)
                .build();

        log.info("Product detail retrieved successfully: productId={}, shopId={}", productId, product.getShopId());
        return Result.success(response);
    }

    private boolean isProductActive(Product product) {
        if (product == null) return false;
        String s = product.getStatus();
        if (s == null) return false;
        return s.trim().equals(Constants.ProductStatus.ACTIVE);
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = Constants.Distance.EARTH_RADIUS_KM;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
