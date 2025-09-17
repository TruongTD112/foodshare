# API Lấy Sản Phẩm Giảm Giá Nhiều Nhất

## Mô tả
API đơn giản để lấy danh sách sản phẩm có giảm giá nhiều nhất, sắp xếp theo mức giảm giá giảm dần. Không cần tìm kiếm theo tên hay vị trí.

## Endpoint
```
GET /products/top-discounts
```

## Tham số
| Tham số | Loại | Bắt buộc | Mô tả | Ví dụ |
|---------|------|----------|-------|-------|
| page | Integer | Không | Số trang (0-based, mặc định: 0) | 0 |
| size | Integer | Không | Kích thước trang (mặc định: 20, tối đa: 100) | 20 |

## Ví dụ sử dụng

### 1. Lấy trang đầu tiên (mặc định)
```bash
curl -X GET "http://localhost:8080/products/top-discounts"
```

### 2. Lấy 10 sản phẩm đầu tiên
```bash
curl -X GET "http://localhost:8080/products/top-discounts?size=10"
```

### 3. Lấy trang thứ 2
```bash
curl -X GET "http://localhost:8080/products/top-discounts?page=1&size=20"
```

### 4. Sử dụng với JavaScript/Fetch
```javascript
const getTopDiscountedProducts = async (page = 0, size = 20) => {
  const response = await fetch(`/products/top-discounts?page=${page}&size=${size}`);
  return await response.json();
};

// Sử dụng
const result = await getTopDiscountedProducts(0, 10);
console.log(result);
```

### 5. Sử dụng với Axios
```javascript
import axios from 'axios';

const getTopDiscountedProducts = async (page = 0, size = 20) => {
  try {
    const response = await axios.get('/products/top-discounts', {
      params: { page, size }
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching top discounted products:', error);
    throw error;
  }
};
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
        "originalPrice": 200000,
        "discountPercentage": 40.00,
        "imageUrl": "https://example.com/pizza.jpg",
        "shopId": 1,
        "shopName": "Pizza Corner",
        "shopLatitude": 10.763000,
        "shopLongitude": 106.661000,
        "distanceKm": null
      },
      {
        "productId": 2,
        "name": "Burger Deluxe",
        "price": 80000,
        "originalPrice": 120000,
        "discountPercentage": 33.33,
        "imageUrl": "https://example.com/burger.jpg",
        "shopId": 2,
        "shopName": "Burger King",
        "shopLatitude": 10.764000,
        "shopLongitude": 106.662000,
        "distanceKm": null
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 2,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

## Đặc điểm
- **Sắp xếp**: Theo số tiền giảm giảm dần (sản phẩm giảm giá nhiều nhất lên đầu)
- **Lọc**: Chỉ hiển thị sản phẩm có `originalPrice > price` (có giảm giá thực sự)
- **Trạng thái**: Chỉ hiển thị sản phẩm và cửa hàng đang hoạt động
- **Tính toán**: Tự động tính phần trăm giảm giá nếu chưa có
- **Phân trang**: Hỗ trợ phân trang với page và size
- **Đơn giản**: Không cần tham số tìm kiếm phức tạp

## Lưu ý quan trọng
- API này rất đơn giản, chỉ cần 2 tham số: `page` và `size`
- Không cần tìm kiếm theo tên, vị trí, hay bất kỳ điều kiện nào khác
- Sản phẩm được sắp xếp theo mức giảm giá (originalPrice - price) giảm dần
- `distanceKm` luôn là `null` vì không tính khoảng cách

## Lỗi có thể xảy ra
- **400**: Tham số phân trang không hợp lệ (page < 0 hoặc size không trong khoảng 1-100)
- **500**: Lỗi server

## So sánh với API khác
| API | Mục đích | Tham số | Phức tạp |
|-----|----------|---------|----------|
| `/products/top-discounts` | Lấy sản phẩm giảm giá nhiều nhất | page, size | Đơn giản |
| `/products/discount` | Tìm kiếm sản phẩm giảm giá | q, sortBy, lat, lon, maxDistanceKm, page, size | Phức tạp |
| `/products/nearby` | Tìm sản phẩm gần đây | lat, lon, page, size | Trung bình |

## Cấu trúc Database
Cần có các trường sau trong bảng Product:
```sql
ALTER TABLE Product ADD COLUMN original_price DECIMAL(10,2);
ALTER TABLE Product ADD COLUMN discount_percentage DECIMAL(5,2);
```

## Ví dụ dữ liệu
```sql
-- Sản phẩm giảm giá 80,000 VND (từ 200,000 xuống 120,000)
INSERT INTO Product (shop_id, category_id, name, price, original_price, discount_percentage, status) 
VALUES (1, 1, 'Pizza Margherita', 120000, 200000, 40.00, '1');

-- Sản phẩm giảm giá 40,000 VND (từ 120,000 xuống 80,000)  
INSERT INTO Product (shop_id, category_id, name, price, original_price, discount_percentage, status) 
VALUES (2, 1, 'Burger Deluxe', 80000, 120000, 33.33, '1');
```

Với dữ liệu trên, API sẽ trả về Pizza Margherita trước vì giảm giá nhiều hơn (80,000 VND vs 40,000 VND).
