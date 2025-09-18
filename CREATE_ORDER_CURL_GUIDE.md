# Hướng Dẫn CURL Đặt Đơn Hàng

## 🎯 **Mục tiêu**
Hướng dẫn sử dụng cURL để đặt đơn hàng qua API.

## 📋 **API Endpoints**

### **1. Đặt đơn hàng**
```http
POST /orders
Content-Type: application/json
Authorization: Bearer <token>
```

### **2. Xem danh sách đơn hàng**
```http
GET /orders
Authorization: Bearer <token>
```

### **3. Hủy đơn hàng**
```http
DELETE /orders/{id}
Authorization: Bearer <token>
```

## 🔧 **CURL Commands**

### **1. Đặt đơn hàng cơ bản**
```bash
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "pickupInMinutes": 30
  }'
```

### **2. Đặt đơn hàng không có pickup time (default)**
```bash
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 1
  }'
```

### **3. Xem danh sách đơn hàng**
```bash
curl -X GET "http://localhost:8080/orders" \
  -H "Authorization: Bearer user_1"
```

### **4. Xem đơn hàng theo status**
```bash
curl -X GET "http://localhost:8080/orders?status=pending" \
  -H "Authorization: Bearer user_1"
```

### **5. Hủy đơn hàng**
```bash
curl -X DELETE "http://localhost:8080/orders/1" \
  -H "Authorization: Bearer user_1"
```

## 📊 **Request Body Format**

### **OrderCreateRequest:**
```json
{
  "shopId": 1,              // ID cửa hàng (bắt buộc)
  "productId": 1,           // ID sản phẩm (bắt buộc)
  "quantity": 2,            // Số lượng (bắt buộc)
  "pickupInMinutes": 30     // Thời gian nhận hàng (tùy chọn)
}
```

### **Fields:**
- **shopId**: ID cửa hàng (bắt buộc)
- **productId**: ID sản phẩm (bắt buộc)
- **quantity**: Số lượng đặt (bắt buộc, > 0)
- **pickupInMinutes**: Thời gian nhận hàng tính bằng phút từ hiện tại (tùy chọn)

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
    "status": "pending",
    "pickupTime": "2024-01-15T12:30:00",
    "expiresAt": "2024-01-15T13:30:00"
  }
}
```

### **Error Response:**
```json
{
  "success": false,
  "code": "400",
  "message": "Insufficient stock"
}
```

## 🧪 **Test Cases**

### **1. Test thành công**
```bash
# Đặt Pizza Margherita
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "pickupInMinutes": 30
  }'
```

### **2. Test lỗi - Không có token**
```bash
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 1
  }'
```

### **3. Test lỗi - Sản phẩm không tồn tại**
```bash
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 999,
    "quantity": 1
  }'
```

### **4. Test lỗi - Số lượng không hợp lệ**
```bash
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 0
  }'
```

## 📝 **Dữ liệu mẫu**

### **Sản phẩm có sẵn:**
- **Pizza Margherita** (ID: 1) - Shop: Pizza Corner (ID: 1)
- **Pizza Pepperoni** (ID: 2) - Shop: Pizza Corner (ID: 1)
- **Burger Deluxe** (ID: 4) - Shop: Burger King (ID: 2)
- **Cà phê đen** (ID: 7) - Shop: Cafe Central (ID: 3)

### **Token mẫu:**
- `user_1` - User ID: 1
- `user_2` - User ID: 2
- `user_3` - User ID: 3

## 🚀 **Quick Start**

### **1. Chạy script test nhanh:**
```bash
chmod +x test_create_order.sh
./test_create_order.sh
```

### **2. Chạy script test đầy đủ:**
```bash
chmod +x create_order_curl.sh
./create_order_curl.sh
```

### **3. Test thủ công:**
```bash
# Đặt đơn hàng
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"shopId": 1, "productId": 1, "quantity": 2, "pickupInMinutes": 30}'

# Xem đơn hàng
curl -X GET "http://localhost:8080/orders" \
  -H "Authorization: Bearer user_1"
```

## ⚠️ **Lưu ý**

### **1. Authentication:**
- Cần token hợp lệ trong header `Authorization`
- Format: `Bearer <token>`
- User ID được lấy từ token

### **2. Validation:**
- `quantity` phải > 0
- `shopId` và `productId` phải tồn tại
- Sản phẩm phải có đủ hàng trong kho

### **3. Business Logic:**
- Tự động set `userId` từ token
- Tính toán `pickupTime` từ `pickupInMinutes`
- Cập nhật `quantityPending` của sản phẩm
- Tạo `expiresAt` (pickupTime + 1 giờ)

## 🔍 **Debug**

### **1. Kiểm tra response:**
```bash
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{"shopId": 1, "productId": 1, "quantity": 1}' \
  -v
```

### **2. Kiểm tra logs:**
- Xem server logs để debug
- Check database để verify data

### **3. Test với Postman:**
- Import collection từ `FoodShare_API_Collection.postman_collection.json`
- Sử dụng environment variables

CURL đặt đơn hàng đã sẵn sàng! 🛒✨
