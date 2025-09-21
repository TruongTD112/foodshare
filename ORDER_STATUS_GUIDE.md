# Hướng Dẫn OrderStatus Enum

## Tổng Quan

Thay vì sử dụng text status, hệ thống sử dụng `OrderStatus` enum để đảm bảo type safety và validation chặt chẽ.

## OrderStatus Enum

### **Các Trạng Thái**
```java
public enum OrderStatus {
    PENDING("PENDING", "Chờ xác nhận"),
    CONFIRMED("CONFIRMED", "Đã xác nhận"),
    PREPARING("PREPARING", "Đang chuẩn bị"),
    READY("READY", "Sẵn sàng"),
    COMPLETED("COMPLETED", "Hoàn thành"),
    CANCELLED("CANCELLED", "Đã hủy");
}
```

### **Cấu Trúc**
- **code**: String code cho database (PENDING, CONFIRMED, ...)
- **description**: Mô tả tiếng Việt cho UI

## Sử Dụng

### **1. UpdateOrderStatusRequest**
```java
@Value
@Builder
public class UpdateOrderStatusRequest {
    @NotNull(message = "Trạng thái không được để trống")
    OrderStatus status;
}
```

### **2. JSON Request**
```json
{
  "status": "CONFIRMED"
}
```

### **3. Validation**
- **Type Safety**: Compile-time checking
- **Enum Validation**: Chỉ chấp nhận giá trị enum hợp lệ
- **Null Check**: `@NotNull` validation

## State Transition

### **Quy Tắc Chuyển Trạng Thái**
```
PENDING → CONFIRMED | CANCELLED
CONFIRMED → PREPARING | CANCELLED
PREPARING → READY | CANCELLED
READY → COMPLETED | CANCELLED
COMPLETED → (không thể chuyển)
CANCELLED → (không thể chuyển)
```

### **Kiểm Tra Transition**
```java
OrderStatus currentStatus = OrderStatus.PENDING;
OrderStatus newStatus = OrderStatus.CONFIRMED;

if (currentStatus.canTransitionTo(newStatus)) {
    // Cho phép chuyển trạng thái
    order.setStatus(newStatus);
} else {
    // Từ chối chuyển trạng thái
    throw new InvalidStatusTransitionException();
}
```

## Helper Methods

### **1. Tìm Theo Code**
```java
OrderStatus status = OrderStatus.fromCode("PENDING");
```

### **2. Kiểm Tra Hợp Lệ**
```java
boolean isValid = OrderStatus.isValid("PENDING");
```

### **3. Kiểm Tra Trạng Thái Cuối**
```java
boolean isFinal = status.isFinalStatus();
// true nếu COMPLETED hoặc CANCELLED
```

### **4. Kiểm Tra Trạng Thái Hoạt Động**
```java
boolean isActive = status.isActiveStatus();
// true nếu PENDING, CONFIRMED, PREPARING, READY
```

## API Usage

### **1. Update Order Status**
```bash
curl -X PUT "http://localhost:8080/api/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "status": "CONFIRMED"
  }'
```

### **2. Valid Status Values**
```json
{
  "status": "PENDING"      // Chờ xác nhận
}
{
  "status": "CONFIRMED"    // Đã xác nhận
}
{
  "status": "PREPARING"    // Đang chuẩn bị
}
{
  "status": "READY"        // Sẵn sàng
}
{
  "status": "COMPLETED"    // Hoàn thành
}
{
  "status": "CANCELLED"    // Đã hủy
}
```

## Error Handling

### **1. Invalid Status**
```json
{
  "status": "INVALID_STATUS"
}
```
**Response:**
```json
{
  "success": false,
  "code": "400",
  "message": "Invalid order status: INVALID_STATUS"
}
```

### **2. Invalid Transition**
```json
{
  "success": false,
  "code": "400",
  "message": "Cannot transition from COMPLETED to PENDING"
}
```

## Database Integration

### **1. Entity Mapping**
```java
@Entity
public class Order {
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
```

### **2. Repository Queries**
```java
// Tìm đơn hàng theo trạng thái
List<Order> findByStatus(OrderStatus status);

// Tìm đơn hàng đang hoạt động
List<Order> findByStatusIn(OrderStatus... statuses);
```

## Frontend Integration

### **1. Status Display**
```javascript
const statusMap = {
  PENDING: "Chờ xác nhận",
  CONFIRMED: "Đã xác nhận",
  PREPARING: "Đang chuẩn bị",
  READY: "Sẵn sàng",
  COMPLETED: "Hoàn thành",
  CANCELLED: "Đã hủy"
};

function getStatusText(status) {
  return statusMap[status] || status;
}
```

### **2. Status Colors**
```css
.status-pending { color: #f59e0b; }
.status-confirmed { color: #3b82f6; }
.status-preparing { color: #8b5cf6; }
.status-ready { color: #10b981; }
.status-completed { color: #059669; }
.status-cancelled { color: #ef4444; }
```

## Testing

### **1. Unit Tests**
```java
@Test
void testStatusTransition() {
    assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED));
    assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.PENDING));
}

@Test
void testFromCode() {
    assertEquals(OrderStatus.PENDING, OrderStatus.fromCode("PENDING"));
    assertThrows(IllegalArgumentException.class, () -> OrderStatus.fromCode("INVALID"));
}
```

### **2. Integration Tests**
```java
@Test
void testUpdateOrderStatus() {
    UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
        .status(OrderStatus.CONFIRMED)
        .build();
    
    // Test API call
    Result<OrderResponse> result = orderService.updateStatus(1, request);
    assertTrue(result.isSuccess());
}
```

## Migration

### **1. Từ String Status**
```java
// Old way
String status = "pending";

// New way
OrderStatus status = OrderStatus.PENDING;
```

### **2. Database Migration**
```sql
-- Update existing records
UPDATE orders SET status = 'PENDING' WHERE status = 'pending';
UPDATE orders SET status = 'CONFIRMED' WHERE status = 'confirmed';
-- ... etc
```

## Best Practices

1. **Luôn sử dụng enum** thay vì string
2. **Validate transition** trước khi update
3. **Log status changes** để audit
4. **Handle edge cases** (final status, invalid transitions)
5. **Use helper methods** để kiểm tra trạng thái
6. **Document business rules** cho từng transition
