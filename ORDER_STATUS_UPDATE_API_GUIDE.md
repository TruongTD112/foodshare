# Hướng Dẫn Order Status Update API

## 🎯 **Mục tiêu**
Hướng dẫn sử dụng API cập nhật trạng thái đơn hàng - một API duy nhất để quản lý tất cả các trạng thái đơn hàng.

## 📋 **API Endpoint**

### **Cập nhật trạng thái đơn hàng**
```http
PUT /orders/{id}/status
Content-Type: application/json
Authorization: Bearer {token}
```

## 🔧 **CURL Commands**

### **1. Cập nhật trạng thái đơn hàng**
```bash
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "status": "confirmed"
  }'
```

### **2. Các trạng thái có thể cập nhật**
```bash
# Pending → Confirmed
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"status": "confirmed"}'

# Confirmed → Preparing
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"status": "preparing"}'

# Preparing → Ready
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"status": "ready"}'

# Ready → Completed
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"status": "completed"}'

# Pending → Cancelled
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"status": "cancelled"}'
```

## 📊 **Request Body Format**

### **UpdateOrderStatusRequest:**
```json
{
  "status": "confirmed|preparing|ready|completed|cancelled"
}
```

## 📊 **Response Format**

### **Success Response:**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,
    "userId": 1,
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "status": "confirmed",
    "pickupTime": "2024-01-15T14:30:00",
    "expiresAt": "2024-01-15T15:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  }
}
```

### **Error Response:**
```json
{
  "success": false,
  "code": "400",
  "message": "Cannot change status from completed to pending"
}
```

## 🔄 **Status Transition Rules**

### **Quy tắc chuyển đổi trạng thái:**

| Từ trạng thái | Có thể chuyển sang |
|---------------|-------------------|
| `pending` | `confirmed`, `preparing`, `ready`, `completed`, `cancelled` |
| `confirmed` | `preparing`, `ready`, `completed`, `cancelled` |
| `preparing` | `ready`, `completed`, `cancelled` |
| `ready` | `completed`, `cancelled` |
| `completed` | ❌ (không thể thay đổi) |
| `cancelled` | ❌ (không thể thay đổi) |

### **Sơ đồ chuyển đổi trạng thái:**
```
pending
  ├── confirmed
  │   ├── preparing
  │   │   ├── ready
  │   │   │   └── completed
  │   │   └── cancelled
  │   ├── ready
  │   │   ├── completed
  │   │   └── cancelled
  │   ├── completed
  │   └── cancelled
  ├── preparing
  │   ├── ready
  │   │   ├── completed
  │   │   └── cancelled
  │   ├── completed
  │   └── cancelled
  ├── ready
  │   ├── completed
  │   └── cancelled
  ├── completed
  └── cancelled
```

## 🧪 **Test Cases**

### **1. Test thành công - Workflow hoàn chỉnh**
```bash
# 1. Tạo đơn hàng
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "pickupTime": "2024-01-15T14:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  }'

# 2. Pending → Confirmed
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"status": "confirmed"}'

# 3. Confirmed → Preparing
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"status": "preparing"}'

# 4. Preparing → Ready
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"status": "ready"}'

# 5. Ready → Completed
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"status": "completed"}'
```

### **2. Test lỗi - Trạng thái không hợp lệ**
```bash
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"status": "invalid_status"}'
```

### **3. Test lỗi - Chuyển đổi không hợp lệ**
```bash
# Từ completed không thể chuyển về pending
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"status": "pending"}'
```

### **4. Test lỗi - Đơn hàng không tồn tại**
```bash
curl -X PUT "http://localhost:8080/orders/999/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"status": "confirmed"}'
```

### **5. Test lỗi - Thiếu trạng thái**
```bash
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{}'
```

## 🔍 **Validation Rules**

### **1. UpdateOrderStatusRequest:**
- `status`: Bắt buộc, phải là một trong: `pending`, `confirmed`, `preparing`, `ready`, `completed`, `cancelled`

### **2. Business Rules:**
- Đơn hàng phải tồn tại
- Chuyển đổi trạng thái phải hợp lệ theo quy tắc
- Không thể thay đổi trạng thái đã `completed` hoặc `cancelled`
- Cần xác thực (token)

## 📝 **Dữ liệu mẫu**

### **Các trạng thái đơn hàng:**
```json
// Pending - Đơn hàng mới tạo
{"status": "pending"}

// Confirmed - Đơn hàng đã xác nhận
{"status": "confirmed"}

// Preparing - Đang chuẩn bị
{"status": "preparing"}

// Ready - Sẵn sàng lấy
{"status": "ready"}

// Completed - Hoàn thành
{"status": "completed"}

// Cancelled - Đã hủy
{"status": "cancelled"}
```

## 🚀 **Quick Start**

### **1. Chạy test script:**
```bash
chmod +x test_order_status_update.sh
./test_order_status_update.sh
```

### **2. Import Postman collection:**
- `Order_Status_Update_Collection.postman_collection.json`

### **3. Test thủ công:**
```bash
# 1. Tạo đơn hàng
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"shopId": 1, "productId": 1, "quantity": 2, "pickupTime": "2024-01-15T14:30:00", "unitPrice": 150000.00, "totalPrice": 300000.00}'

# 2. Cập nhật trạng thái
curl -X PUT "http://localhost:8080/orders/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"status": "confirmed"}'
```

## ⚠️ **Lưu ý quan trọng**

### **1. Xác thực:**
- API yêu cầu token xác thực
- Token được truyền qua header `Authorization: Bearer {token}`

### **2. Business Logic:**
- Khi chuyển sang `completed`: Cập nhật thống kê bán hàng và giảm `quantityPending`
- Khi chuyển sang `cancelled`: Hoàn trả `quantityAvailable` và giảm `quantityPending`

### **3. Trạng thái cuối:**
- `completed` và `cancelled` là trạng thái cuối, không thể thay đổi

### **4. Validation:**
- Kiểm tra chuyển đổi trạng thái hợp lệ
- Kiểm tra đơn hàng tồn tại
- Kiểm tra quyền truy cập

## 🔧 **Postman Testing**

### **1. Import collection:**
1. Mở Postman
2. Click **Import** → **Upload Files**
3. Chọn `Order_Status_Update_Collection.postman_collection.json`
4. Click **Import**

### **2. Test workflow:**
1. **Create Order** - Tạo đơn hàng mới
2. **Update Status** - Cập nhật trạng thái theo thứ tự
3. **Get Order List** - Xem danh sách đơn hàng
4. **Error Testing** - Test các trường hợp lỗi

### **3. Environment Variables:**
- `base_url`: http://localhost:8080
- `order_id`: ID của đơn hàng (tự động set)
- `user_token`: Token xác thực

## ✅ **Kết luận**

**Order Status Update API đã sẵn sàng!**

- ✅ **Single API** - Một API duy nhất cho tất cả trạng thái
- ✅ **Status Validation** - Kiểm tra chuyển đổi trạng thái hợp lệ
- ✅ **Business Logic** - Xử lý logic nghiệp vụ khi thay đổi trạng thái
- ✅ **Error Handling** - Xử lý lỗi đầy đủ
- ✅ **Authentication** - Yêu cầu xác thực
- ✅ **Flexible** - Hỗ trợ tất cả các trạng thái đơn hàng

API cập nhật trạng thái đơn hàng đã sẵn sàng! 🎉✨
