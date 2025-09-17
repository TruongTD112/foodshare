package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.Constants;
import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.PagedResult;
import com.miniapp.foodshare.dto.ProductDetailResponse;
import com.miniapp.foodshare.dto.ProductSearchItem;
import com.miniapp.foodshare.entity.Product;
import com.miniapp.foodshare.entity.ProductSalesStats;
import com.miniapp.foodshare.entity.Shop;
import com.miniapp.foodshare.repo.ProductRepository;
import com.miniapp.foodshare.repo.ProductSalesStatsRepository;
import com.miniapp.foodshare.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final ProductSalesStatsRepository productSalesStatsRepository;

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
    public Result<PagedResult<ProductSearchItem>> searchProducts(
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
            PagedResult<ProductSearchItem> emptyResult = PagedResult.<ProductSearchItem>builder()
                    .content(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .totalPages(0)
                    .hasNext(false)
                    .hasPrevious(false)
                    .build();
            return Result.success(emptyResult);
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

            // Calculate discount percentage from originalPrice and price (chỉ lấy phần nguyên)
            BigDecimal discountPercentage = BigDecimal.ZERO;
            if (product.getOriginalPrice() != null && product.getPrice() != null && product.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = product.getOriginalPrice().subtract(product.getPrice());
                discountPercentage = discountAmount.divide(product.getOriginalPrice(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
            }

            items.add(ProductSearchItem.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .originalPrice(product.getOriginalPrice())
                    .discountPercentage(discountPercentage)
                    .imageUrl(product.getImageUrl())
                    .shopId(product.getShopId())
                    .shopName(shop.getName())
                    .shopLatitude(shop.getLatitude())
                    .shopLongitude(shop.getLongitude())
                    .distanceKm(distanceKm)
                    .totalOrders(null) // Không có thông tin đơn hàng cho API này
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
        long totalElements = productPage.getTotalElements();
        int totalPages = productPage.getTotalPages();
        
        PagedResult<ProductSearchItem> result = PagedResult.<ProductSearchItem>builder()
                .content(items)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();

        log.info("Products search completed: nameQuery={}, found={}, hasCoords={}, priceSort={}, page={}, size={}, totalElements={}",
                nameQuery, items.size(), hasCoords, priceSort, page, size, totalElements);
        return Result.success(result);
    }

    /**
     * Tìm kiếm sản phẩm gần đây dựa trên vị trí người dùng với khoảng cách mặc định.
     * Sử dụng khoảng cách mặc định từ Constants.Distance.DEFAULT_MAX_DISTANCE_KM.
     * 
     * @param latitude vĩ độ của người dùng (bắt buộc)
     * @param longitude kinh độ của người dùng (bắt buộc)
     * @param page số trang (mặc định: 0)
     * @param size kích thước trang (mặc định: 20)
     * @return danh sách sản phẩm gần đây được phân trang
     */
    @Transactional(readOnly = true)
    public Result<PagedResult<ProductSearchItem>> searchNearbyProducts(
            Double latitude,
            Double longitude,
            int page,
            int size
    ) {
        // Validate coordinates
        if (latitude == null || longitude == null) {
            log.warn("Missing coordinates for nearby search: latitude={}, longitude={}", latitude, longitude);
            return Result.error(ErrorCode.INVALID_REQUEST, "Latitude and longitude are required for nearby search");
        }

        // Validate coordinate ranges
        if (latitude < -90 || latitude > 90) {
            log.warn("Invalid latitude: {}", latitude);
            return Result.error(ErrorCode.INVALID_REQUEST, "Latitude must be between -90 and 90 degrees");
        }
        if (longitude < -180 || longitude > 180) {
            log.warn("Invalid longitude: {}", longitude);
            return Result.error(ErrorCode.INVALID_REQUEST, "Longitude must be between -180 and 180 degrees");
        }

        // Use default max distance from constants
        Double maxDistanceKm = Constants.Distance.DEFAULT_MAX_DISTANCE_KM;
        
        log.info("Searching nearby products: latitude={}, longitude={}, maxDistanceKm={}, page={}, size={}", 
                latitude, longitude, maxDistanceKm, page, size);

        // Call existing search method with default distance
        return searchProducts(null, latitude, longitude, maxDistanceKm, null, null, null, page, size);
    }

    /**
     * Lấy danh sách sản phẩm giảm giá nhiều nhất, sắp xếp theo mức giảm giá giảm dần.
     * API đơn giản chỉ lấy tất cả sản phẩm có giảm giá và sắp xếp theo số tiền giảm.
     * 
     * @param latitude vĩ độ của người dùng (tùy chọn)
     * @param longitude kinh độ của người dùng (tùy chọn)
     * @param page số trang (mặc định: 0)
     * @param size kích thước trang (mặc định: 20)
     * @return danh sách sản phẩm giảm giá được phân trang
     */
    @Transactional(readOnly = true)
    public Result<PagedResult<ProductSearchItem>> searchTopDiscountedProducts(Double latitude, Double longitude, int page, int size) {
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

        // Get discounted products sorted by discount amount
        Page<Product> productPage = productRepository.findDiscountedProductsByDiscountAmount(Constants.ProductStatus.ACTIVE, pageable);
        List<Product> products = productPage.getContent();

        if (products.isEmpty()) {
            log.info("No discounted products found: page={}, size={}", page, size);
            PagedResult<ProductSearchItem> emptyResult = PagedResult.<ProductSearchItem>builder()
                    .content(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .totalPages(0)
                    .hasNext(false)
                    .hasPrevious(false)
                    .build();
            return Result.success(emptyResult);
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

            // Calculate discount percentage from originalPrice and price (chỉ lấy phần nguyên)
            BigDecimal discountPercentage = BigDecimal.ZERO;
            if (product.getOriginalPrice() != null && product.getPrice() != null && product.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = product.getOriginalPrice().subtract(product.getPrice());
                discountPercentage = discountAmount.divide(product.getOriginalPrice(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
            }

            items.add(ProductSearchItem.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .originalPrice(product.getOriginalPrice())
                    .discountPercentage(discountPercentage)
                    .imageUrl(product.getImageUrl())
                    .shopId(product.getShopId())
                    .shopName(shop.getName())
                    .shopLatitude(shop.getLatitude())
                    .shopLongitude(shop.getLongitude())
                    .distanceKm(distanceKm)
                    .totalOrders(null) // Không có thông tin đơn hàng cho API này
                    .build());
        }

        // Create paginated result
        long totalElements = productPage.getTotalElements();
        int totalPages = productPage.getTotalPages();
        
        PagedResult<ProductSearchItem> result = PagedResult.<ProductSearchItem>builder()
                .content(items)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();

        log.info("Top discounted products search completed: found={}, page={}, size={}, totalElements={}",
                items.size(), page, size, totalElements);
        return Result.success(result);
    }

    /**
     * Lấy danh sách sản phẩm bán chạy nhất dựa trên tổng số lượng đã mua.
     * Chỉ tính các order có status = 'completed'.
     * 
     * @param latitude vĩ độ của người dùng (tùy chọn)
     * @param longitude kinh độ của người dùng (tùy chọn)
     * @param page số trang (mặc định: 0)
     * @param size kích thước trang (mặc định: 20)
     * @return danh sách sản phẩm bán chạy được phân trang
     */
    @Transactional(readOnly = true)
    public Result<PagedResult<ProductSearchItem>> searchPopularProducts(Double latitude, Double longitude, int page, int size) {
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

        // Get popular products sorted by total quantity sold
        Page<Product> productPage = productRepository.findPopularProducts(Constants.ProductStatus.ACTIVE, pageable);
        List<Product> products = productPage.getContent();

        if (products.isEmpty()) {
            log.info("No popular products found: page={}, size={}", page, size);
            PagedResult<ProductSearchItem> emptyResult = PagedResult.<ProductSearchItem>builder()
                    .content(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .totalPages(0)
                    .hasNext(false)
                    .hasPrevious(false)
                    .build();
            return Result.success(emptyResult);
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

        // Lấy thông tin sales stats cho popular products
        Set<Integer> productIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toSet());
        
        Map<Integer, ProductSalesStats> statsByProductId = new HashMap<>();
        if (!productIds.isEmpty()) {
            statsByProductId = productSalesStatsRepository.findAll().stream()
                    .filter(stats -> productIds.contains(stats.getProductId()))
                    .collect(Collectors.toMap(ProductSalesStats::getProductId, stats -> stats));
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

            // Calculate discount percentage from originalPrice and price (chỉ lấy phần nguyên)
            BigDecimal discountPercentage = BigDecimal.ZERO;
            if (product.getOriginalPrice() != null && product.getPrice() != null && product.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = product.getOriginalPrice().subtract(product.getPrice());
                discountPercentage = discountAmount.divide(product.getOriginalPrice(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
            }

            // Lấy thông tin totalOrders từ ProductSalesStats
            ProductSalesStats salesStats = statsByProductId.get(product.getId());
            Integer totalOrders = salesStats != null ? salesStats.getTotalOrders() : 0;

            items.add(ProductSearchItem.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .originalPrice(product.getOriginalPrice())
                    .discountPercentage(discountPercentage)
                    .imageUrl(product.getImageUrl())
                    .shopId(product.getShopId())
                    .shopName(shop.getName())
                    .shopLatitude(shop.getLatitude())
                    .shopLongitude(shop.getLongitude())
                    .distanceKm(distanceKm)
                    .totalOrders(totalOrders) // Số lượng đơn đã đặt
                    .build());
        }

        // Create paginated result
        long totalElements = productPage.getTotalElements();
        int totalPages = productPage.getTotalPages();
        
        PagedResult<ProductSearchItem> result = PagedResult.<ProductSearchItem>builder()
                .content(items)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();

        log.info("Popular products search completed: found={}, page={}, size={}, totalElements={}",
                items.size(), page, size, totalElements);
        return Result.success(result);
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
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
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
