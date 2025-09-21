package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.AdminShopDetailResponse;
import com.miniapp.foodshare.dto.AdminShopListResponse;
import com.miniapp.foodshare.dto.UpdateShopRequest;
import com.miniapp.foodshare.entity.Product;
import com.miniapp.foodshare.entity.Shop;
import com.miniapp.foodshare.repo.BackOfficeUserRepository;
import com.miniapp.foodshare.repo.ProductRepository;
import com.miniapp.foodshare.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminShopService {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    /**
     * Lấy danh sách tất cả cửa hàng
     */
    @Transactional(readOnly = true)
    public Result<Page<AdminShopListResponse>> getAllShops(Pageable pageable) {
        try {
            Page<Shop> shops = shopRepository.findAll(pageable);

            Page<AdminShopListResponse> response = shops.map(shop -> {
                // Đếm số sản phẩm trong cửa hàng
                int totalProducts = productRepository.findByShopId(shop.getId()).size();

                return AdminShopListResponse.builder()
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
                        .createdAt(shop.getCreatedAt())
                        .updatedAt(shop.getUpdatedAt())
                        .totalProducts(totalProducts)
                        .build();
            });

            log.info("Retrieved all shops: totalElements={}", response.getTotalElements());
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error getting all shops", e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi lấy danh sách cửa hàng");
        }
    }

    /**
     * Lấy chi tiết cửa hàng theo ID
     */
    @Transactional(readOnly = true)
    public Result<AdminShopDetailResponse> getShopDetail(Integer shopId) {
        try {
            Optional<Shop> shopOptional = shopRepository.findById(shopId);
            if (shopOptional.isEmpty()) {
                log.warn("Shop not found: shopId={}", shopId);
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy cửa hàng");
            }

            Shop shop = shopOptional.get();

            // Lấy danh sách sản phẩm trong cửa hàng
            List<Product> products = productRepository.findByShopId(shopId);

            List<AdminShopDetailResponse.AdminProductItem> productItems = products.stream()
                    .map(product -> AdminShopDetailResponse.AdminProductItem.builder()
                            .id(product.getId())
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
                            .build())
                    .collect(Collectors.toList());

            AdminShopDetailResponse response = AdminShopDetailResponse.builder()
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
                    .createdAt(shop.getCreatedAt())
                    .updatedAt(shop.getUpdatedAt())
                    .products(productItems)
                    .build();

            log.info("Retrieved shop detail: shopId={}, productsCount={}", shopId, productItems.size());
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error getting shop detail: shopId={}", shopId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi lấy chi tiết cửa hàng");
        }
    }

    /**
     * Cập nhật cửa hàng
     */
    @Transactional
    public Result<AdminShopDetailResponse> updateShop(Integer shopId, UpdateShopRequest request) {
        try {
            Optional<Shop> shopOptional = shopRepository.findById(shopId);
            if (shopOptional.isEmpty()) {
                log.warn("Shop not found: shopId={}", shopId);
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy cửa hàng");
            }

            Shop shop = shopOptional.get();

            // Cập nhật các trường không null
            if (request.getName() != null) shop.setName(request.getName());
            if (request.getAddress() != null) shop.setAddress(request.getAddress());
            if (request.getPhone() != null) shop.setPhone(request.getPhone());
            if (request.getImageUrl() != null) shop.setImageUrl(request.getImageUrl());
            if (request.getLatitude() != null) shop.setLatitude(request.getLatitude());
            if (request.getLongitude() != null) shop.setLongitude(request.getLongitude());
            if (request.getDescription() != null) shop.setDescription(request.getDescription());
            if (request.getRating() != null) shop.setRating(request.getRating());
            if (request.getStatus() != null) shop.setStatus(request.getStatus());

            Shop updatedShop = shopRepository.save(shop);

            // Lấy danh sách sản phẩm trong cửa hàng
            List<Product> products = productRepository.findByShopId(shopId);

            List<AdminShopDetailResponse.AdminProductItem> productItems = products.stream()
                    .map(product -> AdminShopDetailResponse.AdminProductItem.builder()
                            .id(product.getId())
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
                            .build())
                    .collect(Collectors.toList());

            AdminShopDetailResponse response = AdminShopDetailResponse.builder()
                    .id(updatedShop.getId())
                    .name(updatedShop.getName())
                    .address(updatedShop.getAddress())
                    .phone(updatedShop.getPhone())
                    .imageUrl(updatedShop.getImageUrl())
                    .latitude(updatedShop.getLatitude())
                    .longitude(updatedShop.getLongitude())
                    .description(updatedShop.getDescription())
                    .rating(updatedShop.getRating())
                    .status(updatedShop.getStatus())
                    .createdAt(updatedShop.getCreatedAt())
                    .updatedAt(updatedShop.getUpdatedAt())
                    .products(productItems)
                    .build();

            log.info("Shop updated successfully: shopId={}, name={}", updatedShop.getId(), updatedShop.getName());
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error updating shop: shopId={}", shopId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi cập nhật cửa hàng");
        }
    }

    /**
     * Xóa cửa hàng
     */
    @Transactional
    public Result<String> deleteShop(Integer shopId) {
        try {
            Optional<Shop> shopOptional = shopRepository.findById(shopId);
            if (shopOptional.isEmpty()) {
                log.warn("Shop not found: shopId={}", shopId);
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy cửa hàng");
            }

            // Xóa tất cả sản phẩm trong cửa hàng trước
            List<Product> products = productRepository.findByShopId(shopId);
            productRepository.deleteAll(products);

            // Xóa cửa hàng
            shopRepository.deleteById(shopId);

            log.info("Shop deleted successfully: shopId={}", shopId);
            return Result.success("Xóa cửa hàng thành công");

        } catch (Exception e) {
            log.error("Error deleting shop: shopId={}", shopId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi xóa cửa hàng");
        }
    }
}
