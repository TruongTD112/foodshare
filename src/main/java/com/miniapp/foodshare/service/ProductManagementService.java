package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.Constants;
import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.CreateProductRequest;
import com.miniapp.foodshare.dto.PagedResult;
import com.miniapp.foodshare.dto.ProductManagementResponse;
import com.miniapp.foodshare.dto.UpdateProductRequest;
import com.miniapp.foodshare.entity.Product;
import com.miniapp.foodshare.entity.Shop;
import com.miniapp.foodshare.repo.ProductRepository;
import com.miniapp.foodshare.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductManagementService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    /**
     * Tạo sản phẩm mới
     */
    @Transactional
    public Result<ProductManagementResponse> createProduct(CreateProductRequest request) {
        try {
            // Validate shop exists
            Optional<Shop> shopOptional = shopRepository.findById(request.getShopId());
            if (shopOptional.isEmpty()) {
                log.warn("Shop not found: shopId={}", request.getShopId());
                return Result.error(ErrorCode.SHOP_NOT_FOUND, "Shop not found");
            }

            // Validate original price vs price
            if (request.getOriginalPrice() != null && request.getPrice() != null) {
                if (request.getOriginalPrice().compareTo(request.getPrice()) < 0) {
                    log.warn("Original price cannot be less than current price: originalPrice={}, price={}", 
                            request.getOriginalPrice(), request.getPrice());
                    return Result.error(ErrorCode.INVALID_REQUEST, "Original price cannot be less than current price");
                }
            }

            // Set default values
            String status = request.getStatus() != null ? request.getStatus() : Constants.ProductStatus.ACTIVE;
            Integer quantityAvailable = request.getQuantityAvailable() != null ? request.getQuantityAvailable() : 0;
            Integer quantityPending = 0; // Default pending quantity

            // Create product entity
            Product product = Product.builder()
                    .shopId(request.getShopId())
                    .name(request.getName())
                    .description(request.getDescription())
                    .price(request.getPrice())
                    .originalPrice(request.getOriginalPrice())
                    .imageUrl(request.getImageUrl())
                    .detailImageUrl(request.getDetailImageUrl())
                    .quantityAvailable(quantityAvailable)
                    .quantityPending(quantityPending)
                    .status(status)
                    .build();

            Product savedProduct = productRepository.save(product);

            ProductManagementResponse response = mapToResponse(savedProduct);
            log.info("Product created successfully: productId={}, name={}, shopId={}", 
                    savedProduct.getId(), savedProduct.getName(), savedProduct.getShopId());
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error creating product: name={}, shopId={}", request.getName(), request.getShopId(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to create product");
        }
    }

    /**
     * Cập nhật thông tin sản phẩm
     */
    @Transactional
    public Result<ProductManagementResponse> updateProduct(Integer productId, UpdateProductRequest request) {
        try {
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isEmpty()) {
                log.warn("Product not found: productId={}", productId);
                return Result.error(ErrorCode.PRODUCT_NOT_FOUND, "Product not found");
            }

            Product product = productOptional.get();

            // Update fields if provided
            if (request.getShopId() != null) {
                // Validate new shop exists
                Optional<Shop> shopOptional = shopRepository.findById(request.getShopId());
                if (shopOptional.isEmpty()) {
                    log.warn("Shop not found: shopId={}", request.getShopId());
                    return Result.error(ErrorCode.SHOP_NOT_FOUND, "Shop not found");
                }
                product.setShopId(request.getShopId());
            }
            if (request.getName() != null) {
                product.setName(request.getName());
            }
            if (request.getDescription() != null) {
                product.setDescription(request.getDescription());
            }
            if (request.getPrice() != null) {
                product.setPrice(request.getPrice());
            }
            if (request.getOriginalPrice() != null) {
                product.setOriginalPrice(request.getOriginalPrice());
            }
            if (request.getImageUrl() != null) {
                product.setImageUrl(request.getImageUrl());
            }
            if (request.getDetailImageUrl() != null) {
                product.setDetailImageUrl(request.getDetailImageUrl());
            }
            if (request.getQuantityAvailable() != null) {
                product.setQuantityAvailable(request.getQuantityAvailable());
            }
            if (request.getStatus() != null) {
                product.setStatus(request.getStatus());
            }

            // Validate original price vs price after update
            if (product.getOriginalPrice() != null && product.getPrice() != null) {
                if (product.getOriginalPrice().compareTo(product.getPrice()) < 0) {
                    log.warn("Original price cannot be less than current price: originalPrice={}, price={}", 
                            product.getOriginalPrice(), product.getPrice());
                    return Result.error(ErrorCode.INVALID_REQUEST, "Original price cannot be less than current price");
                }
            }

            Product savedProduct = productRepository.save(product);
            ProductManagementResponse response = mapToResponse(savedProduct);

            log.info("Product updated successfully: productId={}, name={}, shopId={}", 
                    savedProduct.getId(), savedProduct.getName(), savedProduct.getShopId());
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error updating product: productId={}", productId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to update product");
        }
    }

    /**
     * Xóa sản phẩm
     */
    @Transactional
    public Result<Void> deleteProduct(Integer productId) {
        try {
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isEmpty()) {
                log.warn("Product not found: productId={}", productId);
                return Result.error(ErrorCode.PRODUCT_NOT_FOUND, "Product not found");
            }

            Product product = productOptional.get();
            
            // Check if product has pending orders (optional business rule)
            if (product.getQuantityPending() != null && product.getQuantityPending() > 0) {
                log.warn("Cannot delete product with pending orders: productId={}, pendingQuantity={}", 
                        productId, product.getQuantityPending());
                return Result.error(ErrorCode.INVALID_REQUEST, "Cannot delete product with pending orders");
            }

            productRepository.delete(product);
            log.info("Product deleted successfully: productId={}, name={}, shopId={}", 
                    product.getId(), product.getName(), product.getShopId());
            return Result.success(null);

        } catch (Exception e) {
            log.error("Error deleting product: productId={}", productId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to delete product");
        }
    }

    /**
     * Lấy thông tin sản phẩm theo ID
     */
    @Transactional(readOnly = true)
    public Result<ProductManagementResponse> getProductById(Integer productId) {
        try {
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isEmpty()) {
                log.warn("Product not found: productId={}", productId);
                return Result.error(ErrorCode.PRODUCT_NOT_FOUND, "Product not found");
            }

            Product product = productOptional.get();
            ProductManagementResponse response = mapToResponse(product);

            log.info("Product retrieved successfully: productId={}, name={}, shopId={}", 
                    product.getId(), product.getName(), product.getShopId());
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error retrieving product: productId={}", productId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to retrieve product");
        }
    }

    /**
     * Lấy danh sách sản phẩm theo shop ID
     */
    @Transactional(readOnly = true)
    public Result<List<ProductManagementResponse>> getProductsByShopId(Integer shopId) {
        try {
            // Validate shop exists
            Optional<Shop> shopOptional = shopRepository.findById(shopId);
            if (shopOptional.isEmpty()) {
                log.warn("Shop not found: shopId={}", shopId);
                return Result.error(ErrorCode.SHOP_NOT_FOUND, "Shop not found");
            }

            List<Product> products = productRepository.findByShopId(shopId);
            List<ProductManagementResponse> responses = products.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            log.info("Retrieved {} products for shop: shopId={}", responses.size(), shopId);
            return Result.success(responses);

        } catch (Exception e) {
            log.error("Error retrieving products by shop: shopId={}", shopId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to retrieve products by shop");
        }
    }

    /**
     * Lấy danh sách sản phẩm theo shop ID (có phân trang)
     */
    @Transactional(readOnly = true)
    public Result<PagedResult<ProductManagementResponse>> getProductsByShopIdPaged(Integer shopId, Integer page, Integer size, String sortBy, String sortDirection) {
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

            List<ProductManagementResponse> content = productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

            PagedResult<ProductManagementResponse> result = PagedResult.<ProductManagementResponse>builder()
                .content(content)
                .page(effectivePage)
                .size(effectiveSize)
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .hasNext(effectivePage < productPage.getTotalPages() - 1)
                .hasPrevious(effectivePage > 0)
                .build();

            log.info("Retrieved products by shop (paged): shopId={}, page={}, size={}, totalElements={}", shopId, effectivePage, effectiveSize, productPage.getTotalElements());
            return Result.success(result);

        } catch (Exception e) {
            log.error("Error retrieving products by shop (paged): shopId={}", shopId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to retrieve products by shop (paged)");
        }
    }

    /**
     * Map Product entity to ProductManagementResponse
     */
    private ProductManagementResponse mapToResponse(Product product) {
        return ProductManagementResponse.builder()
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
                .build();
    }
}