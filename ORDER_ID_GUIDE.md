# Hướng Dẫn Order ID trong API Create Order

## 🎯 **Mục tiêu**
Hướng dẫn về Order ID được trả về trong API create order.

## ✅ **Order ID đã được implement**

API create order **ĐÃ** trả về order ID cho client trong response.

### **Response Format:**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,                    // ← Order ID cho client
    "userId": 1,
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "status": "pending",
    "pickupTime": "2024-01-15T14:30:00",
    "expiresAt": "2024-01-15T15:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  }
}
```

## 🔍 **Chi tiết implementation**

### **1. OrderResponse DTO:**
```java
@Value
@Builder
public class OrderResponse {
    Integer id;                 // ← Order ID field
    Integer userId;
    Integer shopId;
    Integer productId;
    Integer quantity;
    String status;
    LocalDateTime pickupTime;
    LocalDateTime expiresAt;
    BigDecimal unitPrice;
    BigDecimal totalPrice;
}
```

### **2. OrderService.createOrder():**
```java
OrderResponse response = OrderResponse.builder()
    .id(saved.getId())          // ← Set order ID từ database
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
```

### **3. Database Entity:**
```java
@Entity
@Table(name = "`Order`")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;         // ← Auto-generated ID
    // ... other fields
}
```

## 🧪 **Test Order ID**

### **1. Chạy test script:**
```bash
chmod +x test_order_id.sh
./test_order_id.sh
```

### **2. Test thủ công:**
```bash
# 1. Tạo token
TOKEN_RESPONSE=$(curl -s -X POST "http://localhost:8080/auth/social" \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "providerId": "google_123",
    "email": "user1@example.com",
    "name": "User 1",
    "profilePictureUrl": "https://example.com/avatar1.jpg"
  }')

# 2. Extract token
TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.data.token')

# 3. Tạo đơn hàng
ORDER_RESPONSE=$(curl -s -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "pickupTime": "2024-01-15T14:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  }')

# 4. Extract order ID
ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.data.id')
echo "Order ID: $ORDER_ID"
```

## 📋 **Cách sử dụng Order ID**

### **1. Lưu Order ID:**
```javascript
// Frontend JavaScript
const response = await fetch('/orders', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify(orderData)
});

const result = await response.json();
const orderId = result.data.id;  // ← Lấy Order ID
console.log('Order created with ID:', orderId);
```

### **2. Sử dụng Order ID cho các API khác:**
```bash
# Hủy đơn hàng
curl -X DELETE "http://localhost:8080/orders/$ORDER_ID" \
  -H "Authorization: Bearer $TOKEN"

# Xem chi tiết đơn hàng (nếu có API)
curl -X GET "http://localhost:8080/orders/$ORDER_ID" \
  -H "Authorization: Bearer $TOKEN"
```

### **3. Lưu vào database:**
```sql
-- Order ID được tự động tạo và trả về
INSERT INTO `Order` (user_id, shop_id, product_id, quantity, status, pickup_time, expires_at, unit_price, total_price)
VALUES (1, 1, 1, 2, 'pending', '2024-01-15 14:30:00', '2024-01-15 15:30:00', 150000.00, 300000.00);

-- ID sẽ được auto-generated và trả về trong response
```

## 🔧 **Postman Testing**

### **1. Import Collection:**
- `Order_API_Collection.postman_collection.json`
- `Order_API_Environment.postman_environment.json`

### **2. Test Create Order:**
1. Chạy **Social Login** để tạo token
2. Chạy **Create Order - Pizza Margherita**
3. Kiểm tra response có field `id`
4. Copy Order ID để test các API khác

### **3. Sử dụng Order ID:**
1. Copy Order ID từ response
2. Paste vào environment variable `order_id`
3. Test **Cancel Order** với Order ID đó

## ⚠️ **Lưu ý quan trọng**

### **1. Order ID là unique:**
- Mỗi đơn hàng có ID duy nhất
- ID được auto-generated bởi database
- ID không thể thay đổi sau khi tạo

### **2. Order ID cần thiết cho:**
- Hủy đơn hàng (Cancel Order)
- Xem chi tiết đơn hàng
- Cập nhật trạng thái đơn hàng
- Tracking đơn hàng

### **3. Response structure:**
```json
{
  "success": true,           // ← API call thành công
  "code": "200",            // ← HTTP status code
  "message": "Success",     // ← Message mô tả
  "data": {                 // ← Data object chứa order info
    "id": 1,               // ← Order ID (QUAN TRỌNG)
    // ... other fields
  }
}
```

## ✅ **Kết luận**

**API create order ĐÃ trả về order ID cho client!**

- ✅ Order ID được trả về trong field `data.id`
- ✅ Order ID được auto-generated từ database
- ✅ Order ID có thể sử dụng cho các API khác
- ✅ Response format đầy đủ và rõ ràng

**Không cần thay đổi gì thêm!** 🎉✨
