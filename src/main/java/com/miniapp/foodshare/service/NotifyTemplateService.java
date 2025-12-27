package com.miniapp.foodshare.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.CreateNotifyTemplateRequest;
import com.miniapp.foodshare.dto.NotifyTemplateResponse;
import com.miniapp.foodshare.entity.CustomerUser;
import com.miniapp.foodshare.entity.NotifyMessage;
import com.miniapp.foodshare.entity.NotifyTemplate;
import com.miniapp.foodshare.entity.Product;
import com.miniapp.foodshare.entity.Shop;
import com.miniapp.foodshare.repo.CustomerUserRepository;
import com.miniapp.foodshare.repo.NotifyMessageRepository;
import com.miniapp.foodshare.repo.NotifyTemplateRepository;
import com.miniapp.foodshare.repo.ProductRepository;
import com.miniapp.foodshare.repo.ShopMemberRepository;
import com.miniapp.foodshare.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyTemplateService {

    private final NotifyTemplateRepository notifyTemplateRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final ShopMemberRepository shopMemberRepository;
    private final CustomerUserRepository customerUserRepository;
    private final NotifyMessageRepository notifyMessageRepository;
    private final ObjectMapper objectMapper;

    private static final Double DEFAULT_RADIUS = 5.0; // Default radius 5 km
    private static final String DEFAULT_STATUS = "0"; // Default status
    private static final int MAX_TEMPLATES_PER_DAY = 3; // Maximum templates per shop per day

    /**
     * Tạo notify template mới cho seller
     */
    @Transactional
    public Result<NotifyTemplateResponse> createNotifyTemplate(CreateNotifyTemplateRequest request, Integer sellerId) {
        try {
            // Validate seller owns the shop
            if (!shopMemberRepository.existsByBackofficeUserIdAndShopId(sellerId, request.getShopId())) {
                log.warn("Seller does not own shop: sellerId={}, shopId={}", sellerId, request.getShopId());
                return Result.error(ErrorCode.FORBIDDEN, "You do not have permission to access this shop");
            }

            // Get shop
            Optional<Shop> shopOptional = shopRepository.findById(request.getShopId());
            if (shopOptional.isEmpty()) {
                log.warn("Shop not found: shopId={}", request.getShopId());
                return Result.error(ErrorCode.SHOP_NOT_FOUND, "Shop not found");
            }
            Shop shop = shopOptional.get();

            // Get product
            Optional<Product> productOptional = productRepository.findById(request.getProductId());
            if (productOptional.isEmpty()) {
                log.warn("Product not found: productId={}", request.getProductId());
                return Result.error(ErrorCode.PRODUCT_NOT_FOUND, "Product not found");
            }
            Product product = productOptional.get();

            // Validate product belongs to shop
            if (!product.getShopId().equals(request.getShopId())) {
                log.warn("Product does not belong to shop: productId={}, shopId={}, productShopId={}", 
                        request.getProductId(), request.getShopId(), product.getShopId());
                return Result.error(ErrorCode.PRODUCT_NOT_BELONG_TO_SHOP, "Product does not belong to this shop");
            }

            // Check daily limit for shop
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
            
            long countToday = notifyTemplateRepository.countByShopIdAndDateRange(
                    request.getShopId(), startOfDay, endOfDay);
            
            if (countToday >= MAX_TEMPLATES_PER_DAY) {
                log.warn("Daily limit reached for shop: shopId={}, countToday={}, max={}", 
                        request.getShopId(), countToday, MAX_TEMPLATES_PER_DAY);
                return Result.error(ErrorCode.INVALID_REQUEST, 
                        String.format("Shop has reached the daily limit of %d notify templates", MAX_TEMPLATES_PER_DAY));
            }

            // Calculate discount percentage
            BigDecimal discountPercentage = BigDecimal.ZERO;
            if (product.getOriginalPrice() != null && product.getPrice() != null 
                    && product.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = product.getOriginalPrice().subtract(product.getPrice());
                discountPercentage = discountAmount.divide(product.getOriginalPrice(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(0, RoundingMode.HALF_UP);
            }
            String discountFormat = String.valueOf(product.getPrice().toBigInteger().divide(BigInteger.valueOf(1000L)));

            String title = String.format("%s giảm giá %s%% chỉ còn %sk",
                    product.getName() != null ? product.getName() : "",
                    discountPercentage,
                    discountFormat);

            String content = String.format("Cửa hàng %s tại %s",
                    shop.getName() != null ? shop.getName() : "",
                    shop.getAddress() != null ? shop.getAddress() : "");

            // Set default radius if not provided
            Double radius = request.getRadius() != null ? request.getRadius() : DEFAULT_RADIUS;

            // Create metadata JSON with link
            String link = String.format("https://www.miniapp-foodshare.com/items/%d", request.getProductId());
            Map<String, String> metadataMap = new HashMap<>();
            metadataMap.put("link", link);
            metadataMap.put("image", product.getImageUrl());
            
            String metadata;
            try {
                metadata = objectMapper.writeValueAsString(metadataMap);
            } catch (Exception e) {
                log.error("Error creating metadata JSON: shopId={}, productId={}", 
                        request.getShopId(), request.getProductId(), e);
                return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to create metadata");
            }

            // Create notify template
            LocalDateTime now = LocalDateTime.now();
            NotifyTemplate notifyTemplate = NotifyTemplate.builder()
                    .status(DEFAULT_STATUS)
                    .radius(radius)
                    .shopId(request.getShopId())
                    .productId(request.getProductId())
                    .title(title)
                    .content(content)
                    .metadata(metadata)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            NotifyTemplate savedTemplate = notifyTemplateRepository.save(notifyTemplate);

            log.info("Notify template created successfully: id={}, sellerId={}, shopId={}, productId={}, radius={}", 
                    savedTemplate.getId(), sellerId, request.getShopId(), request.getProductId(), radius);

            // Tìm users trong phạm vi bán kính và tạo notify messages
            createNotifyMessagesForUsersInRadius(savedTemplate, shop);

            NotifyTemplateResponse response = mapToResponse(savedTemplate);
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error creating notify template: sellerId={}, shopId={}, productId={}", 
                    sellerId, request.getShopId(), request.getProductId(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to create notify template");
        }
    }

    /**
     * Tìm users trong phạm vi bán kính và tạo notify messages cho họ
     * 
     * @param template NotifyTemplate đã được tạo
     * @param shop Shop để lấy tọa độ
     */
    private void createNotifyMessagesForUsersInRadius(NotifyTemplate template, Shop shop) {
        try {
            // Kiểm tra shop có tọa độ không
            if (shop.getLatitude() == null || shop.getLongitude() == null) {
                log.warn("Shop does not have coordinates: shopId={}", shop.getId());
                return;
            }

            // Convert BigDecimal to Double for query
            Double shopLatitude = shop.getLatitude().doubleValue();
            Double shopLongitude = shop.getLongitude().doubleValue();
            Double radius = template.getRadius();

            // Tìm users trong phạm vi bán kính
            List<CustomerUser> usersInRadius = customerUserRepository.findUsersWithinRadius(
                    shopLatitude, shopLongitude, radius);

            if (usersInRadius.isEmpty()) {
                log.info("No users found within radius: templateId={}, shopId={}, radius={}km", 
                        template.getId(), shop.getId(), radius);
                return;
            }

            log.info("Found {} users within radius: templateId={}, shopId={}, radius={}km", 
                    usersInRadius.size(), template.getId(), shop.getId(), radius);

            // Tạo notify messages cho từng user với status = "0"
            LocalDateTime now = LocalDateTime.now();
            List<NotifyMessage> notifyMessages = new ArrayList<>();
            
            for (CustomerUser user : usersInRadius) {
                NotifyMessage notifyMessage = NotifyMessage.builder()
                        .templateId(template.getId())
                        .userId(user.getId())
                        .status("0")
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                notifyMessages.add(notifyMessage);
            }

            // Lưu tất cả notify messages
            notifyMessageRepository.saveAll(notifyMessages);

            log.info("Created {} notify messages: templateId={}, shopId={}", 
                    notifyMessages.size(), template.getId(), shop.getId());

        } catch (Exception e) {
            log.error("Error creating notify messages: templateId={}, shopId={}", 
                    template.getId(), shop.getId(), e);
            // Không throw exception để không ảnh hưởng đến việc tạo template
        }
    }

    /**
     * Map NotifyTemplate entity to NotifyTemplateResponse
     */
    private NotifyTemplateResponse mapToResponse(NotifyTemplate template) {
        return NotifyTemplateResponse.builder()
                .id(template.getId())
                .status(template.getStatus())
                .radius(template.getRadius())
                .shopId(template.getShopId())
                .productId(template.getProductId())
                .title(template.getTitle())
                .content(template.getContent())
                .metadata(template.getMetadata())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}

