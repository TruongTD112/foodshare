package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.AdminProductDetailResponse;
import com.miniapp.foodshare.dto.AdminProductListResponse;
import com.miniapp.foodshare.dto.CreateProductRequest;
import com.miniapp.foodshare.dto.UpdateProductRequest;
import com.miniapp.foodshare.entity.Product;
import com.miniapp.foodshare.entity.Shop;
import com.miniapp.foodshare.repo.ProductRepository;
import com.miniapp.foodshare.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    /**
     * Lấy danh sách tất cả sản phẩm
     */
    @Transactional(readOnly = true)
    public Result<Page<AdminProductListResponse>> getAllProducts(Pageable pageable) {
        try {
            Page<Product> products = productRepository.findAll(pageable);
            
            Page<AdminProductListResponse> response = products.map(product -> {
                // Lấy thông tin cửa hàng
                String shopName = "Unknown Shop";
                Optional<Shop> shopOptional = shopRepository.findById(product.getShopId());
                if (shopOptional.isPresent()) {
                    shopName = shopOptional.get().getName();
                }
                
                return AdminProductListResponse.builder()
                        .id(product.getId())
                        .shopId(product.getShopId())
                        .shopName(shopName)
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
            });

            log.info("Retrieved all products: totalElements={}", response.getTotalElements());
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error getting all products", e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi lấy danh sách sản phẩm");
        }
    }

    /**
     * Lấy chi tiết sản phẩm theo ID
     */
    @Transactional(readOnly = true)
    public Result<AdminProductDetailResponse> getProductDetail(Integer productId) {
        try {
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isEmpty()) {
                log.warn("Product not found: productId={}", productId);
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy sản phẩm");
            }

            Product product = productOptional.get();
            
            // Lấy thông tin cửa hàng
            String shopName = "Unknown Shop";
            String shopAddress = "Unknown Address";
            String shopPhone = "Unknown Phone";
            
            Optional<Shop> shopOptional = shopRepository.findById(product.getShopId());
            if (shopOptional.isPresent()) {
                Shop shop = shopOptional.get();
                shopName = shop.getName();
                shopAddress = shop.getAddress();
                shopPhone = shop.getPhone();
            }

            AdminProductDetailResponse response = AdminProductDetailResponse.builder()
                    .id(product.getId())
                    .shopId(product.getShopId())
                    .shopName(shopName)
                    .shopAddress(shopAddress)
                    .shopPhone(shopPhone)
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

            log.info("Retrieved product detail: productId={}, shopName={}", productId, shopName);
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error getting product detail: productId={}", productId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi lấy chi tiết sản phẩm");
        }
    }

    /**
     * Lấy danh sách sản phẩm trong cửa hàng
     */
    @Transactional(readOnly = true)
    public Result<Page<AdminProductListResponse>> getProductsByShop(Integer shopId, Pageable pageable) {
        try {
            // Kiểm tra cửa hàng có tồn tại không
            Optional<Shop> shopOptional = shopRepository.findById(shopId);
            if (shopOptional.isEmpty()) {
                log.warn("Shop not found: shopId={}", shopId);
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy cửa hàng");
            }

            Page<Product> products = productRepository.findByShopId(shopId, pageable);
            
            Page<AdminProductListResponse> response = products.map(product -> {
                String shopName = shopOptional.get().getName();
                
                return AdminProductListResponse.builder()
                        .id(product.getId())
                        .shopId(product.getShopId())
                        .shopName(shopName)
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
            });

            log.info("Retrieved products by shop: shopId={}, totalElements={}", shopId, response.getTotalElements());
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error getting products by shop: shopId={}", shopId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi lấy danh sách sản phẩm theo cửa hàng");
        }
    }

    /**
     * Cập nhật sản phẩm
     */
    @Transactional
    public Result<AdminProductDetailResponse> updateProduct(Integer productId, UpdateProductRequest request) {
        try {
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isEmpty()) {
                log.warn("Product not found: productId={}", productId);
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy sản phẩm");
            }

            Product product = productOptional.get();
            
            // Cập nhật các trường không null
            if (request.getShopId() != null) product.setShopId(request.getShopId());
            if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());
            if (request.getName() != null) product.setName(request.getName());
            if (request.getDescription() != null) product.setDescription(request.getDescription());
            if (request.getPrice() != null) product.setPrice(request.getPrice());
            if (request.getOriginalPrice() != null) product.setOriginalPrice(request.getOriginalPrice());
            if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
            if (request.getDetailImageUrl() != null) product.setDetailImageUrl(request.getDetailImageUrl());
            if (request.getQuantityAvailable() != null) product.setQuantityAvailable(request.getQuantityAvailable());
            if (request.getQuantityPending() != null) product.setQuantityPending(request.getQuantityPending());
            if (request.getStatus() != null) product.setStatus(request.getStatus());

            Product updatedProduct = productRepository.save(product);
            
            // Lấy thông tin cửa hàng
            String shopName = "Unknown Shop";
            String shopAddress = "Unknown Address";
            String shopPhone = "Unknown Phone";
            
            Optional<Shop> shopOptional = shopRepository.findById(updatedProduct.getShopId());
            if (shopOptional.isPresent()) {
                Shop shop = shopOptional.get();
                shopName = shop.getName();
                shopAddress = shop.getAddress();
                shopPhone = shop.getPhone();
            }

            AdminProductDetailResponse response = AdminProductDetailResponse.builder()
                    .id(updatedProduct.getId())
                    .shopId(updatedProduct.getShopId())
                    .shopName(shopName)
                    .shopAddress(shopAddress)
                    .shopPhone(shopPhone)
                    .categoryId(updatedProduct.getCategoryId())
                    .name(updatedProduct.getName())
                    .description(updatedProduct.getDescription())
                    .price(updatedProduct.getPrice())
                    .originalPrice(updatedProduct.getOriginalPrice())
                    .imageUrl(updatedProduct.getImageUrl())
                    .detailImageUrl(updatedProduct.getDetailImageUrl())
                    .quantityAvailable(updatedProduct.getQuantityAvailable())
                    .quantityPending(updatedProduct.getQuantityPending())
                    .status(updatedProduct.getStatus())
                    .createdAt(updatedProduct.getCreatedAt())
                    .updatedAt(updatedProduct.getUpdatedAt())
                    .build();

            log.info("Product updated successfully: productId={}, name={}", 
                    updatedProduct.getId(), updatedProduct.getName());
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error updating product: productId={}", productId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi cập nhật sản phẩm");
        }
    }

    /**
     * Xóa sản phẩm
     */
    @Transactional
    public Result<String> deleteProduct(Integer productId) {
        try {
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isEmpty()) {
                log.warn("Product not found: productId={}", productId);
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy sản phẩm");
            }

            productRepository.deleteById(productId);

            log.info("Product deleted successfully: productId={}", productId);
            return Result.success("Xóa sản phẩm thành công");

        } catch (Exception e) {
            log.error("Error deleting product: productId={}", productId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi xóa sản phẩm");
        }
    }
}
