# Hướng Dẫn Sử Dụng Postman - Order APIs

## 🎯 **Mục tiêu**
Hướng dẫn sử dụng Postman collection để test các API đơn hàng.

## 📁 **Files cần import**

1. **`Order_API_Collection.postman_collection.json`** - Collection chứa tất cả APIs
2. **`Order_API_Environment.postman_environment.json`** - Environment variables

## 🚀 **Cách sử dụng**

### **1. Import Collection và Environment**

1. Mở Postman
2. Click **Import** → **Upload Files**
3. Chọn 2 files JSON đã tạo
4. Click **Import**

### **2. Setup Environment**

1. Chọn **Order API Environment** từ dropdown
2. Cập nhật `base_url` nếu cần: `http://localhost:8080`
3. Các variables khác sẽ được set tự động

### **3. Tạo Token**

#### **Bước 1: Tạo token cho User 1**
1. Vào folder **Authentication**
2. Chọn **Social Login - User 1**
3. Click **Send**
4. Copy `token` từ response
5. Paste vào environment variable `token`

#### **Bước 2: Tạo token cho các User khác**
- Lặp lại với **User 2** và **User 3**
- Lưu token vào `user2_token` và `user3_token`

## 📋 **APIs có sẵn**

### **1. Authentication APIs**
- **Social Login - User 1** - Tạo token cho User 1
- **Social Login - User 2** - Tạo token cho User 2  
- **Social Login - User 3** - Tạo token cho User 3

### **2. Order Management APIs**
- **Create Order - Pizza Margherita** - Đặt Pizza Margherita
- **Create Order - Pizza Pepperoni** - Đặt Pizza Pepperoni
- **Create Order - Burger Deluxe** - Đặt Burger Deluxe
- **Get All Orders** - Lấy tất cả đơn hàng
- **Get Orders by Status - Pending** - Lấy đơn hàng pending
- **Get Orders by Status - Completed** - Lấy đơn hàng completed
- **Get Orders by Status - Cancelled** - Lấy đơn hàng cancelled
- **Cancel Order** - Hủy đơn hàng

### **3. Error Testing APIs**
- **Create Order - Missing Token** - Test thiếu token
- **Create Order - Invalid Token** - Test token không hợp lệ
- **Create Order - Missing Pickup Time** - Test thiếu pickup time
- **Create Order - Invalid Price Calculation** - Test tính toán giá sai
- **Create Order - Past Pickup Time** - Test pickup time quá khứ

## 🔧 **Cách test**

### **1. Test cơ bản**
1. Tạo token (Social Login)
2. Đặt đơn hàng (Create Order)
3. Xem danh sách đơn hàng (Get All Orders)
4. Hủy đơn hàng (Cancel Order)

### **2. Test theo status**
1. Tạo nhiều đơn hàng
2. Test lọc theo status: pending, completed, cancelled

### **3. Test lỗi**
1. Test các trường hợp lỗi trong folder **Error Testing**
2. Kiểm tra response code và message

## 📊 **Request Body mẫu**

### **Create Order:**
```json
{
  "shopId": 1,
  "productId": 1,
  "quantity": 2,
  "pickupTime": "2024-01-15T14:30:00",
  "unitPrice": 150000.00,
  "totalPrice": 300000.00
}
```

### **Social Login:**
```json
{
  "provider": "google",
  "providerId": "google_123",
  "email": "user1@example.com",
  "name": "User 1",
  "profilePictureUrl": "https://example.com/avatar1.jpg"
}
```

## 🔍 **Response mẫu**

### **Create Order Success:**
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
    "pickupTime": "2024-01-15T14:30:00",
    "expiresAt": "2024-01-15T15:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  }
}
```

### **Get Orders Success:**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": [
    {
      "id": 1,
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
  ]
}
```

## ⚙️ **Environment Variables**

| Variable | Description | Example |
|----------|-------------|---------|
| `base_url` | Base URL của API | `http://localhost:8080` |
| `token` | JWT token hiện tại | `eyJhbGciOiJIUzI1NiJ9...` |
| `user1_token` | Token cho User 1 | `eyJhbGciOiJIUzI1NiJ9...` |
| `user2_token` | Token cho User 2 | `eyJhbGciOiJIUzI1NiJ9...` |
| `user3_token` | Token cho User 3 | `eyJhbGciOiJIUzI1NiJ9...` |
| `order_id` | ID đơn hàng để test | `1` |
| `pickup_time` | Thời gian pickup tự động | `2024-01-15T14:30:00` |
| `shop_id` | ID cửa hàng mặc định | `1` |
| `product_id` | ID sản phẩm mặc định | `1` |

## 🧪 **Test Scenarios**

### **Scenario 1: Happy Path**
1. Social Login → Get Token
2. Create Order → Success
3. Get All Orders → See Order
4. Cancel Order → Success

### **Scenario 2: Error Handling**
1. Create Order without Token → 401 Unauthorized
2. Create Order with Invalid Token → 401 Unauthorized
3. Create Order with Missing Fields → 400 Bad Request
4. Create Order with Invalid Data → 400 Bad Request

### **Scenario 3: Status Filtering**
1. Create Multiple Orders
2. Get Orders by Status Pending → See Pending Orders
3. Get Orders by Status Completed → See Completed Orders
4. Get Orders by Status Cancelled → See Cancelled Orders

## 🔧 **Pre-request Scripts**

Collection có pre-request script tự động tạo pickup time:

```javascript
// Auto-generate pickup time for next 30 minutes
const now = new Date();
const pickupTime = new Date(now.getTime() + 30 * 60000);
const pickupTimeString = pickupTime.toISOString().slice(0, 19);

// Set pickup time variable
pm.environment.set('pickup_time', pickupTimeString);

console.log('Auto-generated pickup time:', pickupTimeString);
```

## 📝 **Tips sử dụng**

### **1. Token Management**
- Luôn tạo token mới khi hết hạn
- Sử dụng environment variables để lưu token
- Test với nhiều user khác nhau

### **2. Data Testing**
- Test với các giá trị khác nhau
- Test boundary conditions
- Test error cases

### **3. Debugging**
- Check response status codes
- Check response body
- Check server logs

## 🚀 **Quick Start**

1. **Import** collection và environment
2. **Select** Order API Environment
3. **Run** Social Login để tạo token
4. **Run** Create Order để đặt hàng
5. **Run** Get All Orders để xem kết quả

Postman collection đã sẵn sàng! 🎉✨
