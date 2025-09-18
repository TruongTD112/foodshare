# Cập Nhật API Chi Tiết Sản Phẩm

## 🎯 **Mục tiêu**
Cập nhật API chi tiết sản phẩm (`/products/{id}`) để trả về đầy đủ thông tin giống như API search, bao gồm `originalPrice`, `discountPercentage` và `totalOrders`.

## 📋 **Thay đổi thực hiện**

### **1. ProductDetailResponse DTO**
```java
@Value
@Builder
public class ProductDetailResponse {
    Integer id;
    Integer shopId;
    Integer categoryId;
    String name;
    String description;
    BigDecimal price;
    BigDecimal originalPrice;           // ✅ Thêm mới
    BigDecimal discountPercentage;      // ✅ Thêm mới
    String imageUrl;
    String detailImageUrl;
    Integer quantityAvailable;
    Integer quantityPending;
    String status;
    Integer totalOrders;                // ✅ Thêm mới
    ShopInfo shop;
}
```

### **2. ProductService.getProductDetail()**
```java
// Tính toán discount percentage (chỉ lấy phần nguyên)
BigDecimal discountPercentage = BigDecimal.ZERO;
if (product.getOriginalPrice() != null && product.getPrice() != null && product.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
    BigDecimal discountAmount = product.getOriginalPrice().subtract(product.getPrice());
    discountPercentage = discountAmount.divide(product.getOriginalPrice(), 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .setScale(0, RoundingMode.HALF_UP);
}

// Lấy thông tin totalOrders từ ProductSalesStats
ProductSalesStats salesStats = productSalesStatsRepository.findByProductId(product.getId()).orElse(null);
Integer totalOrders = salesStats != null ? salesStats.getTotalOrders() : 0;

// Tạo response với đầy đủ thông tin
ProductDetailResponse response = ProductDetailResponse.builder()
    .id(product.getId())
    .shopId(product.getShopId())
    .categoryId(product.getCategoryId())
    .name(product.getName())
    .description(product.getDescription())
    .price(product.getPrice())
    .originalPrice(product.getOriginalPrice())        // ✅ Thêm
    .discountPercentage(discountPercentage)           // ✅ Thêm
    .imageUrl(product.getImageUrl())
    .detailImageUrl(product.getDetailImageUrl())
    .quantityAvailable(product.getQuantityAvailable())
    .quantityPending(product.getQuantityPending())
    .status(product.getStatus())
    .totalOrders(totalOrders)                         // ✅ Thêm
    .shop(shopInfo)
    .build();
```

## 📊 **Response Format**

### **Trước khi cập nhật:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Pizza Margherita",
    "price": 120000,
    "imageUrl": "https://example.com/images/pizza-margherita.jpg",
    "detailImageUrl": "https://example.com/images/pizza-margherita-detail1.jpg",
    "quantityAvailable": 50,
    "quantityPending": 0,
    "status": "1",
    "shop": {
      "id": 1,
      "name": "Pizza Corner",
      "phone": "0123456789"
    }
  }
}
```

### **Sau khi cập nhật:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Pizza Margherita",
    "price": 120000,
    "originalPrice": 150000,           // ✅ Giá gốc
    "discountPercentage": 20,          // ✅ Phần trăm giảm giá
    "imageUrl": "https://example.com/images/pizza-margherita.jpg",
    "detailImageUrl": "https://example.com/images/pizza-margherita-detail1.jpg",
    "quantityAvailable": 50,
    "quantityPending": 0,
    "status": "1",
    "totalOrders": 25,                 // ✅ Số lượt mua
    "shop": {
      "id": 1,
      "name": "Pizza Corner",
      "phone": "0123456789",
      "imageUrl": "https://example.com/images/pizza-corner-shop.jpg"
    }
  }
}
```

## 🔧 **Chi tiết kỹ thuật**

### **1. Discount Percentage Calculation:**
```java
// Công thức tính phần trăm giảm giá
BigDecimal discountAmount = originalPrice - price;
BigDecimal discountPercentage = (discountAmount / originalPrice) * 100;

// Làm tròn về phần nguyên
discountPercentage = discountPercentage.setScale(0, RoundingMode.HALF_UP);
```

### **2. Total Orders Lookup:**
```java
// Lấy thống kê bán hàng từ ProductSalesStats
ProductSalesStats salesStats = productSalesStatsRepository.findByProductId(productId).orElse(null);
Integer totalOrders = salesStats != null ? salesStats.getTotalOrders() : 0;
```

### **3. Performance:**
- **Single query** cho ProductSalesStats
- **No N+1 problem** - Chỉ 1 query thêm
- **Cached data** - ProductSalesStats được cập nhật real-time

## 🧪 **Test Cases**

### **Test API chi tiết sản phẩm:**
```bash
# Test sản phẩm có giảm giá
curl "http://localhost:8080/products/1"

# Test sản phẩm không giảm giá
curl "http://localhost:8080/products/2"

# Test sản phẩm chưa có đơn hàng
curl "http://localhost:8080/products/3"
```

### **Kiểm tra response:**
```json
{
  "data": {
    "id": 1,
    "name": "Pizza Margherita",
    "price": 120000,
    "originalPrice": 150000,
    "discountPercentage": 20,
    "totalOrders": 25,
    "shop": {
      "name": "Pizza Corner",
      "phone": "0123456789"
    }
  }
}
```

## 📁 **Files đã cập nhật**

1. **`ProductDetailResponse.java`** - Đã có sẵn các field cần thiết
2. **`ProductService.java`** - Cập nhật method `getProductDetail()`

## ✅ **Lợi ích**

### **1. Consistency:**
- API chi tiết giống API search
- Thông tin đầy đủ cho frontend
- User experience nhất quán

### **2. Business Value:**
- Hiển thị giá gốc và giá giảm
- Phần trăm giảm giá rõ ràng
- Số lượt mua tăng độ tin cậy

### **3. Frontend Usage:**
```jsx
const ProductDetail = ({ product }) => {
  const hasDiscount = product.originalPrice && product.originalPrice > product.price;
  
  return (
    <div className="product-detail">
      <h1>{product.name}</h1>
      
      <div className="price-section">
        <span className="current-price">{product.price.toLocaleString()} VNĐ</span>
        {hasDiscount && (
          <>
            <span className="original-price">{product.originalPrice.toLocaleString()} VNĐ</span>
            <span className="discount">-{product.discountPercentage}%</span>
          </>
        )}
      </div>
      
      <div className="stats">
        <span>Đã bán: {product.totalOrders} đơn</span>
        <span>Còn lại: {product.quantityAvailable} sản phẩm</span>
      </div>
    </div>
  );
};
```

## 🔄 **So sánh với API Search**

| Field | Search API | Detail API | Status |
|-------|------------|------------|--------|
| **originalPrice** | ✅ | ✅ | ✅ Consistent |
| **discountPercentage** | ✅ | ✅ | ✅ Consistent |
| **totalOrders** | ✅ | ✅ | ✅ Consistent |
| **distanceKm** | ✅ | ❌ | ⚠️ Detail không cần |
| **shopLatitude** | ✅ | ✅ (trong shop) | ✅ Consistent |

## 📝 **Lưu ý**

### **1. Discount Logic:**
- Chỉ tính khi `originalPrice > 0`
- Làm tròn về phần nguyên
- Hiển thị 0% nếu không có giảm giá

### **2. Total Orders:**
- Lấy từ `ProductSalesStats` table
- Trả về 0 nếu chưa có thống kê
- Real-time data

### **3. Performance:**
- Thêm 1 query cho mỗi product detail
- Có thể cache nếu cần thiết

Bây giờ API chi tiết sản phẩm đã trả về đầy đủ thông tin! 🎯✨
