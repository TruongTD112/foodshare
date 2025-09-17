# Thêm TotalOrders Cho Tất Cả API Sản Phẩm

## 🎯 **Mục tiêu**
Cập nhật tất cả API có sản phẩm để trả về `totalOrders` (số lượng đã bán) cho mọi sản phẩm.

## 📋 **Thay đổi thực hiện**

### **1. ProductSearchItem DTO**
```java
// Cập nhật comment
Integer totalOrders; // Số lượng đơn đã đặt (cho tất cả API)
```

### **2. ProductSalesStatsRepository**
```java
// Thêm method mới
/**
 * Tìm thống kê bán hàng theo danh sách productId
 */
List<ProductSalesStats> findByProductIdIn(Set<Integer> productIds);
```

### **3. ProductService Updates**

#### **searchProducts() - API tìm kiếm chung**
```java
// Thêm logic lấy sales stats
Set<Integer> productIds = products.stream()
    .map(Product::getId)
    .filter(Objects::nonNull)
    .collect(Collectors.toSet());
Map<Integer, ProductSalesStats> statsByProductId = new HashMap<>();
if (!productIds.isEmpty()) {
    statsByProductId = productSalesStatsRepository.findByProductIdIn(productIds)
        .stream()
        .collect(Collectors.toMap(ProductSalesStats::getProductId, stats -> stats));
}

// Cập nhật tạo ProductSearchItem
ProductSalesStats salesStats = statsByProductId.get(product.getId());
Integer totalOrders = salesStats != null ? salesStats.getTotalOrders() : 0;

items.add(ProductSearchItem.builder()
    // ... other fields
    .totalOrders(totalOrders) // Số lượng đơn đã đặt
    .build());
```

#### **searchTopDiscountedProducts() - API sản phẩm giảm giá**
```java
// Thêm logic lấy sales stats tương tự searchProducts
// Cập nhật tạo ProductSearchItem với totalOrders
```

#### **searchNearbyProducts() - API sản phẩm gần đây**
```java
// Gọi searchProducts() nên đã được cập nhật tự động
```

#### **searchPopularProducts() - API sản phẩm bán chạy**
```java
// Đã có totalOrders từ trước, không cần thay đổi
```

## 📊 **Response Format**

### **Tất cả API sản phẩm đều trả về:**
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
        "imageUrl": "https://example.com/images/pizza-margherita.jpg",
        "detailImageUrl": "https://example.com/images/pizza-margherita-detail1.jpg,https://example.com/images/pizza-margherita-detail2.jpg",
        "shopId": 1,
        "shopName": "Pizza Corner",
        "shopLatitude": 10.76262200,
        "shopLongitude": 106.66017200,
        "distanceKm": 2.0,
        "totalOrders": 25
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

## 🔧 **Chi tiết kỹ thuật**

### **Performance Optimization:**
- **Batch query**: Lấy tất cả `ProductSalesStats` trong 1 query thay vì query từng sản phẩm
- **Memory efficient**: Sử dụng `Map` để lookup nhanh
- **Null safety**: Xử lý trường hợp sản phẩm chưa có thống kê

### **Data Flow:**
1. **Lấy sản phẩm** từ database với pagination
2. **Lấy shop info** cho tất cả sản phẩm
3. **Lấy sales stats** cho tất cả sản phẩm (batch query)
4. **Tạo response** với đầy đủ thông tin

## 🧪 **Test Cases**

### **Test tất cả API:**
```bash
# Test search products (có totalOrders)
curl "http://localhost:8080/products?page=0&size=5"

# Test nearby products (có totalOrders)
curl "http://localhost:8080/products/nearby?lat=10.762622&lon=106.660172&page=0&size=5"

# Test popular products (có totalOrders)
curl "http://localhost:8080/products/popular?page=0&size=5"

# Test top discounts (có totalOrders)
curl "http://localhost:8080/products/top-discounts?page=0&size=5"
```

### **Kiểm tra response:**
```json
{
  "content": [
    {
      "productId": 1,
      "name": "Pizza Margherita",
      "totalOrders": 25,
      "shopName": "Pizza Corner"
    },
    {
      "productId": 2,
      "name": "Pizza Pepperoni",
      "totalOrders": 0,
      "shopName": "Pizza Corner"
    }
  ]
}
```

## 📁 **Files đã cập nhật**

1. **`ProductSearchItem.java`** - Cập nhật comment
2. **`ProductSalesStatsRepository.java`** - Thêm method `findByProductIdIn`
3. **`ProductService.java`** - Cập nhật 2 method:
   - `searchProducts()` - Thêm logic lấy sales stats
   - `searchTopDiscountedProducts()` - Thêm logic lấy sales stats

## ✅ **Lợi ích**

### **1. Thông tin đầy đủ:**
- Tất cả API đều trả về số lượng đã bán
- Người dùng biết sản phẩm nào bán chạy
- Tăng độ tin cậy cho sản phẩm

### **2. UI/UX tốt hơn:**
```jsx
const ProductCard = ({ product }) => {
  return (
    <div className="product-card">
      <img src={product.imageUrl} alt={product.name} />
      <h3>{product.name}</h3>
      <p>Giá: {product.price.toLocaleString()} VNĐ</p>
      <p>Đã bán: {product.totalOrders} đơn</p>
      <p>Cửa hàng: {product.shopName}</p>
    </div>
  );
};
```

### **3. Business Intelligence:**
- Phân tích xu hướng bán hàng
- Xác định sản phẩm hot
- Tối ưu inventory

## 🎨 **Frontend Usage**

### **Hiển thị số lượng đã bán:**
```javascript
const formatTotalOrders = (totalOrders) => {
  if (totalOrders === 0) return 'Chưa có đơn hàng';
  if (totalOrders === 1) return '1 đơn đã bán';
  return `${totalOrders} đơn đã bán`;
};

// Sử dụng
const ordersText = formatTotalOrders(product.totalOrders);
console.log(ordersText); // "25 đơn đã bán"
```

### **Sorting theo số lượng bán:**
```javascript
const sortByPopularity = (products) => {
  return products.sort((a, b) => b.totalOrders - a.totalOrders);
};

// Sử dụng
const popularProducts = sortByPopularity(products);
```

### **Filter sản phẩm bán chạy:**
```javascript
const getPopularProducts = (products, minOrders = 10) => {
  return products.filter(product => product.totalOrders >= minOrders);
};

// Sử dụng
const hotProducts = getPopularProducts(products, 20);
```

## 📊 **API Comparison**

| API | Trước | Sau |
|-----|-------|-----|
| **Search Products** | `totalOrders: null` | `totalOrders: 25` |
| **Nearby Products** | `totalOrders: null` | `totalOrders: 25` |
| **Popular Products** | `totalOrders: 25` | `totalOrders: 25` |
| **Top Discounts** | `totalOrders: null` | `totalOrders: 25` |

## 🚀 **Performance Impact**

### **Database Queries:**
- **Trước**: 1 query cho products + 1 query cho shops
- **Sau**: 1 query cho products + 1 query cho shops + 1 query cho sales stats

### **Memory Usage:**
- **Tăng nhẹ**: Thêm Map để lưu sales stats
- **Trade-off**: Tăng 1 query để có thông tin đầy đủ

### **Response Size:**
- **Tăng nhẹ**: Thêm field `totalOrders` (4 bytes per product)
- **Lợi ích**: Thông tin hữu ích cho frontend

## 📝 **Lưu ý**

- **Default value**: Sản phẩm chưa có đơn hàng sẽ có `totalOrders = 0`
- **Performance**: Batch query để tối ưu database access
- **Consistency**: Tất cả API đều có cùng format response

Bây giờ tất cả API sản phẩm đều trả về số lượng đã bán! 📊✨
