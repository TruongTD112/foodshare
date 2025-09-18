package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Constants;
import com.miniapp.foodshare.dto.OrderCreateRequest;
import com.miniapp.foodshare.dto.OrderResponse;
import com.miniapp.foodshare.entity.Order;
import com.miniapp.foodshare.entity.Product;
import com.miniapp.foodshare.entity.Shop;
import com.miniapp.foodshare.repo.OrderRepository;
import com.miniapp.foodshare.repo.ProductRepository;
import com.miniapp.foodshare.repo.ProductSalesStatsRepository;
import com.miniapp.foodshare.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final ProductSalesStatsRepository productSalesStatsRepository;
	private final ShopRepository shopRepository;
	
	@Transactional
	public Result<OrderResponse> createOrder(OrderCreateRequest req) {
		if (req == null) {
			log.warn("Invalid request: request is null");
			return Result.error(ErrorCode.INVALID_REQUEST, "Invalid request");
		}
		if (req.getUserId() == null || req.getShopId() == null || req.getProductId() == null || req.getQuantity() == null) {
			log.warn("Missing required fields: userId={}, shopId={}, productId={}, quantity={}", 
				req.getUserId(), req.getShopId(), req.getProductId(), req.getQuantity());
			return Result.error(ErrorCode.MISSING_REQUIRED_FIELDS, "Missing required fields");
		}
		if (req.getQuantity() < Constants.Validation.MIN_QUANTITY || req.getQuantity() > Constants.Validation.MAX_QUANTITY) {
			log.warn("Invalid quantity: quantity={}", req.getQuantity());
			return Result.error(ErrorCode.INVALID_QUANTITY, "Quantity must be between " + Constants.Validation.MIN_QUANTITY + " and " + Constants.Validation.MAX_QUANTITY);
		}

		Product product = productRepository.findById(req.getProductId()).orElse(null);
		if (product == null) {
			log.warn("Product not found: productId={}", req.getProductId());
			return Result.error(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + req.getProductId());
		}
		if (!product.getShopId().equals(req.getShopId())) {
			log.warn("Product does not belong to shop: productId={}, shopId={}, productShopId={}", 
				req.getProductId(), req.getShopId(), product.getShopId());
			return Result.error(ErrorCode.PRODUCT_NOT_BELONG_TO_SHOP, "Product does not belong to shop");
		}
		if (product.getStatus() == null || !product.getStatus().trim().equals(Constants.ProductStatus.ACTIVE)) {
			log.warn("Product is not available: productId={}, status={}", req.getProductId(), product.getStatus());
			return Result.error(ErrorCode.PRODUCT_NOT_AVAILABLE, "Product is not available");
		}
		
		Shop shop = shopRepository.findById(req.getShopId()).orElse(null);
		if (shop == null) {
			log.warn("Shop not found: shopId={}", req.getShopId());
			return Result.error(ErrorCode.SHOP_NOT_FOUND, "Shop not found: " + req.getShopId());
		}
		if (shop.getStatus() == null || !shop.getStatus().trim().equals(Constants.ShopStatus.ACTIVE)) {
			log.warn("Shop is not active: shopId={}, status={}", req.getShopId(), shop.getStatus());
			return Result.error(ErrorCode.SHOP_NOT_ACTIVE, "Shop is not active");
		}

		Integer available = product.getQuantityAvailable() == null ? 0 : product.getQuantityAvailable();
		Integer pending = product.getQuantityPending() == null ? 0 : product.getQuantityPending();
		if (req.getQuantity() > (available - pending)) {
			log.warn("Insufficient stock: requested={}, available={}, pending={}", req.getQuantity(), available, pending);
			return Result.error(ErrorCode.INSUFFICIENT_STOCK, "Insufficient stock");
		}

		// Validate pickup time
		LocalDateTime pickupTime = req.getPickupTime();
		if (pickupTime == null) {
			log.warn("Pickup time is required: userId={}, productId={}", req.getUserId(), req.getProductId());
			return Result.error(ErrorCode.INVALID_REQUEST, "Pickup time is required");
		}
		
		// Validate pickup time is in the future
		LocalDateTime now = LocalDateTime.now();
		if (pickupTime.isBefore(now)) {
			log.warn("Pickup time must be in the future: userId={}, productId={}, pickupTime={}", 
					req.getUserId(), req.getProductId(), pickupTime);
			return Result.error(ErrorCode.INVALID_REQUEST, "Pickup time must be in the future");
		}
		
		// Validate unit price
		BigDecimal unitPrice = req.getUnitPrice();
		if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
			log.warn("Unit price must be greater than 0: userId={}, productId={}, unitPrice={}", 
					req.getUserId(), req.getProductId(), unitPrice);
			return Result.error(ErrorCode.INVALID_REQUEST, "Unit price must be greater than 0");
		}
		
		// Validate total price
		BigDecimal totalPrice = req.getTotalPrice();
		if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
			log.warn("Total price must be greater than 0: userId={}, productId={}, totalPrice={}", 
					req.getUserId(), req.getProductId(), totalPrice);
			return Result.error(ErrorCode.INVALID_REQUEST, "Total price must be greater than 0");
		}
		
		// Validate total price calculation
		BigDecimal expectedTotal = unitPrice.multiply(BigDecimal.valueOf(req.getQuantity()));
		if (totalPrice.compareTo(expectedTotal) != 0) {
			log.warn("Total price calculation mismatch: userId={}, productId={}, expected={}, actual={}", 
					req.getUserId(), req.getProductId(), expectedTotal, totalPrice);
			return Result.error(ErrorCode.INVALID_REQUEST, "Total price calculation is incorrect");
		}
		
		LocalDateTime expiresAt = pickupTime.plusMinutes(Constants.Time.ORDER_EXPIRY_MINUTES);

		Order order = Order.builder()
			.userId(req.getUserId())
			.shopId(req.getShopId())
			.productId(req.getProductId())
			.quantity(req.getQuantity())
			.status(Constants.OrderStatus.PENDING)
			.pickupTime(pickupTime)
			.expiresAt(expiresAt)
			.unitPrice(unitPrice)
			.totalPrice(totalPrice)
			.build();

		Order saved = orderRepository.save(order);

		product.setQuantityPending(pending + req.getQuantity());
		productRepository.save(product);

		OrderResponse response = OrderResponse.builder()
			.id(saved.getId())
			.userId(saved.getUserId())
			.shopId(saved.getShopId())
			.productId(saved.getProductId())
			.quantity(saved.getQuantity())
			.status(saved.getStatus())
			.pickupTime(saved.getPickupTime())
			.expiresAt(saved.getExpiresAt())
			.unitPrice(saved.getUnitPrice())
			.totalPrice(saved.getTotalPrice())
			.build();
			
		log.info("Order created successfully: orderId={}, userId={}, shopId={}, productId={}, quantity={}", 
			response.getId(), response.getUserId(), response.getShopId(), response.getProductId(), response.getQuantity());
		return Result.success(response);
	}

	/**
	 * Cập nhật thống kê bán hàng khi order được completed
	 * 
	 * @param productId ID sản phẩm
	 * @param quantity Số lượng đã bán
	 */
	@Transactional
	public void updateSalesStats(Integer productId, Integer quantity) {
		try {
			productSalesStatsRepository.updateSalesStats(productId, quantity);
			log.info("Sales stats updated: productId={}, quantity={}", productId, quantity);
		} catch (Exception e) {
			log.error("Failed to update sales stats: productId={}, quantity={}, error={}", 
					productId, quantity, e.getMessage());
		}
	}

	/**
	 * Xác nhận order (chuyển từ pending sang completed)
	 * Cập nhật thống kê bán hàng
	 */
	@Transactional
	public Result<OrderResponse> confirmOrder(Integer orderId) {
		Order order = orderRepository.findById(orderId).orElse(null);
		if (order == null) {
			log.warn("Order not found: orderId={}", orderId);
			return Result.error(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId);
		}

		if (!Constants.OrderStatus.PENDING.equals(order.getStatus())) {
			log.warn("Order cannot be confirmed: orderId={}, status={}", orderId, order.getStatus());
			return Result.error(ErrorCode.INVALID_ORDER_STATUS, "Only pending orders can be confirmed");
		}

		// Cập nhật status order
		order.setStatus(Constants.OrderStatus.COMPLETED);
		Order savedOrder = orderRepository.save(order);

		// Cập nhật thống kê bán hàng
		updateSalesStats(order.getProductId(), order.getQuantity());

		// Cập nhật quantity trong Product (chỉ giảm quantityPending, không cần giảm quantityAvailable)
		Product product = productRepository.findById(order.getProductId()).orElse(null);
		if (product != null) {
			Integer currentPending = product.getQuantityPending() != null ? product.getQuantityPending() : 0;
			
			// Chỉ giảm quantityPending vì quantityAvailable đã được giảm khi tạo order
			product.setQuantityPending(currentPending - order.getQuantity());
			productRepository.save(product);
		}

		OrderResponse response = map(savedOrder);
		log.info("Order confirmed successfully: orderId={}, productId={}, quantity={}", 
				orderId, order.getProductId(), order.getQuantity());
		return Result.success(response);
	}

	@Transactional(readOnly = true)
	public Result<List<OrderResponse>> listOrdersByUser(Integer userId, String status) {
		List<Order> orders;
		if (status == null || status.isBlank()) {
			orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
		} else {
			orders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status.trim());
		}
		List<OrderResponse> response = orders.stream()
			.map(this::map)
			.collect(Collectors.toList());
		log.info("Orders retrieved successfully: userId={}, status={}, count={}", userId, status, response.size());
		return Result.success(response);
	}

	@Transactional
	public Result<OrderResponse> cancelOrder(Integer orderId, Integer userId) {
		Order order = orderRepository.findById(orderId).orElse(null);
		if (order == null) {
			log.warn("Order not found: orderId={}", orderId);
			return Result.error(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId);
		}
		if (!order.getUserId().equals(userId)) {
			log.warn("Forbidden: orderId={}, userId={}, orderUserId={}", orderId, userId, order.getUserId());
			return Result.error(ErrorCode.FORBIDDEN, "Forbidden");
		}
		if (order.getStatus() == null) {
			log.warn("Invalid order status: orderId={}, status={}", orderId, order.getStatus());
			return Result.error(ErrorCode.INVALID_ORDER_STATUS, "Invalid order status");
		}
		String status = order.getStatus().trim().toLowerCase();
		if (Constants.OrderStatus.CANCELLED.equals(status)) {
			log.info("Order already cancelled: orderId={}", orderId);
			return Result.success(map(order));
		}
		if (!Constants.OrderStatus.PENDING.equals(status)) {
			log.warn("Order cannot be cancelled: orderId={}, status={}", orderId, status);
			return Result.error(ErrorCode.ORDER_CANNOT_BE_CANCELLED, "Only pending orders can be cancelled");
		}

		Product product = productRepository.findById(order.getProductId()).orElse(null);
		if (product == null) {
			log.warn("Product not found during order cancellation: productId={}", order.getProductId());
			return Result.error(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + order.getProductId());
		}
		Integer pending = product.getQuantityPending() == null ? 0 : product.getQuantityPending();
		int newPending = pending - (order.getQuantity() == null ? 0 : order.getQuantity());
		if (newPending < 0) newPending = 0;
		product.setQuantityPending(newPending);
		productRepository.save(product);

		order.setStatus(Constants.OrderStatus.CANCELLED);
		Order saved = orderRepository.save(order);
		OrderResponse response = map(saved);
		log.info("Order cancelled successfully: orderId={}, userId={}", orderId, userId);
		return Result.success(response);
	}

	private OrderResponse map(Order o) {
		return OrderResponse.builder()
			.id(o.getId())
			.userId(o.getUserId())
			.shopId(o.getShopId())
			.productId(o.getProductId())
			.quantity(o.getQuantity())
			.status(o.getStatus())
			.pickupTime(o.getPickupTime())
			.expiresAt(o.getExpiresAt())
			.unitPrice(o.getUnitPrice())
			.totalPrice(o.getTotalPrice())
			.build();
	}
}
