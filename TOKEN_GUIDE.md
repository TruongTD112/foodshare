# Hướng Dẫn Tạo và Sử Dụng Token

## 🎯 **Mục tiêu**
Hướng dẫn tạo và sử dụng JWT token để test API đặt đơn hàng.

## 🔧 **Cách tạo token**

### **1. Sử dụng Social Login API**
```bash
curl -X POST "http://localhost:8080/auth/social" \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "providerId": "google_123",
    "email": "user1@example.com",
    "name": "User 1",
    "profilePictureUrl": "https://example.com/avatar1.jpg"
  }'
```

### **2. Response mẫu:**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "email": "user1@example.com",
    "name": "User 1"
  }
}
```

## 🚀 **Script tự động**

### **1. Chạy script tạo token:**
```bash
chmod +x generate_token.sh
./generate_token.sh
```

### **2. Chạy script test nhanh:**
```bash
chmod +x test_with_token.sh
./test_with_token.sh
```

## 📋 **Sử dụng token**

### **1. Copy token từ response**
```bash
# Từ response JSON, copy giá trị của "token"
TOKEN="eyJhbGciOiJIUzI1NiJ9..."
```

### **2. Sử dụng trong header**
```bash
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "pickupTime": "2024-01-15T14:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  }'
```

## 🧪 **Test Cases**

### **1. Tạo token cho User 1**
```bash
curl -X POST "http://localhost:8080/auth/social" \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "providerId": "google_123",
    "email": "user1@example.com",
    "name": "User 1",
    "profilePictureUrl": "https://example.com/avatar1.jpg"
  }'
```

### **2. Tạo token cho User 2**
```bash
curl -X POST "http://localhost:8080/auth/social" \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "providerId": "google_456",
    "email": "user2@example.com",
    "name": "User 2",
    "profilePictureUrl": "https://example.com/avatar2.jpg"
  }'
```

### **3. Tạo token cho User 3**
```bash
curl -X POST "http://localhost:8080/auth/social" \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "providerId": "google_789",
    "email": "user3@example.com",
    "name": "User 3",
    "profilePictureUrl": "https://example.com/avatar3.jpg"
  }'
```

## 🔍 **Debug Token**

### **1. Kiểm tra token có hợp lệ:**
```bash
# Test với API cần authentication
curl -X GET "http://localhost:8080/orders" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### **2. Kiểm tra logs:**
- Xem server logs để debug
- Check JWT parsing errors

## ⚠️ **Lưu ý quan trọng**

### **1. Token Expiry:**
- Token có thời hạn 24 giờ
- Cần tạo token mới khi hết hạn

### **2. User ID:**
- Token chứa `uid` (user ID)
- API sẽ tự động lấy `userId` từ token

### **3. Security:**
- Token chỉ dành cho testing
- Không sử dụng trong production

## 🛠️ **Troubleshooting**

### **1. Lỗi "Invalid token":**
- Kiểm tra token có đúng format
- Kiểm tra token có hết hạn
- Kiểm tra header Authorization

### **2. Lỗi "Unauthorized":**
- Kiểm tra token có được gửi đúng
- Kiểm tra endpoint có cần authentication

### **3. Lỗi SQL "Order":**
- Đã sửa bằng cách thêm backticks: `"Order"`
- Restart server nếu cần

## 📝 **Ví dụ hoàn chỉnh**

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

# 3. Sử dụng token
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "pickupTime": "2024-01-15T14:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  }'
```

Token đã sẵn sàng! 🎉✨
