package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.CreateShopRequest;
import com.miniapp.foodshare.dto.ShopManagementResponse;
import com.miniapp.foodshare.dto.UpdateShopRequest;
import com.miniapp.foodshare.entity.Shop;
import com.miniapp.foodshare.entity.ShopMember;
import com.miniapp.foodshare.repo.ShopRepository;
import com.miniapp.foodshare.repo.ShopMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopManagementService {

    private final ShopRepository shopRepository;
    private final ShopMemberRepository shopMemberRepository;

    /**
     * Tạo cửa hàng mới
     */
    @Transactional
    public Result<ShopManagementResponse> createShop(CreateShopRequest request, Integer sellerId) {
        try {
            // Validate coordinates
            if (request.getLatitude() == null || request.getLongitude() == null) {
                log.warn("Latitude and longitude are required");
                return Result.error(ErrorCode.INVALID_REQUEST, "Latitude and longitude are required");
            }

            // Validate coordinate ranges
            if (request.getLatitude().compareTo(BigDecimal.valueOf(-90)) < 0 ||
                request.getLatitude().compareTo(BigDecimal.valueOf(90)) > 0) {
                log.warn("Invalid latitude: {}", request.getLatitude());
                return Result.error(ErrorCode.INVALID_REQUEST, "Latitude must be between -90 and 90");
            }

            if (request.getLongitude().compareTo(BigDecimal.valueOf(-180)) < 0 || 
                request.getLongitude().compareTo(BigDecimal.valueOf(180)) > 0) {
                log.warn("Invalid longitude: {}", request.getLongitude());
                return Result.error(ErrorCode.INVALID_REQUEST, "Longitude must be between -180 and 180");
            }

            // Set default status if not provided
            String status = request.getStatus() != null ? request.getStatus() : "active";

            // Create shop entity
            Shop shop = Shop.builder()
                    .name(request.getName())
                    .address(request.getAddress())
                    .phone(request.getPhone())
                    .imageUrl(request.getImageUrl())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .description(request.getDescription())
                    .rating(BigDecimal.ZERO) // Default rating
                    .status(status)
                    .build();

            Shop savedShop = shopRepository.save(shop);

            // Tạo bản ghi Shop_Member để gán seller làm owner của cửa hàng (nếu có sellerId)
            if (sellerId != null) {
                ShopMember shopMember = ShopMember.builder()
                        .shopId(savedShop.getId())
                        .backofficeUserId(sellerId)
                        .role("OWNER")
                        .build();
                
                shopMemberRepository.save(shopMember);
                log.info("Shop created successfully: shopId={}, name={}, sellerId={}", 
                        savedShop.getId(), savedShop.getName(), sellerId);
            } else {
                log.info("Shop created successfully: shopId={}, name={} (no seller assigned)", 
                        savedShop.getId(), savedShop.getName());
            }

            ShopManagementResponse response = mapToResponse(savedShop);
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error creating shop: name={}", request.getName(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to create shop");
        }
    }

    /**
     * Cập nhật thông tin cửa hàng
     */
    @Transactional
    public Result<ShopManagementResponse> updateShop(Integer shopId, UpdateShopRequest request) {
        try {
            Optional<Shop> shopOptional = shopRepository.findById(shopId);
            if (shopOptional.isEmpty()) {
                log.warn("Shop not found: shopId={}", shopId);
                return Result.error(ErrorCode.SHOP_NOT_FOUND, "Shop not found");
            }

            Shop shop = shopOptional.get();

            // Update fields if provided
            if (request.getName() != null) {
                shop.setName(request.getName());
            }
            if (request.getAddress() != null) {
                shop.setAddress(request.getAddress());
            }
            if (request.getPhone() != null) {
                shop.setPhone(request.getPhone());
            }
            if (request.getImageUrl() != null) {
                shop.setImageUrl(request.getImageUrl());
            }
            if (request.getLatitude() != null) {
                // Validate latitude
                if (request.getLatitude().compareTo(BigDecimal.valueOf(-90)) < 0 || 
                    request.getLatitude().compareTo(BigDecimal.valueOf(90)) > 0) {
                    log.warn("Invalid latitude: {}", request.getLatitude());
                    return Result.error(ErrorCode.INVALID_REQUEST, "Latitude must be between -90 and 90");
                }
                shop.setLatitude(request.getLatitude());
            }
            if (request.getLongitude() != null) {
                // Validate longitude
                if (request.getLongitude().compareTo(BigDecimal.valueOf(-180)) < 0 || 
                    request.getLongitude().compareTo(BigDecimal.valueOf(180)) > 0) {
                    log.warn("Invalid longitude: {}", request.getLongitude());
                    return Result.error(ErrorCode.INVALID_REQUEST, "Longitude must be between -180 and 180");
                }
                shop.setLongitude(request.getLongitude());
            }
            if (request.getDescription() != null) {
                shop.setDescription(request.getDescription());
            }
            if (request.getStatus() != null) {
                shop.setStatus(request.getStatus());
            }

            Shop savedShop = shopRepository.save(shop);
            ShopManagementResponse response = mapToResponse(savedShop);

            log.info("Shop updated successfully: shopId={}, name={}", savedShop.getId(), savedShop.getName());
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error updating shop: shopId={}", shopId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to update shop");
        }
    }

    /**
     * Xóa cửa hàng
     */
    @Transactional
    public Result<Void> deleteShop(Integer shopId) {
        try {
            Optional<Shop> shopOptional = shopRepository.findById(shopId);
            if (shopOptional.isEmpty()) {
                log.warn("Shop not found: shopId={}", shopId);
                return Result.error(ErrorCode.SHOP_NOT_FOUND, "Shop not found");
            }

            Shop shop = shopOptional.get();
            shopRepository.delete(shop);
            log.info("Shop deleted successfully: shopId={}, name={}", shop.getId(), shop.getName());
            return Result.success(null);

        } catch (Exception e) {
            log.error("Error deleting shop: shopId={}", shopId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to delete shop");
        }
    }

    /**
     * Lấy thông tin cửa hàng theo ID
     */
    @Transactional(readOnly = true)
    public Result<ShopManagementResponse> getShopById(Integer shopId) {
        try {
            Optional<Shop> shopOptional = shopRepository.findById(shopId);
            if (shopOptional.isEmpty()) {
                log.warn("Shop not found: shopId={}", shopId);
                return Result.error(ErrorCode.SHOP_NOT_FOUND, "Shop not found");
            }

            Shop shop = shopOptional.get();
            ShopManagementResponse response = mapToResponse(shop);

            log.info("Shop retrieved successfully: shopId={}, name={}", shop.getId(), shop.getName());
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error retrieving shop: shopId={}", shopId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to retrieve shop");
        }
    }

    /**
     * Lấy danh sách cửa hàng của seller
     */
    @Transactional(readOnly = true)
    public Result<List<ShopManagementResponse>> getShopsBySellerId(Integer sellerId) {
        try {
            // Lấy danh sách shop ID mà seller có quyền truy cập
            List<Integer> shopIds = shopMemberRepository.findShopIdsBySellerId(sellerId);
            
            if (shopIds.isEmpty()) {
                log.info("No shops found for seller: sellerId={}", sellerId);
                return Result.success(List.of());
            }

            // Lấy thông tin chi tiết các shop
            List<Shop> shops = shopRepository.findAllById(shopIds);
            
            // Map sang response
            List<ShopManagementResponse> responses = shops.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            log.info("Shops retrieved successfully for seller: sellerId={}, count={}", sellerId, responses.size());
            return Result.success(responses);

        } catch (Exception e) {
            log.error("Error retrieving shops for seller: sellerId={}", sellerId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to retrieve shops");
        }
    }

    /**
     * Map Shop entity to ShopManagementResponse
     */
    private ShopManagementResponse mapToResponse(Shop shop) {
        return ShopManagementResponse.builder()
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
    }
}
