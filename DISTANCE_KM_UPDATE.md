# Cập Nhật API Trả Về DistanceKm

## 🎯 **Mục tiêu**
Cập nhật tất cả API sản phẩm để trả về `distanceKm` trong response, giúp client hiển thị khoảng cách từ người dùng đến sản phẩm.

## 📋 **Thay đổi thực hiện**

### **1. ProductService.java**
- **API `searchTopDiscountedProducts`**: Thêm tham số `latitude`, `longitude` (tùy chọn)
- **API `searchPopularProducts`**: Thêm tham số `latitude`, `longitude` (tùy chọn)
- **Tính toán khoảng cách**: Tất cả API đều tính toán `distanceKm` khi có tọa độ
- **Logic tính toán**: Sử dụng công thức Haversine để tính khoảng cách chính xác

### **2. ProductController.java**
- **API `/products/top-discounts`**: Thêm tham số `lat`, `lon` (tùy chọn)
- **API `/products/popular`**: Thêm tham số `lat`, `lon` (tùy chọn)
- **Swagger documentation**: Cập nhật mô tả API với tham số mới

### **3. ProductSearchItem.java**
- **Field `distanceKm`**: Đã có sẵn, không cần thay đổi
- **Kiểu dữ liệu**: `Double` (có thể null nếu không có tọa độ)

## 🔧 **Cách hoạt động**

### **Khi có tọa độ:**
```json
{
  "productId": 1,
  "name": "Pizza Margherita",
  "price": 150000,
  "originalPrice": 200000,
  "discountPercentage": 25,  // Chỉ hiển thị phần nguyên
  "shopName": "Pizza Corner",
  "shopLatitude": 10.762622,
  "shopLongitude": 106.660172,
  "distanceKm": 2.5  // Khoảng cách tính bằng km
}
```

### **Khi không có tọa độ:**
```json
{
  "productId": 1,
  "name": "Pizza Margherita",
  "price": 150000,
  "originalPrice": 200000,
  "discountPercentage": 25,  // Chỉ hiển thị phần nguyên
  "shopName": "Pizza Corner",
  "shopLatitude": 10.762622,
  "shopLongitude": 106.660172,
  "distanceKm": null  // Không tính khoảng cách
}
```

## 📊 **API Endpoints được cập nhật**

### **1. Tìm kiếm chung** - `GET /products`
- **Tham số mới**: `lat`, `lon` (tùy chọn)
- **Tính năng**: Tính khoảng cách khi có tọa độ
- **Sắp xếp**: Theo khoảng cách (nếu có tọa độ) hoặc theo giá

### **2. Tìm kiếm gần đây** - `GET /products/nearby`
- **Tham số**: `lat`, `lon` (bắt buộc)
- **Tính năng**: Luôn tính khoảng cách và sắp xếp theo khoảng cách
- **Bán kính**: Sử dụng khoảng cách mặc định (50km)

### **3. Sản phẩm giảm giá** - `GET /products/top-discounts`
- **Tham số mới**: `lat`, `lon` (tùy chọn)
- **Tính năng**: Tính khoảng cách khi có tọa độ
- **Sắp xếp**: Theo mức giảm giá (không thay đổi)

### **4. Sản phẩm bán chạy** - `GET /products/popular`
- **Tham số mới**: `lat`, `lon` (tùy chọn)
- **Tính năng**: Tính khoảng cách khi có tọa độ
- **Sắp xếp**: Theo số lượng bán (không thay đổi)

## 🧪 **Test Cases**

### **Test với tọa độ:**
```bash
# Sản phẩm giảm giá với tọa độ
curl "http://localhost:8080/products/top-discounts?lat=10.762622&lon=106.660172&page=0&size=5"

# Sản phẩm bán chạy với tọa độ
curl "http://localhost:8080/products/popular?lat=10.762622&lon=106.660172&page=0&size=5"
```

### **Test không có tọa độ:**
```bash
# Sản phẩm giảm giá không có tọa độ
curl "http://localhost:8080/products/top-discounts?page=0&size=5"

# Sản phẩm bán chạy không có tọa độ
curl "http://localhost:8080/products/popular?page=0&size=5"
```

## 📁 **Files đã cập nhật**

1. **`ProductService.java`** - Logic tính toán khoảng cách
2. **`ProductController.java`** - API endpoints với tham số mới
3. **`FoodShare_API_Collection.postman_collection.json`** - Postman collection
4. **`test_api_curl.sh`** - Script test cURL
5. **`POSTMAN_TEST_GUIDE.md`** - Hướng dẫn test

## ✅ **Kết quả**

- **Tất cả API** đều trả về `distanceKm` trong response
- **Tương thích ngược**: API vẫn hoạt động khi không có tọa độ
- **Hiệu suất**: Chỉ tính khoảng cách khi cần thiết
- **Chính xác**: Sử dụng công thức Haversine chuẩn
- **Linh hoạt**: Client có thể chọn có/không truyền tọa độ
- **Phần trăm giảm giá**: Chỉ hiển thị phần nguyên (không có số thập phân)

## 🚀 **Cách sử dụng**

### **Client có tọa độ:**
```javascript
// Gọi API với tọa độ để có khoảng cách
const response = await fetch('/products/popular?lat=10.762622&lon=106.660172');
const data = await response.json();
// data.content[0].distanceKm sẽ có giá trị (km)
```

### **Client không có tọa độ:**
```javascript
// Gọi API không có tọa độ
const response = await fetch('/products/popular');
const data = await response.json();
// data.content[0].distanceKm sẽ là null
```

Tất cả API đã được cập nhật để trả về `distanceKm` một cách nhất quán! 🎉
