# Hướng Dẫn Product Management APIs

## 🎯 **Mục tiêu**
Hướng dẫn sử dụng các API quản lý sản phẩm bao gồm thêm, xóa và cập nhật sản phẩm.

## 📋 **APIs có sẵn**

### **1. Tạo sản phẩm mới**
```http
POST /api/admin/products
Content-Type: application/json
```

### **2. Cập nhật thông tin sản phẩm**
```http
PUT /api/admin/products/{productId}
Content-Type: application/json
```

### **3. Xóa sản phẩm**
```http
DELETE /api/admin/products/{productId}
```

### **4. Lấy thông tin sản phẩm**
```http
GET /api/admin/products/{productId}
```

### **5. Lấy danh sách tất cả sản phẩm**
```http
GET /api/admin/products
```

### **6. Lấy danh sách sản phẩm theo shop ID**
```http
GET /api/admin/products/shop/{shopId}
```

### **7. Lấy danh sách sản phẩm theo trạng thái**
```http
GET /api/admin/products/status/{status}
```

### **8. Lấy danh sách sản phẩm theo category ID**
```http
GET /api/admin/products/category/{categoryId}
```

## 🔧 **CURL Commands**

### **1. Tạo sản phẩm mới**
```bash
curl -X POST "http://localhost:8080/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": 1,
    "name": "Pizza Margherita New",
    "description": "Pizza cổ điển với cà chua, mozzarella và húng quế tươi",
    "price": 150000.00,
    "originalPrice": 180000.00,
    "imageUrl": "https://example.com/pizza_margherita_new.jpg",
    "detailImageUrl": "https://example.com/pizza_margherita_detail.jpg",
    "quantityAvailable": 50,
    "status": "active"
  }'
```

### **2. Cập nhật thông tin sản phẩm**
```bash
curl -X PUT "http://localhost:8080/api/admin/products/1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pizza Margherita Updated",
    "description": "Pizza cổ điển đã được cập nhật với nguyên liệu cao cấp",
    "price": 160000.00,
    "originalPrice": 200000.00,
    "quantityAvailable": 60,
    "status": "active"
  }'
```

### **3. Lấy thông tin sản phẩm**
```bash
curl -X GET "http://localhost:8080/api/admin/products/1"
```

### **4. Lấy danh sách tất cả sản phẩm**
```bash
curl -X GET "http://localhost:8080/api/admin/products"
```

### **5. Lấy danh sách sản phẩm theo shop**
```bash
curl -X GET "http://localhost:8080/api/admin/products/shop/1"
```

### **6. Lấy danh sách sản phẩm theo trạng thái**
```bash
curl -X GET "http://localhost:8080/api/admin/products/status/active"
```

### **7. Lấy danh sách sản phẩm theo category**
```bash
curl -X GET "http://localhost:8080/api/admin/products/category/1"
```

### **8. Xóa sản phẩm**
```bash
curl -X DELETE "http://localhost:8080/api/admin/products/1"
```

## 📊 **Request Body Format**

### **CreateProductRequest:**
```json
{
  "shopId": 1,                              // Bắt buộc, ID của shop
  "categoryId": 1,                          // Bắt buộc, ID của category
  "name": "Pizza Margherita",               // Bắt buộc, tên sản phẩm
  "description": "Pizza cổ điển ngon",       // Tùy chọn, mô tả
  "price": 150000.00,                       // Bắt buộc, giá hiện tại
  "originalPrice": 180000.00,               // Tùy chọn, giá gốc
  "imageUrl": "https://example.com/pizza.jpg", // Tùy chọn, URL ảnh
  "detailImageUrl": "https://example.com/pizza_detail.jpg", // Tùy chọn, URL ảnh chi tiết
  "quantityAvailable": 50,                  // Tùy chọn, số lượng có sẵn
  "status": "active"                        // Tùy chọn, trạng thái (active/inactive/out_of_stock)
}
```

### **UpdateProductRequest:**
```json
{
  "shopId": 2,                              // Tùy chọn, ID của shop
  "categoryId": 2,                          // Tùy chọn, ID của category
  "name": "Pizza Margherita Updated",       // Tùy chọn, tên sản phẩm
  "description": "Pizza đã cập nhật",       // Tùy chọn, mô tả
  "price": 160000.00,                       // Tùy chọn, giá hiện tại
  "originalPrice": 200000.00,               // Tùy chọn, giá gốc
  "imageUrl": "https://example.com/pizza_updated.jpg", // Tùy chọn, URL ảnh
  "detailImageUrl": "https://example.com/pizza_detail_updated.jpg", // Tùy chọn, URL ảnh chi tiết
  "quantityAvailable": 60,                  // Tùy chọn, số lượng có sẵn
  "status": "active"                        // Tùy chọn, trạng thái
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
    "shopId": 1,
    "categoryId": 1,
    "name": "Pizza Margherita",
    "description": "Pizza cổ điển với cà chua, mozzarella",
    "price": 150000.00,
    "originalPrice": 180000.00,
    "imageUrl": "https://example.com/pizza.jpg",
    "detailImageUrl": "https://example.com/pizza_detail.jpg",
    "quantityAvailable": 50,
    "quantityPending": 0,
    "status": "active"
  }
}
```

### **Error Response:**
```json
{
  "success": false,
  "code": "400",
  "message": "Original price cannot be less than current price"
}
```

## 🧪 **Test Cases**

### **1. Test thành công**
```bash
# Tạo sản phẩm
curl -X POST "http://localhost:8080/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": 1,
    "name": "Test Product",
    "price": 100000.00,
    "status": "active"
  }'

# Cập nhật sản phẩm
curl -X PUT "http://localhost:8080/api/admin/products/1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product Updated"
  }'

# Xóa sản phẩm
curl -X DELETE "http://localhost:8080/api/admin/products/1"
```

### **2. Test lỗi - Thiếu thông tin bắt buộc**
```bash
curl -X POST "http://localhost:8080/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "price": 100000.00
  }'
```

### **3. Test lỗi - Giá gốc nhỏ hơn giá hiện tại**
```bash
curl -X POST "http://localhost:8080/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": 1,
    "name": "Invalid Price Product",
    "price": 200000.00,
    "originalPrice": 150000.00,
    "status": "active"
  }'
```

### **4. Test lỗi - Shop không tồn tại**
```bash
curl -X POST "http://localhost:8080/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 999,
    "categoryId": 1,
    "name": "Invalid Shop Product",
    "price": 100000.00,
    "status": "active"
  }'
```

### **5. Test lỗi - Sản phẩm không tồn tại**
```bash
curl -X GET "http://localhost:8080/api/admin/products/999"
curl -X PUT "http://localhost:8080/api/admin/products/999" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test"}'
curl -X DELETE "http://localhost:8080/api/admin/products/999"
```

## 🔍 **Validation Rules**

### **1. CreateProductRequest:**
- `shopId`: Bắt buộc, phải là số dương và shop phải tồn tại
- `categoryId`: Bắt buộc, phải là số dương
- `name`: Bắt buộc, tối đa 255 ký tự
- `description`: Tùy chọn, tối đa 1000 ký tự
- `price`: Bắt buộc, phải là số dương
- `originalPrice`: Tùy chọn, phải là số dương và >= price
- `imageUrl`: Tùy chọn, tối đa 255 ký tự
- `detailImageUrl`: Tùy chọn, tối đa 1000 ký tự
- `quantityAvailable`: Tùy chọn, phải là số dương
- `status`: Tùy chọn, active/inactive/out_of_stock

### **2. UpdateProductRequest:**
- Tất cả fields đều tùy chọn
- Validation rules giống CreateProductRequest
- Nếu cập nhật shopId, shop mới phải tồn tại

## 📝 **Dữ liệu mẫu**

### **Sản phẩm Pizza:**
```json
{
  "shopId": 1,
  "categoryId": 1,
  "name": "Pizza Margherita",
  "description": "Pizza cổ điển với cà chua, mozzarella và húng quế tươi",
  "price": 150000.00,
  "originalPrice": 180000.00,
  "imageUrl": "https://example.com/pizza_margherita.jpg",
  "detailImageUrl": "https://example.com/pizza_margherita_detail.jpg",
  "quantityAvailable": 50,
  "status": "active"
}
```

### **Sản phẩm Burger:**
```json
{
  "shopId": 2,
  "categoryId": 2,
  "name": "Burger Deluxe",
  "description": "Burger cao cấp với thịt bò wagyu và phô mai cheddar",
  "price": 120000.00,
  "originalPrice": 150000.00,
  "imageUrl": "https://example.com/burger_deluxe.jpg",
  "detailImageUrl": "https://example.com/burger_deluxe_detail.jpg",
  "quantityAvailable": 30,
  "status": "active"
}
```

### **Sản phẩm Cafe:**
```json
{
  "shopId": 1,
  "categoryId": 3,
  "name": "Cafe Latte Special",
  "description": "Cafe latte đặc biệt với hương vị độc đáo",
  "price": 45000.00,
  "originalPrice": 50000.00,
  "imageUrl": "https://example.com/cafe_latte_special.jpg",
  "detailImageUrl": "https://example.com/cafe_latte_detail.jpg",
  "quantityAvailable": 0,
  "status": "out_of_stock"
}
```

## 🚀 **Quick Start**

### **1. Chạy test script:**
```bash
chmod +x test_product_management.sh
./test_product_management.sh
```

### **2. Import Postman collection:**
- `Product_Management_API_Collection.postman_collection.json`

### **3. Test thủ công:**
```bash
# 1. Tạo sản phẩm
curl -X POST "http://localhost:8080/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{"shopId": 1, "categoryId": 1, "name": "Test Product", "price": 100000.00, "status": "active"}'

# 2. Lấy danh sách sản phẩm
curl -X GET "http://localhost:8080/api/admin/products"

# 3. Cập nhật sản phẩm
curl -X PUT "http://localhost:8080/api/admin/products/1" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Product Updated"}'

# 4. Xóa sản phẩm
curl -X DELETE "http://localhost:8080/api/admin/products/1"
```

## ⚠️ **Lưu ý quan trọng**

### **1. Giá cả:**
- `price`: Giá hiện tại của sản phẩm
- `originalPrice`: Giá gốc (phải >= price)
- Nếu không có `originalPrice`, sản phẩm không có giảm giá

### **2. Trạng thái:**
- `active`: Sản phẩm đang bán
- `inactive`: Sản phẩm tạm ngưng
- `out_of_stock`: Sản phẩm hết hàng

### **3. Số lượng:**
- `quantityAvailable`: Số lượng có sẵn
- `quantityPending`: Số lượng đang chờ (tự động tính)

### **4. Xóa sản phẩm:**
- Không thể xóa sản phẩm có `quantityPending > 0`
- Xóa vĩnh viễn khỏi database

### **5. Shop và Category:**
- `shopId` phải tồn tại trong hệ thống
- `categoryId` phải tồn tại trong hệ thống

## 🔧 **Postman Testing**

### **1. Import collection:**
1. Mở Postman
2. Click **Import** → **Upload Files**
3. Chọn `Product_Management_API_Collection.postman_collection.json`
4. Click **Import**

### **2. Test workflow:**
1. **Create Product** - Tạo sản phẩm mới
2. **Get Product by ID** - Lấy thông tin sản phẩm
3. **Update Product** - Cập nhật thông tin
4. **Get All Products** - Xem danh sách
5. **Get Products by Shop** - Lọc theo shop
6. **Get Products by Status** - Lọc theo trạng thái
7. **Get Products by Category** - Lọc theo category
8. **Delete Product** - Xóa sản phẩm

### **3. Error Testing:**
1. Test các trường hợp lỗi trong folder **Error Testing**
2. Kiểm tra response code và message

## ✅ **Kết luận**

**Product Management APIs đã sẵn sàng!**

- ✅ **Create Product** - Tạo sản phẩm mới
- ✅ **Update Product** - Cập nhật thông tin sản phẩm
- ✅ **Delete Product** - Xóa sản phẩm
- ✅ **Get Product** - Lấy thông tin sản phẩm
- ✅ **List Products** - Lấy danh sách sản phẩm
- ✅ **Filter by Shop** - Lọc theo shop
- ✅ **Filter by Status** - Lọc theo trạng thái
- ✅ **Filter by Category** - Lọc theo category
- ✅ **Validation** - Kiểm tra dữ liệu đầu vào
- ✅ **Error Handling** - Xử lý lỗi đầy đủ

APIs quản lý sản phẩm đã sẵn sàng! 🎉✨
