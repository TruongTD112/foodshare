package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Constants;
import com.miniapp.foodshare.dto.OrderCreateRequest;
import com.miniapp.foodshare.dto.OrderResponse;
import com.miniapp.foodshare.dto.PagedResult;
import com.miniapp.foodshare.dto.ShopOrderListRequest;
import com.miniapp.foodshare.dto.ShopOrderResponse;
import com.miniapp.foodshare.dto.UpdateOrderStatusRequest;
import com.miniapp.foodshare.entity.CustomerUser;
import com.miniapp.foodshare.entity.Order;
import com.miniapp.foodshare.entity.Product;
import com.miniapp.foodshare.entity.Shop;
import com.miniapp.foodshare.entity.ShopMember;
import com.miniapp.foodshare.repo.CustomerUserRepository;
import com.miniapp.foodshare.repo.OrderRepository;
import com.miniapp.foodshare.repo.ProductRepository;
import com.miniapp.foodshare.repo.ProductSalesStatsRepository;
import com.miniapp.foodshare.repo.ShopMemberRepository;
import com.miniapp.foodshare.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final ProductSalesStatsRepository productSalesStatsRepository;
	private final ShopRepository shopRepository;
	private final CustomerUserRepository customerUserRepository;
	private final ShopMemberRepository shopMemberRepository;
	
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

	/**
	 * Cập nhật trạng thái đơn hàng (API duy nhất)
	 * Hỗ trợ tất cả các trạng thái: pending, confirmed, preparing, ready, completed, cancelled
	 */
	@Transactional
	public Result<OrderResponse> updateOrderStatus(Integer orderId, UpdateOrderStatusRequest request) {
		try {
			// Validate order exists
			Order order = orderRepository.findById(orderId).orElse(null);
			if (order == null) {
				log.warn("Order not found: orderId={}", orderId);
				return Result.error(ErrorCode.ORDER_NOT_FOUND, "Order not found");
			}

			String currentStatus = order.getStatus();
			String newStatus = request.getStatus();

			// Validate status transition
			if (!isValidStatusTransition(currentStatus, newStatus)) {
				log.warn("Invalid status transition: orderId={}, from={}, to={}", orderId, currentStatus, newStatus);
				return Result.error(ErrorCode.INVALID_ORDER_STATUS, 
					String.format("Cannot change status from %s to %s", currentStatus, newStatus));
			}

			// Update order status
			order.setStatus(newStatus);
			Order savedOrder = orderRepository.save(order);

			// Handle business logic based on status change
			handleStatusChangeBusinessLogic(order, currentStatus, newStatus);

			OrderResponse response = map(savedOrder);
			log.info("Order status updated successfully: orderId={}, from={}, to={}", 
				orderId, currentStatus, newStatus);
			return Result.success(response);

		} catch (Exception e) {
			log.error("Error updating order status: orderId={}, status={}", orderId, request.getStatus(), e);
			return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to update order status");
		}
	}

	/**
	 * Kiểm tra chuyển đổi trạng thái có hợp lệ không
	 */
	private boolean isValidStatusTransition(String currentStatus, String newStatus) {
		if (currentStatus == null || newStatus == null) {
			return false;
		}

		// Không thể thay đổi trạng thái đã completed hoặc cancelled
		if (Constants.OrderStatus.COMPLETED.equals(currentStatus) || 
			Constants.OrderStatus.CANCELLED.equals(currentStatus)) {
			return false;
		}

		// Có thể chuyển sang bất kỳ trạng thái nào từ pending
		if (Constants.OrderStatus.PENDING.equals(currentStatus)) {
			return true;
		}

		return false;
	}

	/**
	 * Xử lý logic nghiệp vụ khi thay đổi trạng thái
	 */
	private void handleStatusChangeBusinessLogic(Order order, String oldStatus, String newStatus) {
		// Khi chuyển sang completed, cập nhật thống kê bán hàng
		if (Constants.OrderStatus.COMPLETED.equals(newStatus)) {
			updateSalesStats(order.getProductId(), order.getQuantity());
			
			// Giảm quantityPending trong Product
			Product product = productRepository.findById(order.getProductId()).orElse(null);
			if (product != null) {
				Integer currentPending = product.getQuantityPending() != null ? product.getQuantityPending() : 0;
				product.setQuantityPending(currentPending - order.getQuantity());
				productRepository.save(product);
			}
		}

		// Khi chuyển sang cancelled, hoàn trả quantity
		if (Constants.OrderStatus.CANCELLED.equals(newStatus)) {
			Product product = productRepository.findById(order.getProductId()).orElse(null);
			if (product != null) {
				Integer currentAvailable = product.getQuantityAvailable() != null ? product.getQuantityAvailable() : 0;
				Integer currentPending = product.getQuantityPending() != null ? product.getQuantityPending() : 0;
				
				// Tăng lại quantityAvailable và giảm quantityPending
				product.setQuantityAvailable(currentAvailable + order.getQuantity());
				product.setQuantityPending(currentPending - order.getQuantity());
				productRepository.save(product);
			}
		}
	}

	/**
	 * Lấy danh sách đơn hàng theo shop
	 */
	@Transactional(readOnly = true)
	public Result<PagedResult<ShopOrderResponse>> getOrdersByShop(Integer sellerUserId, ShopOrderListRequest request) {
		try {
			// Kiểm tra shopId có được cung cấp không
			if (request.getShopId() == null) {
				log.warn("ShopId is required: sellerUserId={}", sellerUserId);
				return Result.error(ErrorCode.INVALID_REQUEST, "ShopId is required");
			}
			
			// Kiểm tra seller có phải là thành viên của shop không
			ShopMember shopMember = shopMemberRepository.findByBackofficeUserIdAndShopId(sellerUserId, request.getShopId())
				.stream().findFirst().orElse(null);
			if (shopMember == null) {
				log.warn("Seller is not a member of shop: sellerUserId={}, shopId={}", sellerUserId, request.getShopId());
				return Result.error(ErrorCode.FORBIDDEN, "You don't have permission to access this shop's orders");
			}
			
			// Validate pagination parameters
			int page = request.getPage() != null ? request.getPage() : 0;
			int size = request.getSize() != null ? request.getSize() : 20;
			
			if (page < 0) {
				log.warn("Invalid page number: page={}", page);
				return Result.error(ErrorCode.INVALID_PAGE_NUMBER, "Page number must be >= 0");
			}
			
			// Tạo Pageable với sorting
			String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
			String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";
			Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
			Pageable pageable = PageRequest.of(page, size, sort);
			
			// Lấy danh sách Order với filter theo ngày và status
			Page<Order> orders = orderRepository.findOrdersWithFilters(
				request.getShopId(), 
				request.getStatus(),
				request.getFromDate(), 
				request.getToDate(), 
				pageable
			);
			
			// Batch load tất cả thông tin cần thiết
			List<Order> orderList = orders.getContent();
			Map<Integer, CustomerUser> userMap = batchLoadUsers(orderList);
			Map<Integer, Product> productMap = batchLoadProducts(orderList);
			Map<Integer, Shop> shopMap = batchLoadShops(orderList);
			
			// Map sang ShopOrderResponse với thông tin đã load
			List<ShopOrderResponse> orderResponses = orderList.stream()
				.map(order -> mapToShopOrderResponseWithMaps(order, userMap, productMap, shopMap))
				.collect(Collectors.toList());
			
			// Tạo PagedResult
			long totalElements = orders.getTotalElements();
			int totalPages = orders.getTotalPages();
			
			PagedResult<ShopOrderResponse> result = PagedResult.<ShopOrderResponse>builder()
				.content(orderResponses)
				.page(page)
				.size(size)
				.totalElements(totalElements)
				.totalPages(totalPages)
				.hasNext(page < totalPages - 1)
				.hasPrevious(page > 0)
				.build();
			
			log.info("Shop orders retrieved successfully: shopId={}, sellerUserId={}, count={}", 
				request.getShopId(), sellerUserId, totalElements);
			return Result.success(result);
			
		} catch (Exception e) {
			log.error("Error getting shop orders: sellerUserId={}, shopId={}", sellerUserId, request.getShopId(), e);
			return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to get shop orders");
		}
	}

	/**
	 * Cập nhật trạng thái đơn hàng cho seller
	 */
	@Transactional
	public Result<ShopOrderResponse> updateOrderStatusForSeller(Integer orderId, UpdateOrderStatusRequest request, Integer sellerUserId) {
		try {
			// Validate order exists
			Order order = orderRepository.findById(orderId).orElse(null);
			if (order == null) {
				log.warn("Order not found: orderId={}", orderId);
				return Result.error(ErrorCode.ORDER_NOT_FOUND, "Order not found");
			}

			// Kiểm tra seller có phải là thành viên của shop không
			ShopMember shopMember = shopMemberRepository.findByBackofficeUserIdAndShopId(sellerUserId, order.getShopId())
				.stream().findFirst().orElse(null);
			if (shopMember == null) {
				log.warn("Seller is not a member of shop: sellerUserId={}, shopId={}", sellerUserId, order.getShopId());
				return Result.error(ErrorCode.FORBIDDEN, "You don't have permission to update this order");
			}

			String currentStatus = order.getStatus();
			String newStatus = request.getStatus();

			// Validate status transition
			if (!isValidStatusTransition(currentStatus, newStatus)) {
				log.warn("Invalid status transition: orderId={}, from={}, to={}", orderId, currentStatus, newStatus);
				return Result.error(ErrorCode.INVALID_ORDER_STATUS, 
					String.format("Cannot change status from %s to %s", currentStatus, newStatus));
			}

			// Update order status
			order.setStatus(newStatus);
			Order savedOrder = orderRepository.save(order);

			// Handle business logic based on status change
			handleStatusChangeBusinessLogic(order, currentStatus, newStatus);

			// Map to ShopOrderResponse
			ShopOrderResponse response = mapToShopOrderResponse(savedOrder);
			
			log.info("Order status updated successfully by seller: orderId={}, from={}, to={}, sellerId={}", 
				orderId, currentStatus, newStatus, sellerUserId);
			return Result.success(response);

		} catch (Exception e) {
			log.error("Error updating order status by seller: orderId={}, status={}, sellerId={}", 
				orderId, request.getStatus(), sellerUserId, e);
			return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to update order status");
		}
	}

	/**
	 * Lấy danh sách đơn hàng cho admin
	 * Có thể lấy tất cả đơn hàng hoặc filter theo shopId
	 */
	@Transactional(readOnly = true)
	public Result<PagedResult<ShopOrderResponse>> getAllOrdersForAdmin(ShopOrderListRequest request) {
		try {
			// Validate pagination parameters
			int page = request.getPage() != null ? request.getPage() : 0;
			int size = request.getSize() != null ? request.getSize() : 20;
			
			if (page < 0) {
				log.warn("Invalid page number: page={}", page);
				return Result.error(ErrorCode.INVALID_PAGE_NUMBER, "Page number must be >= 0");
			}
			
			// Tạo Pageable với sorting
			String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
			String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";
			Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
			Pageable pageable = PageRequest.of(page, size, sort);
			
			// Lấy danh sách Order với filter theo ngày và status
			Page<Order> orders = orderRepository.findOrdersWithFilters(
				request.getShopId(), 
				request.getStatus(),
				request.getFromDate(), 
				request.getToDate(), 
				pageable
			);
			
			// Batch load tất cả thông tin cần thiết
			List<Order> orderList = orders.getContent();
			Map<Integer, CustomerUser> userMap = batchLoadUsers(orderList);
			Map<Integer, Product> productMap = batchLoadProducts(orderList);
			Map<Integer, Shop> shopMap = batchLoadShops(orderList);
			
			// Map sang ShopOrderResponse với thông tin đã load
			List<ShopOrderResponse> orderResponses = orderList.stream()
				.map(order -> mapToShopOrderResponseWithMaps(order, userMap, productMap, shopMap))
				.collect(Collectors.toList());
			
			// Tạo PagedResult
			long totalElements = orders.getTotalElements();
			int totalPages = orders.getTotalPages();
			
			PagedResult<ShopOrderResponse> result = PagedResult.<ShopOrderResponse>builder()
				.content(orderResponses)
				.page(page)
				.size(size)
				.totalElements(totalElements)
				.totalPages(totalPages)
				.hasNext(page < totalPages - 1)
				.hasPrevious(page > 0)
				.build();
			
			log.info("Orders retrieved successfully for admin: shopId={}, count={}", 
				request.getShopId(), totalElements);
			return Result.success(result);
			
		} catch (Exception e) {
			log.error("Error getting orders for admin: shopId={}", request.getShopId(), e);
			return Result.error(ErrorCode.INTERNAL_ERROR, "Failed to get orders");
		}
	}

	
	/**
	 * Map Order sang ShopOrderResponse
	 */
	private ShopOrderResponse mapToShopOrderResponse(Order order) {
		// Lấy thông tin customer
		CustomerUser customer = customerUserRepository.findById(order.getUserId()).orElse(null);
		
		// Lấy thông tin product
		Product product = productRepository.findById(order.getProductId()).orElse(null);
		
		// Lấy thông tin shop
		Shop shop = shopRepository.findById(order.getShopId()).orElse(null);
		
		// Map status description
		String statusDescription = getStatusDescription(order.getStatus());
		
		return ShopOrderResponse.builder()
			.id(order.getId())
			.userId(order.getUserId())
			.customerName(customer != null ? customer.getName() : "N/A")
			.customerEmail(customer != null ? customer.getEmail() : "N/A")
			.customerPhone(customer != null ? customer.getPhoneNumber() : "N/A")
			.shopId(order.getShopId())
			.shopName(shop != null ? shop.getName() : "N/A")
			.productId(order.getProductId())
			.productName(product != null ? product.getName() : "N/A")
			.productImage(product != null ? product.getImageUrl() : null)
			.quantity(order.getQuantity())
			.status(order.getStatus())
			.pickupTime(order.getPickupTime())
			.expiresAt(order.getExpiresAt())
			.unitPrice(order.getUnitPrice())
			.totalPrice(order.getTotalPrice())
			.createdAt(order.getCreatedAt())
			.updatedAt(order.getUpdatedAt())
			.build();
	}

	/**
	 * Batch load tất cả User cần thiết
	 */
	private Map<Integer, CustomerUser> batchLoadUsers(List<Order> orders) {
		List<Integer> userIds = orders.stream()
			.map(Order::getUserId)
			.distinct()
			.collect(Collectors.toList());
		
		List<CustomerUser> users = customerUserRepository.findAllById(userIds);
		return users.stream()
			.collect(Collectors.toMap(CustomerUser::getId, Function.identity()));
	}

	/**
	 * Batch load tất cả Product cần thiết
	 */
	private Map<Integer, Product> batchLoadProducts(List<Order> orders) {
		List<Integer> productIds = orders.stream()
			.map(Order::getProductId)
			.distinct()
			.collect(Collectors.toList());
		
		List<Product> products = productRepository.findAllById(productIds);
		return products.stream()
			.collect(Collectors.toMap(Product::getId, Function.identity()));
	}

	/**
	 * Batch load tất cả Shop cần thiết
	 */
	private Map<Integer, Shop> batchLoadShops(List<Order> orders) {
		List<Integer> shopIds = orders.stream()
			.map(Order::getShopId)
			.distinct()
			.collect(Collectors.toList());
		
		List<Shop> shops = shopRepository.findAllById(shopIds);
		return shops.stream()
			.collect(Collectors.toMap(Shop::getId, Function.identity()));
	}

	/**
	 * Map Order sang ShopOrderResponse với Map đã load sẵn (tối ưu nhất)
	 */
	private ShopOrderResponse mapToShopOrderResponseWithMaps(Order order, 
			Map<Integer, CustomerUser> userMap, 
			Map<Integer, Product> productMap, 
			Map<Integer, Shop> shopMap) {
		
		CustomerUser customer = userMap.get(order.getUserId());
		Product product = productMap.get(order.getProductId());
		Shop shop = shopMap.get(order.getShopId());
		
		// Map status description
		String statusDescription = getStatusDescription(order.getStatus());
		
		return ShopOrderResponse.builder()
			.id(order.getId())
			.userId(order.getUserId())
			.customerName(customer != null ? customer.getName() : "N/A")
			.customerEmail(customer != null ? customer.getEmail() : "N/A")
			.customerPhone(customer != null ? customer.getPhoneNumber() : "N/A")
			.shopId(order.getShopId())
			.shopName(shop != null ? shop.getName() : "N/A")
			.productId(order.getProductId())
			.productName(product != null ? product.getName() : "N/A")
			.productImage(product != null ? product.getImageUrl() : null)
			.quantity(order.getQuantity())
			.status(order.getStatus())
			.statusDescription(statusDescription)
			.pickupTime(order.getPickupTime())
			.expiresAt(order.getExpiresAt())
			.unitPrice(order.getUnitPrice())
			.totalPrice(order.getTotalPrice())
			.createdAt(order.getCreatedAt())
			.updatedAt(order.getUpdatedAt())
			.build();
	}


	
	/**
	 * Lấy mô tả trạng thái đơn hàng
	 */
	private String getStatusDescription(String status) {
		if (status == null) return "Không xác định";
		
		switch (status) {
			case Constants.OrderStatus.PENDING:
				return "Đang chờ xác nhận";
			case Constants.OrderStatus.CONFIRMED:
				return "Đã xác nhận";
			case Constants.OrderStatus.COMPLETED:
				return "Hoàn thành";
			case Constants.OrderStatus.CANCELLED:
				return "Đã hủy";
			default:
				return "Không xác định";
		}
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
