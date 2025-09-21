# 📋 HƯỚNG DẪN API LẤY SẢN PHẨM THEO SHOP ID

## 🎯 Tổng quan

API này cho phép lấy danh sách sản phẩm theo Shop ID cụ thể. Hữu ích khi cần xem tất cả sản phẩm của một cửa hàng.

## 📡 API Endpoints

### 1. Lấy sản phẩm theo Shop ID

**Endpoint:** `GET /api/admin/products/shop/{shopId}`

**Mô tả:** Lấy danh sách tất cả sản phẩm của một shop cụ thể

**Parameters:**
- `shopId` (path, required): ID của shop cần lấy sản phẩm

**Response:**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": [
    {
      "id": 1,
      "shopId": 1,
      "categoryId": 1,
      "name": "Pizza Margherita",
      "description": "Pizza cổ điển với phô mai mozzarella",
      "price": 150000.00,
      "originalPrice": 180000.00,
      "imageUrl": "https://example.com/pizza.jpg",
      "detailImageUrl": "https://example.com/pizza_detail.jpg",
      "quantityAvailable": 50,
      "quantityPending": 0,
      "status": "1"
    }
  ]
}
```

### 2. Lấy sản phẩm theo Shop ID và trạng thái

**Endpoint:** `GET /api/admin/products/shop/{shopId}/status/{status}`

**Mô tả:** Lấy danh sách sản phẩm của một shop cụ thể với trạng thái cụ thể

**Parameters:**
- `shopId` (path, required): ID của shop
- `status` (path, required): Trạng thái sản phẩm (1: available, 2: sold_out, 3: no_longer_sell)

### 3. Lấy tất cả sản phẩm

**Endpoint:** `GET /api/admin/products`

**Mô tả:** Lấy danh sách tất cả sản phẩm trong hệ thống

## 🧪 Test với cURL

### 1. Lấy sản phẩm theo Shop ID 1

```bash
curl -X GET "http://localhost:8080/api/admin/products/shop/1" \
  -H "Content-Type: application/json"
```

### 2. Lấy sản phẩm theo Shop ID 2

```bash
curl -X GET "http://localhost:8080/api/admin/products/shop/2" \
  -H "Content-Type: application/json"
```

### 3. Lấy sản phẩm theo Shop ID và trạng thái

```bash
# Lấy sản phẩm Shop 1 với trạng thái 1 (available)
curl -X GET "http://localhost:8080/api/admin/products/shop/1/status/1" \
  -H "Content-Type: application/json"

# Lấy sản phẩm Shop 1 với trạng thái 2 (sold_out)
curl -X GET "http://localhost:8080/api/admin/products/shop/1/status/2" \
  -H "Content-Type: application/json"

# Lấy sản phẩm Shop 1 với trạng thái 3 (no_longer_sell)
curl -X GET "http://localhost:8080/api/admin/products/shop/1/status/3" \
  -H "Content-Type: application/json"
```

### 4. Lấy tất cả sản phẩm

```bash
curl -X GET "http://localhost:8080/api/admin/products" \
  -H "Content-Type: application/json"
```

## 📊 Test với Postman

1. **Import Collection:**
   - Import file `Products_By_Shop_API_Collection.postman_collection.json`

2. **Set Environment:**
   - `base_url`: `http://localhost:8080`

3. **Chạy Test:**
   - Chạy từng request theo thứ tự
   - Kiểm tra response và status code

## 🔧 Test với Script

```bash
# Chạy script test tự động
chmod +x test_products_by_shop.sh
./test_products_by_shop.sh
```

## 📝 Response Codes

| Code | Mô tả |
|------|-------|
| 200 | Thành công |
| 400 | Dữ liệu không hợp lệ |
| 404 | Shop không tồn tại |
| 500 | Lỗi server |

## 🎯 Use Cases

### 1. Quản lý sản phẩm theo shop
- Xem tất cả sản phẩm của một shop
- Kiểm tra số lượng sản phẩm theo shop
- Quản lý inventory theo shop

### 2. Phân tích dữ liệu
- So sánh sản phẩm giữa các shop
- Thống kê sản phẩm theo shop
- Báo cáo hiệu suất shop

### 3. Tích hợp frontend
- Hiển thị sản phẩm theo shop trên UI
- Filter sản phẩm theo shop
- Navigation theo shop

## ⚠️ Lưu ý

1. **Shop ID phải tồn tại:** API sẽ trả về lỗi 404 nếu shop không tồn tại
2. **Trạng thái sản phẩm:** Chỉ lấy sản phẩm có trạng thái hợp lệ
3. **Performance:** API trả về tất cả sản phẩm của shop, cần cân nhắc pagination cho shop có nhiều sản phẩm
4. **Security:** API này dành cho admin, cần xác thực

## 🚀 Mở rộng

### Thêm pagination
```java
@GetMapping("/shop/{shopId}")
public Result<Page<ProductManagementResponse>> getProductsByShopId(
    @PathVariable Integer shopId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
) {
    // Implementation with pagination
}
```

### Thêm filter theo trạng thái
```java
@GetMapping("/shop/{shopId}")
public Result<List<ProductManagementResponse>> getProductsByShopId(
    @PathVariable Integer shopId,
    @RequestParam(required = false) String status
) {
    // Implementation with status filter
}
```

### Thêm sort
```java
@GetMapping("/shop/{shopId}")
public Result<List<ProductManagementResponse>> getProductsByShopId(
    @PathVariable Integer shopId,
    @RequestParam(defaultValue = "name") String sortBy,
    @RequestParam(defaultValue = "asc") String sortDir
) {
    // Implementation with sorting
}
```

## 📚 Related APIs

- `GET /api/admin/products` - Lấy tất cả sản phẩm
- `GET /api/admin/products/{id}` - Lấy sản phẩm theo ID
- `POST /api/admin/products` - Tạo sản phẩm mới
- `PUT /api/admin/products/{id}` - Cập nhật sản phẩm
- `DELETE /api/admin/products/{id}` - Xóa sản phẩm
- `GET /api/admin/shops` - Lấy danh sách shop
- `GET /api/admin/shops/{id}` - Lấy thông tin shop theo ID
