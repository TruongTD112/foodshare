# Hướng Dẫn Order Status Constants

## Tổng Quan

Hệ thống sử dụng constants từ `Constants.OrderStatus` thay vì enum để đảm bảo tương thích với database hiện tại.

## Constants Definition

### **Constants.OrderStatus**
```java
public static final class OrderStatus {
    public static final String PENDING = "1";      // Chờ xác nhận
    public static final String CONFIRMED = "2";    // Đã xác nhận
    public static final String COMPLETED = "4";    // Hoàn thành
    public static final String CANCELLED = "3";    // Đã hủy
    
    private OrderStatus() {}
}
```

## UpdateOrderStatusRequest

### **DTO Structure**
```java
@Value
@Builder
public class UpdateOrderStatusRequest {
    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "^(1|2|3|4)$", 
             message = "Trạng thái phải là 1 (PENDING), 2 (CONFIRMED), 3 (CANCELLED), hoặc 4 (COMPLETED)")
    String status;
}
```

### **Validation Rules**
- **NotBlank**: Không được để trống
- **Pattern**: Chỉ chấp nhận "1", "2", "3", "4"
- **Message**: Thông báo lỗi rõ ràng

## API Usage

### **1. Update Order Status**
```bash
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "status": "2"
  }'
```

### **2. Valid Status Values**
```json
{
  "status": "1"    // PENDING - Chờ xác nhận
}
{
  "status": "2"    // CONFIRMED - Đã xác nhận
}
{
  "status": "3"    // CANCELLED - Đã hủy
}
{
  "status": "4"    // COMPLETED - Hoàn thành
}
```

## Status Mapping

### **Database Values**
| Status | Value | Description |
|--------|-------|-------------|
| PENDING | "1" | Chờ xác nhận |
| CONFIRMED | "2" | Đã xác nhận |
| CANCELLED | "3" | Đã hủy |
| COMPLETED | "4" | Hoàn thành |

### **Business Logic**
```java
// Kiểm tra trạng thái
if (Constants.OrderStatus.PENDING.equals(order.getStatus())) {
    // Xử lý đơn hàng chờ xác nhận
}

// Cập nhật trạng thái
order.setStatus(Constants.OrderStatus.CONFIRMED);
```

## State Transition Rules

### **Valid Transitions**
```
PENDING (1) → CONFIRMED (2) | CANCELLED (3)
CONFIRMED (2) → COMPLETED (4) | CANCELLED (3)
COMPLETED (4) → (không thể chuyển)
CANCELLED (3) → (không thể chuyển)
```

### **Implementation Example**
```java
public boolean canTransitionTo(String currentStatus, String newStatus) {
    switch (currentStatus) {
        case Constants.OrderStatus.PENDING:
            return Constants.OrderStatus.CONFIRMED.equals(newStatus) || 
                   Constants.OrderStatus.CANCELLED.equals(newStatus);
        case Constants.OrderStatus.CONFIRMED:
            return Constants.OrderStatus.COMPLETED.equals(newStatus) || 
                   Constants.OrderStatus.CANCELLED.equals(newStatus);
        case Constants.OrderStatus.COMPLETED:
        case Constants.OrderStatus.CANCELLED:
            return false; // Không thể chuyển từ trạng thái cuối
        default:
            return false;
    }
}
```

## Service Implementation

### **OrderService Example**
```java
@Transactional
public Result<OrderResponse> updateOrderStatus(Integer orderId, UpdateOrderStatusRequest request) {
    Order order = orderRepository.findById(orderId).orElse(null);
    if (order == null) {
        return Result.error(ErrorCode.ORDER_NOT_FOUND, "Order not found");
    }
    
    // Kiểm tra transition hợp lệ
    if (!canTransitionTo(order.getStatus(), request.getStatus())) {
        return Result.error(ErrorCode.INVALID_STATUS_TRANSITION, 
            "Cannot transition from " + order.getStatus() + " to " + request.getStatus());
    }
    
    // Cập nhật trạng thái
    order.setStatus(request.getStatus());
    Order savedOrder = orderRepository.save(order);
    
    return Result.success(mapToResponse(savedOrder));
}
```

## Frontend Integration

### **Status Display**
```javascript
const statusMap = {
  "1": { text: "Chờ xác nhận", color: "#f59e0b", class: "status-pending" },
  "2": { text: "Đã xác nhận", color: "#3b82f6", class: "status-confirmed" },
  "3": { text: "Đã hủy", color: "#ef4444", class: "status-cancelled" },
  "4": { text: "Hoàn thành", color: "#059669", class: "status-completed" }
};

function getStatusInfo(status) {
  return statusMap[status] || { text: "Unknown", color: "#6b7280", class: "status-unknown" };
}
```

### **Status Options**
```javascript
const statusOptions = [
  { value: "1", text: "Chờ xác nhận" },
  { value: "2", text: "Đã xác nhận" },
  { value: "3", text: "Đã hủy" },
  { value: "4", text: "Hoàn thành" }
];
```

## Error Handling

### **1. Invalid Status**
```json
{
  "status": "5"
}
```
**Response:**
```json
{
  "success": false,
  "code": "400",
  "message": "Trạng thái phải là 1 (PENDING), 2 (CONFIRMED), 3 (CANCELLED), hoặc 4 (COMPLETED)"
}
```

### **2. Invalid Transition**
```json
{
  "success": false,
  "code": "400",
  "message": "Cannot transition from 4 to 1"
}
```

## Testing

### **1. Unit Tests**
```java
@Test
void testValidStatusValues() {
    assertTrue(Constants.OrderStatus.PENDING.equals("1"));
    assertTrue(Constants.OrderStatus.CONFIRMED.equals("2"));
    assertTrue(Constants.OrderStatus.CANCELLED.equals("3"));
    assertTrue(Constants.OrderStatus.COMPLETED.equals("4"));
}

@Test
void testStatusTransition() {
    assertTrue(canTransitionTo(Constants.OrderStatus.PENDING, Constants.OrderStatus.CONFIRMED));
    assertFalse(canTransitionTo(Constants.OrderStatus.COMPLETED, Constants.OrderStatus.PENDING));
}
```

### **2. Integration Tests**
```bash
# Test valid status update
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"status": "2"}'

# Test invalid status
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"status": "5"}'
```

## Database Integration

### **1. Entity Mapping**
```java
@Entity
public class Order {
    @Column(name = "status")
    private String status; // Lưu dưới dạng string "1", "2", "3", "4"
}
```

### **2. Repository Queries**
```java
// Tìm đơn hàng theo trạng thái
List<Order> findByStatus(String status);

// Tìm đơn hàng chờ xác nhận
List<Order> findByStatus(Constants.OrderStatus.PENDING);

// Tìm đơn hàng đã hoàn thành
List<Order> findByStatus(Constants.OrderStatus.COMPLETED);
```

## Migration

### **1. Từ Text Status**
```java
// Old way
String status = "pending";

// New way
String status = Constants.OrderStatus.PENDING;
```

### **2. Database Migration**
```sql
-- Update existing records
UPDATE orders SET status = '1' WHERE status = 'pending';
UPDATE orders SET status = '2' WHERE status = 'confirmed';
UPDATE orders SET status = '3' WHERE status = 'cancelled';
UPDATE orders SET status = '4' WHERE status = 'completed';
```

## Best Practices

1. **Luôn sử dụng constants** thay vì hardcode string
2. **Validate status values** trước khi xử lý
3. **Check transition rules** trước khi update
4. **Log status changes** để audit
5. **Handle edge cases** (final status, invalid transitions)
6. **Use meaningful variable names** cho constants
7. **Document business rules** cho từng transition
