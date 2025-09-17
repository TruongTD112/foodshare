# Cập Nhật API Sản Phẩm Bán Chạy - Thêm TotalOrders

## 🎯 **Mục tiêu**
Thêm field `totalOrders` (số lượng đơn đã đặt) vào API sản phẩm bán chạy để hiển thị thông tin chi tiết hơn cho người dùng.

## 📋 **Thay đổi thực hiện**

### **1. ProductSearchItem.java**
```java
// Thêm field mới
Integer totalOrders; // Số lượng đơn đã đặt (chỉ cho API popular)
```

### **2. ProductService.java**
- **Thêm dependency**: `ProductSalesStatsRepository`
- **Logic mới**: Lấy thông tin `totalOrders` từ bảng `ProductSalesStats`
- **Chỉ áp dụng**: API `searchPopularProducts` (sản phẩm bán chạy)

### **3. Logic xử lý**
```java
// Lấy thông tin sales stats cho popular products
Set<Integer> productIds = products.stream()
        .map(Product::getId)
        .collect(Collectors.toSet());

Map<Integer, ProductSalesStats> statsByProductId = new HashMap<>();
if (!productIds.isEmpty()) {
    statsByProductId = productSalesStatsRepository.findAll().stream()
            .filter(stats -> productIds.contains(stats.getProductId()))
            .collect(Collectors.toMap(ProductSalesStats::getProductId, stats -> stats));
}

// Lấy totalOrders cho từng sản phẩm
ProductSalesStats salesStats = statsByProductId.get(product.getId());
Integer totalOrders = salesStats != null ? salesStats.getTotalOrders() : 0;
```

## 📊 **Response Format**

### **API Popular Products:**
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
        "discountPercentage": 20,
        "imageUrl": "https://example.com/pizza.jpg",
        "shopId": 1,
        "shopName": "Pizza Corner",
        "shopLatitude": 10.76262200,
        "shopLongitude": 106.66017200,
        "distanceKm": 2.5,
        "totalOrders": 25  // Số lượng đơn đã đặt
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

### **API khác (không có totalOrders):**
```json
{
  "productId": 1,
  "name": "Pizza Margherita",
  "price": 120000,
  "originalPrice": 150000,
  "discountPercentage": 20,
  "imageUrl": "https://example.com/pizza.jpg",
  "shopId": 1,
  "shopName": "Pizza Corner",
  "shopLatitude": 10.76262200,
  "shopLongitude": 106.66017200,
  "distanceKm": 2.5,
  "totalOrders": null  // Không có thông tin đơn hàng
}
```

## 🔧 **Cách hoạt động**

### **1. API Popular Products:**
- **Lấy sản phẩm**: Từ `Product` table với sắp xếp theo `totalQuantitySold`
- **Lấy thông tin shop**: Từ `Shop` table
- **Lấy thông tin đơn hàng**: Từ `ProductSalesStats` table
- **Trả về**: `totalOrders` có giá trị thực

### **2. API khác:**
- **Lấy sản phẩm**: Từ `Product` table
- **Lấy thông tin shop**: Từ `Shop` table
- **Trả về**: `totalOrders = null`

## 📈 **Lợi ích**

### **Cho người dùng:**
- **Thông tin chi tiết**: Biết sản phẩm nào được đặt nhiều nhất
- **Độ tin cậy**: Sản phẩm có nhiều đơn = được tin tưởng
- **Quyết định mua**: Dựa trên dữ liệu thực tế

### **Cho shop:**
- **Hiển thị uy tín**: Số lượng đơn đặt tăng độ tin cậy
- **Marketing**: Có thể quảng bá sản phẩm bán chạy
- **Phân tích**: Hiểu được sản phẩm nào được ưa chuộng

## 🧪 **Test Cases**

### **Test API Popular Products:**
```bash
# Test với tọa độ
curl "http://localhost:8080/products/popular?lat=10.762622&lon=106.660172&page=0&size=5"

# Test không có tọa độ
curl "http://localhost:8080/products/popular?page=0&size=5"
```

### **Kiểm tra response:**
```json
{
  "content": [
    {
      "productId": 1,
      "name": "Pizza Margherita",
      "totalOrders": 25,  // Phải có giá trị
      "distanceKm": 2.5
    }
  ]
}
```

### **Test API khác:**
```bash
# Test API giảm giá
curl "http://localhost:8080/products/top-discounts?page=0&size=5"

# Kiểm tra totalOrders = null
```

## 📁 **Files đã cập nhật**

1. **`ProductSearchItem.java`** - Thêm field `totalOrders`
2. **`ProductService.java`** - Logic lấy thông tin đơn hàng
3. **`FoodShare_API_Collection.postman_collection.json`** - Cập nhật test cases

## ✅ **Kết quả**

- **API Popular Products**: Trả về `totalOrders` với giá trị thực từ database
- **API khác**: Trả về `totalOrders = null`
- **Hiệu suất**: Chỉ query thêm `ProductSalesStats` cho API popular
- **Tương thích ngược**: Không ảnh hưởng API hiện tại

## 🚀 **Cách sử dụng**

### **Frontend hiển thị:**
```javascript
// Hiển thị số lượng đơn đặt
if (product.totalOrders !== null) {
    console.log(`Đã có ${product.totalOrders} đơn đặt sản phẩm này`);
} else {
    console.log('Không có thông tin đơn hàng');
}
```

### **Sắp xếp theo độ phổ biến:**
```javascript
// Sắp xếp sản phẩm theo số lượng đơn đặt
products.sort((a, b) => (b.totalOrders || 0) - (a.totalOrders || 0));
```

Bây giờ API sản phẩm bán chạy đã trả về thông tin chi tiết về số lượng đơn đặt! 🎉📊
