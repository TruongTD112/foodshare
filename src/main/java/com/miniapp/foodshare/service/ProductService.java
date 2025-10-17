package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.Constants;
import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.PagedResult;
import com.miniapp.foodshare.dto.ProductDetailResponse;
import com.miniapp.foodshare.dto.ProductResponse;
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
     * Tìm kiếm sản phẩm tổng quát với thứ tự ưu tiên: name → discount → distance
     * Sử dụng native query để tối ưu hiệu suất và sắp xếp thông minh
     * <p>
     * Thứ tự ưu tiên:
     * 1. Tên sản phẩm match (1000 điểm)
     * 2. Có giảm giá (500 điểm) 
     * 3. Khoảng cách gần (1000 - distance_km điểm)
     * <p>
     * Behavior:
     * - Nếu có nameQuery: tìm kiếm theo tên (case-insensitive)
     * - Nếu có latitude/longitude: tính khoảng cách và ưu tiên gần
     * - Nếu có maxDistanceKm: lọc theo khoảng cách tối đa
     * - Nếu có minPrice/maxPrice: lọc theo khoảng giá
     * - Nếu có minDiscount: lọc theo mức giảm giá tối thiểu
     * - Nếu có sortBy: sắp xếp theo trường được chỉ định
     * - Chỉ lấy sản phẩm và cửa hàng active
     *
     * @param nameQuery     tên sản phẩm để tìm kiếm (tùy chọn)
     * @param latitude      vĩ độ khách hàng (tùy chọn)
     * @param longitude     kinh độ khách hàng (tùy chọn)
     * @param maxDistanceKm khoảng cách tối đa (km) (tùy chọn)
     * @param minPrice      giá tối thiểu (tùy chọn)
     * @param maxPrice      giá tối đa (tùy chọn)
     * @param minDiscount   mức giảm giá tối thiểu (%) (tùy chọn)
     * @param sortBy        trường để sắp xếp (name, price, discount, distance) (tùy chọn)
     * @param sortDirection hướng sắp xếp (asc, desc) (tùy chọn)
     * @param page          số trang (0-based, mặc định: 0)
     * @param size          kích thước trang (mặc định: 20)
     * @return danh sách sản phẩm được phân trang với thông tin cửa hàng và khoảng cách
     */
    @Transactional(readOnly = true)
    public Result<PagedResult<ProductSearchItem>> searchProducts(
            String nameQuery,
            Double latitude,
            Double longitude,
            Double maxDistanceKm,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minDiscount,
            String sortBy,
            String sortDirection,
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

        // Validate coordinates if provided
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            log.warn("Invalid latitude: {}", latitude);
            return Result.error(ErrorCode.INVALID_REQUEST, "Latitude must be between -90 and 90 degrees");
        }
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            log.warn("Invalid longitude: {}", longitude);
            return Result.error(ErrorCode.INVALID_REQUEST, "Longitude must be between -180 and 180 degrees");
        }

        log.info("Searching products with priority: nameQuery={}, hasCoords={}, maxDistanceKm={}, minPrice={}, maxPrice={}, minDiscount={}, sortBy={}, sortDirection={}, page={}, size={}", 
                nameQuery, latitude != null && longitude != null, maxDistanceKm, minPrice, maxPrice, minDiscount, sortBy, sortDirection, page, size);

        try {
            // Create pageable for database query
            Pageable pageable = PageRequest.of(page, size);

            // Use native query with priority scoring and sorting
            List<Object[]> results = productRepository.searchProductsWithPriority(
                nameQuery, latitude, longitude, maxDistanceKm, minPrice, maxPrice, minDiscount, sortBy, sortDirection, pageable
            );

            // Convert results to ProductSearchItem
            List<ProductSearchItem> items = new ArrayList<>();
            for (Object[] row : results) {
                try {
                    // Map database columns to ProductSearchItem
                    Integer productId = (Integer) row[0];
                    String name = (String) row[1];
                    BigDecimal price = (BigDecimal) row[2];
                    BigDecimal originalPrice = (BigDecimal) row[3];
                    String imageUrl = (String) row[4];
                    Integer shopId = (Integer) row[5];
                    String shopName = (String) row[6];
                    BigDecimal shopLatitude = (BigDecimal) row[10];
                    BigDecimal shopLongitude = (BigDecimal) row[11];
                    Double distanceKm = row[15] != null ? ((Number) row[15]).doubleValue() : null;

                    // Calculate discount percentage
                    BigDecimal discountPercentage = BigDecimal.ZERO;
                    if (originalPrice != null && price != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal discountAmount = originalPrice.subtract(price);
                        discountPercentage = discountAmount.divide(originalPrice, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
                    }

                    // Get total orders from ProductSalesStats (simplified for now)
                    Integer totalOrders = 0; // TODO: Could be optimized with a separate query

                    ProductSearchItem item = ProductSearchItem.builder()
                            .productId(productId)
                            .name(name)
                            .price(price)
                            .originalPrice(originalPrice)
                            .imageUrl(imageUrl)
                            .shopId(shopId)
                            .shopName(shopName)
                            .shopLatitude(shopLatitude)
                            .shopLongitude(shopLongitude)
                            .distanceKm(distanceKm)
                            .discountPercentage(discountPercentage)
                            .totalOrders(totalOrders)
                            .build();

                    items.add(item);
                } catch (Exception e) {
                    log.warn("Error mapping product result: {}", e.getMessage());
                    continue;
                }
            }
            
            // For native query, we need to get total count separately
            // This is a limitation of native queries with pagination
            int totalElements = items.size(); // Simplified for now
            int totalPages = (int) Math.ceil((double) totalElements / size);
        
        PagedResult<ProductSearchItem> result = PagedResult.<ProductSearchItem>builder()
                .content(items)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();

            log.info("Products search completed with priority: nameQuery={}, found={}, hasCoords={}, page={}, size={}, totalElements={}",
                    nameQuery, items.size(), latitude != null && longitude != null, page, size, totalElements);
        return Result.success(result);

        } catch (Exception e) {
            log.error("Error searching products: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Error searching products");
        }
    }

    /**
     * Tìm kiếm sản phẩm gần đây dựa trên vị trí người dùng với khoảng cách mặc định.
     * Sử dụng native query để tính khoảng cách trực tiếp trong database, tối ưu hiệu suất.
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

        // Validate pagination parameters
        if (page < Constants.Pagination.DEFAULT_PAGE_NUMBER) {
            log.warn("Invalid page number: page={}", page);
            return Result.error(ErrorCode.INVALID_PAGE_NUMBER, "Page number must be >= 0");
        }
        if (size <= 0 || size > Constants.Pagination.MAX_PAGE_SIZE) {
            log.warn("Invalid page size: size={}", size);
            return Result.error(ErrorCode.INVALID_PAGE_SIZE, "Page size must be between 1 and 100");
        }

        // Use default max distance from constants
        Double maxDistanceKm = Constants.Distance.DEFAULT_MAX_DISTANCE_KM;
        
        log.info("Searching nearby products with native query: latitude={}, longitude={}, maxDistanceKm={}, page={}, size={}",
                latitude, longitude, maxDistanceKm, page, size);

        try {
            // Create pageable for database query
            Pageable pageable = PageRequest.of(page, size);

            // Use native query to get products with distance calculation in database
            List<Object[]> results = productRepository.findNearbyProductsWithDistance(
                    latitude, longitude, maxDistanceKm, pageable
            );

            // Convert results to ProductSearchItem
            List<ProductSearchItem> items = new ArrayList<>();
            for (Object[] row : results) {
                try {
                    // Map database columns to ProductSearchItem
                    Integer productId = (Integer) row[0];
                    String name = (String) row[1];
                    BigDecimal price = (BigDecimal) row[2];
                    BigDecimal originalPrice = (BigDecimal) row[3];
                    String imageUrl = (String) row[4];
                    Integer shopId = (Integer) row[5];
                    String shopName = (String) row[6];
                    Double distanceKm = ((Number) row[15]).doubleValue();

                    // Calculate discount percentage
                    BigDecimal discountPercentage = BigDecimal.ZERO;
                    if (originalPrice != null && price != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal discountAmount = originalPrice.subtract(price);
                        discountPercentage = discountAmount.divide(originalPrice, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
                    }

                    // Get total orders from ProductSalesStats (simplified for now)
                    Integer totalOrders = 0; // TODO: Could be optimized with a separate query

                    ProductSearchItem item = ProductSearchItem.builder()
                            .productId(productId)
                            .name(name)
                            .price(price)
                            .originalPrice(originalPrice)
                            .imageUrl(imageUrl)
                            .shopId(shopId)
                            .shopName(shopName)
                            .distanceKm(distanceKm)
                            .discountPercentage(discountPercentage)
                            .totalOrders(totalOrders)
                            .build();

                    items.add(item);
                } catch (Exception e) {
                    log.warn("Error mapping product result: {}", e.getMessage());
                    continue;
                }
            }

            // For native query, we need to get total count separately
            // This is a limitation of native queries with pagination
            // In production, you might want to use a separate count query
            int totalElements = items.size(); // Simplified for now
            int totalPages = (int) Math.ceil((double) totalElements / size);

            PagedResult<ProductSearchItem> result = PagedResult.<ProductSearchItem>builder()
                    .content(items)
                    .page(page)
                    .size(size)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .hasNext(page < totalPages - 1)
                    .hasPrevious(page > 0)
                    .build();

            log.info("Nearby products search completed: found={}, page={}, size={}, totalElements={}",
                    items.size(), page, size, totalElements);
            return Result.success(result);

        } catch (Exception e) {
            log.error("Error searching nearby products: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Error searching nearby products");
        }
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

        // Get sales stats for all products
        Set<Integer> productIds = products.stream()
                .map(Product::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, ProductSalesStats> statsByProductId = new HashMap<>();
        if (!productIds.isEmpty()) {
            statsByProductId = productSalesStatsRepository.findByProductIdIn(productIds)
                    .stream()
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
                distanceKm = (double) Math.round(haversineKm(latitude, longitude, shop.getLatitude().doubleValue(), shop.getLongitude().doubleValue()));
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
                    .detailImageUrl(product.getDetailImageUrl())
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
                distanceKm = (double) Math.round(haversineKm(latitude, longitude, shop.getLatitude().doubleValue(), shop.getLongitude().doubleValue()));
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
                    .detailImageUrl(product.getDetailImageUrl())
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

        // Calculate discount percentage from originalPrice and price (chỉ lấy phần nguyên)
        BigDecimal discountPercentage = BigDecimal.ZERO;
        if (product.getOriginalPrice() != null && product.getPrice() != null && product.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountAmount = product.getOriginalPrice().subtract(product.getPrice());
            discountPercentage = discountAmount.divide(product.getOriginalPrice(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
        }

        // Lấy thông tin totalOrders từ ProductSalesStats
        ProductSalesStats salesStats = productSalesStatsRepository.findByProductId(product.getId()).orElse(null);
        Integer totalOrders = salesStats != null ? salesStats.getTotalOrders() : 0;

        ProductDetailResponse.ShopInfo shopInfo = ProductDetailResponse.ShopInfo.builder()
                .id(shop.getId())
                .name(shop.getName())
                .address(shop.getAddress())
                .phone(shop.getPhone())
                .imageUrl(shop.getImageUrl())
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
                .originalPrice(product.getOriginalPrice())
                .discountPercentage(discountPercentage)
                .imageUrl(product.getImageUrl())
                .detailImageUrl(product.getDetailImageUrl())
                .quantityAvailable(product.getQuantityAvailable())
                .quantityPending(product.getQuantityPending())
                .status(product.getStatus())
                .totalOrders(totalOrders)
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

    /**
     * Lấy danh sách sản phẩm theo shop ID với phân trang
     */
    @Transactional(readOnly = true)
    public Result<PagedResult<ProductResponse>> getProductsByShop(Integer shopId, Integer page, Integer size, String sortBy, String sortDirection) {
        try {
            // Validate shop exists
            Optional<Shop> shopOptional = shopRepository.findById(shopId);
            if (shopOptional.isEmpty()) {
                log.warn("Shop not found: shopId={}", shopId);
                return Result.error(ErrorCode.SHOP_NOT_FOUND, "Shop not found");
            }

            int effectivePage = page != null ? page : 0;
            int effectiveSize = size != null && size > 0 ? size : 20;
            String effectiveSortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
            String effectiveDirection = (sortDirection != null && !sortDirection.isBlank()) ? sortDirection : "desc";

            org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.fromString(effectiveDirection), effectiveSortBy
            );
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(effectivePage, effectiveSize, sort);

            org.springframework.data.domain.Page<Product> productPage = productRepository.findByShopId(shopId, pageable);

            List<ProductResponse> content = productPage.getContent().stream()
                .filter(this::isProductActive)
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());

            PagedResult<ProductResponse> result = PagedResult.<ProductResponse>builder()
                .content(content)
                .page(effectivePage)
                .size(effectiveSize)
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .hasNext(effectivePage < productPage.getTotalPages() - 1)
                .hasPrevious(effectivePage > 0)
                .build();

            log.info("Retrieved products by shop: shopId={}, page={}, size={}, totalElements={}", shopId, effectivePage, effectiveSize, productPage.getTotalElements());
            return Result.success(result);

        } catch (Exception e) {
            log.error("Error retrieving products by shop: shopId={}", shopId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to retrieve products by shop");
        }
    }

    /**
     * Map Product entity to ProductResponse
     */
    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .shopId(product.getShopId())
                .categoryId(product.getCategoryId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .imageUrl(product.getImageUrl())
                .detailImageUrl(product.getDetailImageUrl())
                .quantityAvailable(product.getQuantityAvailable())
                .quantityPending(product.getQuantityPending())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
