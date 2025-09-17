# API Tìm Kiếm Sản Phẩm Gần Đây

## Mô tả
API này cho phép tìm kiếm tất cả sản phẩm trong bán kính mặc định (50km) từ vị trí người dùng.

## Endpoint
```
GET /products/nearby
```

## Tham số
| Tham số | Loại | Bắt buộc | Mô tả | Ví dụ |
|---------|------|----------|-------|-------|
| lat | Double | Có | Vĩ độ của người dùng | 10.762622 |
| lon | Double | Có | Kinh độ của người dùng | 106.660172 |
| page | Integer | Không | Số trang (0-based, mặc định: 0) | 0 |
| size | Integer | Không | Kích thước trang (mặc định: 20, tối đa: 100) | 20 |

## Ví dụ sử dụng

### 1. Tìm kiếm cơ bản
```bash
curl -X GET "http://localhost:8080/products/nearby?lat=10.762622&lon=106.660172"
```

### 2. Tìm kiếm với phân trang
```bash
curl -X GET "http://localhost:8080/products/nearby?lat=10.762622&lon=106.660172&page=0&size=10"
```

### 3. Sử dụng với JavaScript/Fetch
```javascript
const searchNearbyProducts = async (latitude, longitude, page = 0, size = 20) => {
  const response = await fetch(
    `/products/nearby?lat=${latitude}&lon=${longitude}&page=${page}&size=${size}`
  );
  return await response.json();
};

// Sử dụng
const result = await searchNearbyProducts(10.762622, 106.660172);
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
        "price": 150000,
        "imageUrl": "https://example.com/pizza.jpg",
        "shopId": 1,
        "shopName": "Pizza Corner",
        "shopLatitude": 10.763000,
        "shopLongitude": 106.661000,
        "distanceKm": 2.5
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

## Đặc điểm
- **Khoảng cách mặc định**: 50km (có thể cấu hình trong Constants.Distance.DEFAULT_MAX_DISTANCE_KM)
- **Sắp xếp**: Theo khoảng cách tăng dần (gần nhất trước)
- **Lọc**: Chỉ hiển thị sản phẩm và cửa hàng đang hoạt động
- **Phân trang**: Hỗ trợ phân trang với page và size
- **Validation**: Kiểm tra tọa độ hợp lệ (-90 ≤ lat ≤ 90, -180 ≤ lon ≤ 180)

## Lỗi có thể xảy ra
- **400**: Thiếu tọa độ hoặc tọa độ không hợp lệ
- **500**: Lỗi server

## Lưu ý
- API này sử dụng công thức Haversine để tính khoảng cách chính xác
- Chỉ trả về sản phẩm từ các cửa hàng có trạng thái "1" (active)
- Chỉ trả về sản phẩm có trạng thái "1" (active)
