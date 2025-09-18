# Hướng Dẫn API Đặt Đơn Hàng Mới

## 🎯 **Thay đổi chính**

API đặt đơn hàng đã được thiết kế lại với các yêu cầu mới:

### **1. Input mới:**
- ✅ **`pickupTime`** - Ngày và giờ đặt hàng (thay vì `pickupInMinutes`)
- ✅ **`unitPrice`** - Giá trên từng sản phẩm tại thời điểm đặt
- ✅ **`totalPrice`** - Tổng giá của đơn hàng

### **2. Validation mới:**
- `pickupTime` phải trong tương lai
- `unitPrice` và `totalPrice` phải > 0
- `totalPrice` = `unitPrice` × `quantity`

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
    "pickupTime": "2024-01-15T14:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  }'
```

### **2. Đặt đơn hàng với thời gian khác**
```bash
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 2,
    "quantity": 1,
    "pickupTime": "2024-01-15T16:00:00",
    "unitPrice": 180000.00,
    "totalPrice": 180000.00
  }'
```

### **3. Xem danh sách đơn hàng**
```bash
curl -X GET "http://localhost:8080/orders" \
  -H "Authorization: Bearer user_1"
```

### **4. Hủy đơn hàng**
```bash
curl -X DELETE "http://localhost:8080/orders/1" \
  -H "Authorization: Bearer user_1"
```

## 📊 **Request Body Format**

### **OrderCreateRequest mới:**
```json
{
  "shopId": 1,                    // ID cửa hàng (bắt buộc)
  "productId": 1,                 // ID sản phẩm (bắt buộc)
  "quantity": 2,                  // Số lượng (bắt buộc)
  "pickupTime": "2024-01-15T14:30:00",  // Ngày và giờ đặt hàng (bắt buộc)
  "unitPrice": 150000.00,         // Giá trên từng sản phẩm (bắt buộc)
  "totalPrice": 300000.00         // Tổng giá đơn hàng (bắt buộc)
}
```

### **Fields:**
- **shopId**: ID cửa hàng (bắt buộc)
- **productId**: ID sản phẩm (bắt buộc)
- **quantity**: Số lượng đặt (bắt buộc, > 0)
- **pickupTime**: Ngày và giờ đặt hàng (bắt buộc, phải trong tương lai)
- **unitPrice**: Giá trên từng sản phẩm (bắt buộc, > 0)
- **totalPrice**: Tổng giá đơn hàng (bắt buộc, > 0)

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
  "message": "Pickup time is required"
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
    "pickupTime": "2024-01-15T14:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  }'
```

### **2. Test lỗi - Thiếu pickup time**
```bash
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 1,
    "unitPrice": 150000.00,
    "totalPrice": 150000.00
  }'
```

### **3. Test lỗi - Pickup time trong quá khứ**
```bash
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 1,
    "pickupTime": "2023-01-01T10:00:00",
    "unitPrice": 150000.00,
    "totalPrice": 150000.00
  }'
```

### **4. Test lỗi - Tính toán tổng giá sai**
```bash
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "pickupTime": "2024-01-15T14:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 200000.00
  }'
```

## 🗄️ **Database Changes**

### **1. Cột mới trong bảng Order:**
```sql
ALTER TABLE `Order` 
ADD COLUMN `unit_price` DECIMAL(10,2) AFTER `expires_at`,
ADD COLUMN `total_price` DECIMAL(10,2) AFTER `unit_price`;
```

### **2. Migration script:**
```bash
# Chạy migration
mysql -u root -p foodshare < migrate_order_table.sql
```

## 🚀 **Quick Start**

### **1. Chạy migration:**
```bash
mysql -u root -p foodshare < migrate_order_table.sql
```

### **2. Chạy script test mới:**
```bash
chmod +x create_order_new_api.sh
./create_order_new_api.sh
```

### **3. Test thủ công:**
```bash
# Đặt đơn hàng
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
```

## ⚠️ **Lưu ý quan trọng**

### **1. Validation mới:**
- **pickupTime** phải trong tương lai
- **unitPrice** và **totalPrice** phải > 0
- **totalPrice** = **unitPrice** × **quantity**

### **2. Business Logic:**
- Giá được lưu tại thời điểm đặt hàng
- Không phụ thuộc vào giá hiện tại của sản phẩm
- Tự động set `userId` từ token
- Tạo `expiresAt` (pickupTime + 1 giờ)

### **3. Database:**
- Cần chạy migration script trước khi sử dụng
- Cột `unit_price` và `total_price` mới
- Dữ liệu cũ sẽ được cập nhật tự động

## 🔍 **So sánh API cũ vs mới**

| Field | API Cũ | API Mới |
|-------|--------|---------|
| Thời gian | `pickupInMinutes` | `pickupTime` |
| Giá | Không có | `unitPrice`, `totalPrice` |
| Validation | Cơ bản | Nâng cao |
| Response | Thiếu giá | Đầy đủ thông tin |

API mới đã sẵn sàng! 🚀✨
