package com.miniapp.foodshare.controller;

import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.dto.OrderCreateRequest;
import com.miniapp.foodshare.dto.OrderResponse;
import com.miniapp.foodshare.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
	private final OrderService orderService;

	@PostMapping
	public Result<OrderResponse> createOrder(@RequestBody OrderCreateRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Integer uid = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;
		if (uid == null) { 
			log.warn("Unauthorized access attempt to create order");
			return Result.error(ErrorCode.UNAUTHORIZED, "Unauthorized");
		}
		OrderCreateRequest effective = OrderCreateRequest.builder()
			.userId(uid)
			.shopId(request.getShopId())
			.productId(request.getProductId())
			.quantity(request.getQuantity())
			.pickupInMinutes(request.getPickupInMinutes())
			.build();
		Result<OrderResponse> result = orderService.createOrder(effective);
		if (result.isSuccess()) {
			log.info("Order created successfully: orderId={}, userId={}, shopId={}, productId={}", 
				result.getData().getId(), result.getData().getUserId(), result.getData().getShopId(), result.getData().getProductId());
		} else {
			log.warn("Order creation failed: code={}, message={}", result.getCode(), result.getMessage());
		}
		return result;
	}

	@GetMapping
	public Result<List<OrderResponse>> listMyOrders(@RequestParam(name = "status", required = false) String status) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Integer uid = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;
		if (uid == null) { 
			log.warn("Unauthorized access attempt to list orders");
			return Result.error(ErrorCode.UNAUTHORIZED, "Unauthorized");
		}
		Result<List<OrderResponse>> result = orderService.listOrdersByUser(uid, status);
		if (result.isSuccess()) {
			log.info("Orders retrieved successfully: userId={}, status={}, count={}", uid, status, result.getData().size());
		} else {
			log.warn("Order retrieval failed: code={}, message={}", result.getCode(), result.getMessage());
		}
		return result;
	}

	@DeleteMapping("/{id}")
	public Result<OrderResponse> cancelOrder(@PathVariable("id") Integer id) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Integer uid = auth != null && auth.getPrincipal() instanceof Integer ? (Integer) auth.getPrincipal() : null;
		if (uid == null) { 
			log.warn("Unauthorized access attempt to cancel order: orderId={}", id);
			return Result.error(ErrorCode.UNAUTHORIZED, "Unauthorized");
		}
		Result<OrderResponse> result = orderService.cancelOrder(id, uid);
		if (result.isSuccess()) {
			log.info("Order cancelled successfully: orderId={}, userId={}", id, uid);
		} else {
			log.warn("Order cancellation failed: orderId={}, code={}, message={}", id, result.getCode(), result.getMessage());
		}
		return result;
	}
}
