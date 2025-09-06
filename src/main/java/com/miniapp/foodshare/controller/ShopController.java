package com.miniapp.foodshare.controller;

import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.ShopDetailResponse;
import com.miniapp.foodshare.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/shops")
public class ShopController {
	private final ShopService shopService;

	@GetMapping("/{id}")
	public Result<ShopDetailResponse> getShop(@PathVariable("id") Integer id) {
		Result<ShopDetailResponse> result = shopService.getShopDetail(id);
		if (result.isSuccess()) {
			log.info("Shop detail retrieved successfully: shopId={}, productCount={}", id, result.getData().getProducts().size());
		} else {
			log.warn("Shop detail retrieval failed: shopId={}, code={}, message={}", id, result.getCode(), result.getMessage());
		}
		return result;
	}
} 