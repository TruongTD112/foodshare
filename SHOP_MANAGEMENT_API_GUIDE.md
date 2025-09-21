# Hướng Dẫn Shop Management APIs

## 🎯 **Mục tiêu**
Hướng dẫn sử dụng các API quản lý cửa hàng bao gồm thêm, xóa và cập nhật cửa hàng.

## 📋 **APIs có sẵn**

### **1. Tạo cửa hàng mới**
```http
POST /api/admin/shops
Content-Type: application/json
```

### **2. Cập nhật thông tin cửa hàng**
```http
PUT /api/admin/shops/{shopId}
Content-Type: application/json
```

### **3. Xóa cửa hàng**
```http
DELETE /api/admin/shops/{shopId}
```

### **4. Lấy thông tin cửa hàng**
```http
GET /api/admin/shops/{shopId}
```

### **5. Lấy danh sách tất cả cửa hàng**
```http
GET /api/admin/shops
```

### **6. Lấy danh sách cửa hàng theo trạng thái**
```http
GET /api/admin/shops/status/{status}
```

## 🔧 **CURL Commands**

### **1. Tạo cửa hàng mới**
```bash
curl -X POST "http://localhost:8080/api/admin/shops" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pizza Palace New",
    "address": "456 Đường XYZ, Quận 2, TP.HCM",
    "phone": "0987654321",
    "imageUrl": "https://example.com/pizza_palace_new.jpg",
    "latitude": 10.7769,
    "longitude": 106.7009,
    "description": "Cửa hàng pizza mới với không gian rộng rãi",
    "status": "active"
  }'
```

### **2. Cập nhật thông tin cửa hàng**
```bash
curl -X PUT "http://localhost:8080/api/admin/shops/1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pizza Palace Updated",
    "address": "789 Đường ABC, Quận 3, TP.HCM",
    "phone": "0123456789",
    "imageUrl": "https://example.com/pizza_palace_updated.jpg",
    "latitude": 10.7829,
    "longitude": 106.6959,
    "description": "Cửa hàng pizza đã được cập nhật với menu mới",
    "status": "active"
  }'
```

### **3. Lấy thông tin cửa hàng**
```bash
curl -X GET "http://localhost:8080/api/admin/shops/1"
```

### **4. Lấy danh sách tất cả cửa hàng**
```bash
curl -X GET "http://localhost:8080/api/admin/shops"
```

### **5. Lấy danh sách cửa hàng theo trạng thái**
```bash
curl -X GET "http://localhost:8080/api/admin/shops/status/active"
```

### **6. Xóa cửa hàng**
```bash
curl -X DELETE "http://localhost:8080/api/admin/shops/1"
```

## 📊 **Request Body Format**

### **CreateShopRequest:**
```json
{
  "name": "Pizza Palace",                    // Bắt buộc, tên cửa hàng
  "address": "123 Đường ABC, Quận 1, TP.HCM", // Tùy chọn, địa chỉ
  "phone": "0123456789",                     // Tùy chọn, số điện thoại
  "imageUrl": "https://example.com/shop.jpg", // Tùy chọn, URL ảnh
  "latitude": 10.7769,                       // Bắt buộc, vĩ độ
  "longitude": 106.7009,                     // Bắt buộc, kinh độ
  "description": "Cửa hàng pizza ngon",       // Tùy chọn, mô tả
  "status": "active"                         // Tùy chọn, trạng thái (active/inactive/suspended)
}
```

### **UpdateShopRequest:**
```json
{
  "name": "Pizza Palace Updated",            // Tùy chọn, tên cửa hàng
  "address": "789 Đường ABC, Quận 3, TP.HCM", // Tùy chọn, địa chỉ
  "phone": "0987654321",                     // Tùy chọn, số điện thoại
  "imageUrl": "https://example.com/shop_updated.jpg", // Tùy chọn, URL ảnh
  "latitude": 10.7829,                       // Tùy chọn, vĩ độ
  "longitude": 106.6959,                     // Tùy chọn, kinh độ
  "description": "Cửa hàng pizza đã cập nhật", // Tùy chọn, mô tả
  "status": "active"                         // Tùy chọn, trạng thái
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
    "name": "Pizza Palace",
    "address": "123 Đường ABC, Quận 1, TP.HCM",
    "phone": "0123456789",
    "imageUrl": "https://example.com/shop.jpg",
    "latitude": 10.7769,
    "longitude": 106.7009,
    "description": "Cửa hàng pizza ngon",
    "rating": 4.5,
    "status": "active"
  }
}
```

### **Error Response:**
```json
{
  "success": false,
  "code": "400",
  "message": "Invalid coordinates"
}
```

## 🧪 **Test Cases**

### **1. Test thành công**
```bash
# Tạo cửa hàng
curl -X POST "http://localhost:8080/api/admin/shops" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Shop",
    "address": "123 Test Street",
    "phone": "0123456789",
    "latitude": 10.7769,
    "longitude": 106.7009,
    "status": "active"
  }'

# Cập nhật cửa hàng
curl -X PUT "http://localhost:8080/api/admin/shops/1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Shop Updated"
  }'

# Xóa cửa hàng
curl -X DELETE "http://localhost:8080/api/admin/shops/1"
```

### **2. Test lỗi - Thiếu thông tin bắt buộc**
```bash
curl -X POST "http://localhost:8080/api/admin/shops" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "latitude": 10.7769
  }'
```

### **3. Test lỗi - Tọa độ không hợp lệ**
```bash
curl -X POST "http://localhost:8080/api/admin/shops" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Invalid Shop",
    "latitude": 200.0,
    "longitude": 300.0,
    "status": "active"
  }'
```

### **4. Test lỗi - Cửa hàng không tồn tại**
```bash
curl -X GET "http://localhost:8080/api/admin/shops/999"
curl -X PUT "http://localhost:8080/api/admin/shops/999" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test"}'
curl -X DELETE "http://localhost:8080/api/admin/shops/999"
```

## 🔍 **Validation Rules**

### **1. CreateShopRequest:**
- `name`: Bắt buộc, tối đa 255 ký tự
- `address`: Tùy chọn, tối đa 255 ký tự
- `phone`: Tùy chọn, format số điện thoại Việt Nam
- `imageUrl`: Tùy chọn, tối đa 255 ký tự
- `latitude`: Bắt buộc, từ -90 đến 90
- `longitude`: Bắt buộc, từ -180 đến 180
- `description`: Tùy chọn, tối đa 1000 ký tự
- `status`: Tùy chọn, active/inactive/suspended

### **2. UpdateShopRequest:**
- Tất cả fields đều tùy chọn
- Validation rules giống CreateShopRequest

## 📝 **Dữ liệu mẫu**

### **Cửa hàng Pizza:**
```json
{
  "name": "Pizza Palace",
  "address": "123 Đường ABC, Quận 1, TP.HCM",
  "phone": "0123456789",
  "imageUrl": "https://example.com/pizza_palace.jpg",
  "latitude": 10.7769,
  "longitude": 106.7009,
  "description": "Cửa hàng pizza với menu đa dạng",
  "status": "active"
}
```

### **Cửa hàng Burger:**
```json
{
  "name": "Burger Joint",
  "address": "456 Đường XYZ, Quận 2, TP.HCM",
  "phone": "0987654321",
  "imageUrl": "https://example.com/burger_joint.jpg",
  "latitude": 10.7829,
  "longitude": 106.6959,
  "description": "Cửa hàng burger với không gian rộng rãi",
  "status": "active"
}
```

### **Cửa hàng Cafe:**
```json
{
  "name": "Cafe Central",
  "address": "789 Đường DEF, Quận 3, TP.HCM",
  "phone": "0369852147",
  "imageUrl": "https://example.com/cafe_central.jpg",
  "latitude": 10.7659,
  "longitude": 106.7059,
  "description": "Cửa hàng cafe với không gian yên tĩnh",
  "status": "inactive"
}
```

## 🚀 **Quick Start**

### **1. Chạy test script:**
```bash
chmod +x test_shop_management.sh
./test_shop_management.sh
```

### **2. Import Postman collection:**
- `Shop_Management_API_Collection.postman_collection.json`

### **3. Test thủ công:**
```bash
# 1. Tạo cửa hàng
curl -X POST "http://localhost:8080/api/admin/shops" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Shop", "latitude": 10.7769, "longitude": 106.7009, "status": "active"}'

# 2. Lấy danh sách cửa hàng
curl -X GET "http://localhost:8080/api/admin/shops"

# 3. Cập nhật cửa hàng
curl -X PUT "http://localhost:8080/api/admin/shops/1" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Shop Updated"}'

# 4. Xóa cửa hàng
curl -X DELETE "http://localhost:8080/api/admin/shops/1"
```

## ⚠️ **Lưu ý quan trọng**

### **1. Tọa độ:**
- Latitude: -90 đến 90
- Longitude: -180 đến 180
- Sử dụng BigDecimal để đảm bảo độ chính xác

### **2. Trạng thái:**
- `active`: Cửa hàng đang hoạt động
- `inactive`: Cửa hàng tạm ngưng
- `suspended`: Cửa hàng bị đình chỉ

### **3. Số điện thoại:**
- Format số điện thoại Việt Nam
- Hỗ trợ các format: 0123456789, +84123456789, 0123 456 789

### **4. Xóa cửa hàng:**
- Xóa vĩnh viễn khỏi database
- Cần kiểm tra có sản phẩm liên quan không (tùy chọn)

## 🔧 **Postman Testing**

### **1. Import collection:**
1. Mở Postman
2. Click **Import** → **Upload Files**
3. Chọn `Shop_Management_API_Collection.postman_collection.json`
4. Click **Import**

### **2. Test workflow:**
1. **Create Shop** - Tạo cửa hàng mới
2. **Get Shop by ID** - Lấy thông tin cửa hàng
3. **Update Shop** - Cập nhật thông tin
4. **Get All Shops** - Xem danh sách
5. **Delete Shop** - Xóa cửa hàng

### **3. Error Testing:**
1. Test các trường hợp lỗi trong folder **Error Testing**
2. Kiểm tra response code và message

## ✅ **Kết luận**

**Shop Management APIs đã sẵn sàng!**

- ✅ **Create Shop** - Tạo cửa hàng mới
- ✅ **Update Shop** - Cập nhật thông tin cửa hàng
- ✅ **Delete Shop** - Xóa cửa hàng
- ✅ **Get Shop** - Lấy thông tin cửa hàng
- ✅ **List Shops** - Lấy danh sách cửa hàng
- ✅ **Filter by Status** - Lọc theo trạng thái
- ✅ **Validation** - Kiểm tra dữ liệu đầu vào
- ✅ **Error Handling** - Xử lý lỗi đầy đủ

APIs quản lý cửa hàng đã sẵn sàng! 🎉✨
