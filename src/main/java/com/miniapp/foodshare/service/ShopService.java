package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Constants;
import com.miniapp.foodshare.dto.ShopDetailResponse;
import com.miniapp.foodshare.entity.Product;
import com.miniapp.foodshare.entity.Shop;
import com.miniapp.foodshare.repo.ProductRepository;
import com.miniapp.foodshare.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {
	private final ShopRepository shopRepository;
	private final ProductRepository productRepository;

	@Transactional(readOnly = true)
	public Result<ShopDetailResponse> getShopDetail(Integer shopId) {
		Shop shop = shopRepository.findById(shopId).orElse(null);
		if (shop == null) {
			log.warn("Shop not found: shopId={}", shopId);
			return Result.error(ErrorCode.SHOP_NOT_FOUND, "Shop not found: " + shopId);
		}

		if (shop.getStatus() == null || !shop.getStatus().trim().equals(Constants.ShopStatus.ACTIVE)) {
			log.warn("Shop is not active: shopId={}, status={}", shopId, shop.getStatus());
			return Result.error(ErrorCode.SHOP_NOT_ACTIVE, "Shop is not active");
		}

		List<Product> products = productRepository.findByShopId(shopId);
		List<ShopDetailResponse.ProductItem> productItems = products.stream()
			.filter(p -> p.getStatus() != null && p.getStatus().trim().equals(Constants.ProductStatus.ACTIVE))
			.map(p -> ShopDetailResponse.ProductItem.builder()
				.id(p.getId())
				.categoryId(p.getCategoryId())
				.name(p.getName())
				.description(p.getDescription())
				.price(p.getPrice())
				.imageUrl(p.getImageUrl())
				.quantityAvailable(p.getQuantityAvailable())
				.quantityPending(p.getQuantityPending())
				.status(p.getStatus())
				.build())
			.collect(Collectors.toList());

		ShopDetailResponse response = ShopDetailResponse.builder()
			.id(shop.getId())
			.name(shop.getName())
			.address(shop.getAddress())
			.latitude(shop.getLatitude() != null ? shop.getLatitude().doubleValue() : null)
			.longitude(shop.getLongitude() != null ? shop.getLongitude().doubleValue() : null)
			.description(shop.getDescription())
			.rating(shop.getRating())
			.status(shop.getStatus())
			.products(productItems)
			.build();

		log.info("Shop detail retrieved successfully: shopId={}, productCount={}", shopId, productItems.size());
		return Result.success(response);
	}
} 