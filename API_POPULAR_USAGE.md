# API Lấy Sản Phẩm Bán Chạy Nhất

## Mô tả
API để lấy danh sách sản phẩm bán chạy nhất dựa trên tổng số lượng đã mua. Sử dụng bảng `ProductSalesStats` riêng để thống kê hiệu quả.

## Endpoint
```
GET /products/popular
```

## Tham số
| Tham số | Loại | Bắt buộc | Mô tả | Ví dụ |
|---------|------|----------|-------|-------|
| page | Integer | Không | Số trang (0-based, mặc định: 0) | 0 |
| size | Integer | Không | Kích thước trang (mặc định: 20, tối đa: 100) | 20 |

## Ví dụ sử dụng

### 1. Lấy trang đầu tiên (mặc định)
```bash
curl -X GET "http://localhost:8080/products/popular"
```

### 2. Lấy 10 sản phẩm bán chạy nhất
```bash
curl -X GET "http://localhost:8080/products/popular?size=10"
```

### 3. Lấy trang thứ 2
```bash
curl -X GET "http://localhost:8080/products/popular?page=1&size=20"
```

### 4. Sử dụng với JavaScript/Fetch
```javascript
const getPopularProducts = async (page = 0, size = 20) => {
  const response = await fetch(`/products/popular?page=${page}&size=${size}`);
  return await response.json();
};

// Sử dụng
const result = await getPopularProducts(0, 10);
console.log(result);
```

## Response Format
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "content": [
      {
        "productId": 1,
        "name": "Pizza Margherita",
        "price": 120000,
        "originalPrice": 150000,
        "discountPercentage": 20.00,
        "imageUrl": "https://example.com/pizza.jpg",
        "shopId": 1,
        "shopName": "Pizza Corner",
        "shopLatitude": 10.763000,
        "shopLongitude": 106.661000,
        "distanceKm": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

## Cơ chế hoạt động

### 1. **Bảng ProductSalesStats**
```sql
CREATE TABLE ProductSalesStats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL UNIQUE,
    total_quantity_sold INT NOT NULL DEFAULT 0,
    total_orders INT NOT NULL DEFAULT 0,
    last_sold_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 2. **Cập nhật thống kê**
- Khi order chuyển sang status `completed`
- Tự động cộng dồn `total_quantity_sold` và `total_orders`
- Cập nhật `last_sold_at`

### 3. **Query hiệu quả**
```sql
SELECT p.* FROM Product p 
INNER JOIN ProductSalesStats s ON p.id = s.product_id 
WHERE p.status = '1' 
ORDER BY s.total_quantity_sold DESC
```

## Đặc điểm
- **Hiệu quả cao**: Không cần tính toán từ Order mỗi lần
- **Real-time**: Cập nhật ngay khi order completed
- **Sắp xếp**: Theo tổng số lượng đã bán giảm dần
- **Lọc**: Chỉ hiển thị sản phẩm và cửa hàng active
- **Phân trang**: Hỗ trợ đầy đủ phân trang

## So sánh với cách cũ

### **Cách cũ (tính từ Order):**
```sql
-- Chậm, phải GROUP BY mỗi lần
SELECT p.* FROM Product p 
WHERE p.status = '1' AND p.id IN (
    SELECT o.product_id FROM Order o 
    WHERE o.status = 'completed' 
    GROUP BY o.product_id 
    ORDER BY SUM(o.quantity) DESC
)
```

### **Cách mới (dùng bảng stats):**
```sql
-- Nhanh, chỉ cần JOIN
SELECT p.* FROM Product p 
INNER JOIN ProductSalesStats s ON p.id = s.product_id 
WHERE p.status = '1' 
ORDER BY s.total_quantity_sold DESC
```

## Lợi ích
1. **Performance**: Query nhanh hơn 10-100 lần
2. **Scalability**: Không bị chậm khi có nhiều order
3. **Real-time**: Thống kê luôn cập nhật
4. **Flexibility**: Có thể thêm các metric khác (revenue, rating, etc.)

## Lưu ý quan trọng
- Thống kê chỉ được cập nhật khi order chuyển sang `completed`
- Nếu order bị cancel, thống kê không bị ảnh hưởng
- Có thể reset thống kê bằng cách xóa bảng `ProductSalesStats`

## Lỗi có thể xảy ra
- **400**: Tham số phân trang không hợp lệ
- **500**: Lỗi server

## Migration Script
```sql
-- Tạo bảng ProductSalesStats
CREATE TABLE ProductSalesStats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL UNIQUE,
    total_quantity_sold INT NOT NULL DEFAULT 0,
    total_orders INT NOT NULL DEFAULT 0,
    last_sold_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES Product(id)
);

-- Khởi tạo dữ liệu từ Order hiện có
INSERT INTO ProductSalesStats (product_id, total_quantity_sold, total_orders, last_sold_at)
SELECT 
    product_id,
    SUM(quantity) as total_quantity_sold,
    COUNT(*) as total_orders,
    MAX(updated_at) as last_sold_at
FROM Order 
WHERE status = 'completed'
GROUP BY product_id;
```