# Thêm Cột Ảnh Đại Diện Vào Bảng Cửa Hàng

## 🎯 **Mục tiêu**
Thêm cột `image_url` vào bảng `Shop` để lưu ảnh đại diện của cửa hàng và trả về cho tất cả API liên quan.

## 📋 **Thay đổi thực hiện**

### **1. Database Schema**
```sql
-- Thêm cột image_url vào bảng Shop
ALTER TABLE Shop ADD COLUMN image_url VARCHAR(255) NULL;
```

### **2. Entity Shop.java**
```java
@Column(name = "image_url", length = 255)
private String imageUrl;
```

### **3. DTO Updates**

#### **ProductDetailResponse.ShopInfo**
```java
@Value
@Builder
public static class ShopInfo {
    Integer id;
    String name;
    String address;
    String phone;
    String imageUrl; // Thêm mới
    BigDecimal latitude;
    BigDecimal longitude;
    String description;
    BigDecimal rating;
    String status;
}
```

#### **ShopDetailResponse**
```java
@Value
@Builder
public class ShopDetailResponse {
    Integer id;
    String name;
    String address;
    String phone;
    String imageUrl; // Thêm mới
    Double latitude;
    Double longitude;
    String description;
    BigDecimal rating;
    String status;
    List<ProductItem> products;
}
```

### **4. Service Updates**

#### **ProductService.java**
```java
ProductDetailResponse.ShopInfo shopInfo = ProductDetailResponse.ShopInfo.builder()
    .id(shop.getId())
    .name(shop.getName())
    .address(shop.getAddress())
    .phone(shop.getPhone())
    .imageUrl(shop.getImageUrl()) // Thêm mới
    .latitude(shop.getLatitude())
    .longitude(shop.getLongitude())
    .description(shop.getDescription())
    .rating(shop.getRating())
    .status(shop.getStatus())
    .build();
```

#### **ShopService.java**
```java
ShopDetailResponse response = ShopDetailResponse.builder()
    .id(shop.getId())
    .name(shop.getName())
    .address(shop.getAddress())
    .phone(shop.getPhone())
    .imageUrl(shop.getImageUrl()) // Thêm mới
    .latitude(shop.getLatitude() != null ? shop.getLatitude().doubleValue() : null)
    .longitude(shop.getLongitude() != null ? shop.getLongitude().doubleValue() : null)
    .description(shop.getDescription())
    .rating(shop.getRating())
    .status(shop.getStatus())
    .products(productItems)
    .build();
```

## 📊 **Response Format**

### **API Product Detail:**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Pizza Margherita",
    "price": 120000,
    "shop": {
      "id": 1,
      "name": "Pizza Corner",
      "address": "123 Đường ABC, Quận 1, TP.HCM",
      "phone": "0123456789",
      "imageUrl": "https://example.com/images/pizza-corner-shop.jpg",
      "latitude": 10.76262200,
      "longitude": 106.66017200,
      "description": "Pizza ngon nhất thành phố",
      "rating": 4.50,
      "status": "1"
    }
  }
}
```

### **API Shop Detail:**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Pizza Corner",
    "address": "123 Đường ABC, Quận 1, TP.HCM",
    "phone": "0123456789",
    "imageUrl": "https://example.com/images/pizza-corner-shop.jpg",
    "latitude": 10.762622,
    "longitude": 106.660172,
    "description": "Pizza ngon nhất thành phố",
    "rating": 4.50,
    "status": "1",
    "products": [
      {
        "id": 1,
        "name": "Pizza Margherita",
        "price": 120000,
        "imageUrl": "https://example.com/images/pizza-margherita.jpg"
      }
    ]
  }
}
```

## 🔧 **Chi tiết kỹ thuật**

### **Database Schema:**
- **Cột**: `image_url VARCHAR(255)`
- **Nullable**: Có thể NULL
- **Length**: Tối đa 255 ký tự (đủ cho URL ảnh)

### **Dữ liệu mẫu:**
```sql
INSERT INTO Shop (name, address, phone, image_url, latitude, longitude, description, rating, status) VALUES 
('Pizza Corner', '123 Đường ABC, Quận 1, TP.HCM', '0123456789', 'https://example.com/images/pizza-corner-shop.jpg', 10.762622, 106.660172, 'Pizza ngon nhất thành phố', 4.5, '1'),
('Burger King', '456 Đường XYZ, Quận 2, TP.HCM', '0987654321', 'https://example.com/images/burger-king-shop.jpg', 10.763000, 106.661000, 'Burger thơm ngon', 4.2, '1'),
('Cafe Central', '789 Đường DEF, Quận 3, TP.HCM', '0369258147', 'https://example.com/images/cafe-central-shop.jpg', 10.764000, 106.662000, 'Cà phê chất lượng cao', 4.8, '1');
```

## 🧪 **Test Cases**

### **Test API với ảnh cửa hàng:**
```bash
# Test product detail (có shop info với imageUrl)
curl "http://localhost:8080/products/1"

# Test shop detail (có imageUrl)
curl "http://localhost:8080/shops/1"

# Test search products (có shop info với imageUrl)
curl "http://localhost:8080/products?page=0&size=5"
```

### **Kiểm tra response:**
```json
{
  "shop": {
    "id": 1,
    "name": "Pizza Corner",
    "address": "123 Đường ABC, Quận 1, TP.HCM",
    "phone": "0123456789",
    "imageUrl": "https://example.com/images/pizza-corner-shop.jpg",
    "latitude": 10.76262200,
    "longitude": 106.66017200
  }
}
```

## 📁 **Files đã cập nhật**

1. **`Shop.java`** - Thêm field `imageUrl`
2. **`ProductDetailResponse.java`** - Thêm `imageUrl` vào `ShopInfo`
3. **`ShopDetailResponse.java`** - Thêm field `imageUrl`
4. **`ProductService.java`** - Cập nhật tạo `ShopInfo` với `imageUrl`
5. **`ShopService.java`** - Cập nhật tạo `ShopDetailResponse` với `imageUrl`
6. **`create_tables.sql`** - Cập nhật schema và dữ liệu mẫu
7. **`add_shop_image_url.sql`** - Script migration cho database hiện tại

## 🚀 **Migration Steps**

### **Cho database mới:**
```bash
# Chạy file create_tables.sql (đã cập nhật)
mysql -u root -p foodshare < create_tables.sql
```

### **Cho database hiện tại:**
```bash
# Chạy file migration
mysql -u root -p foodshare < add_shop_image_url.sql
```

## ✅ **Lợi ích cho Frontend**

### **1. Hiển thị ảnh cửa hàng:**
- Ảnh đại diện cho cửa hàng
- Tăng tính nhận diện thương hiệu
- Tạo ấn tượng tốt cho người dùng

### **2. UI/UX tốt hơn:**
```jsx
const ShopCard = ({ shop }) => {
  return (
    <div className="shop-card">
      <img 
        src={shop.imageUrl} 
        alt={shop.name}
        className="shop-image"
        onError={(e) => {
          e.target.src = '/images/default-shop.jpg';
        }}
      />
      <div className="shop-info">
        <h3>{shop.name}</h3>
        <p>📍 {shop.address}</p>
        <p>📞 {shop.phone}</p>
        <p>⭐ {shop.rating}/5</p>
      </div>
    </div>
  );
};
```

### **3. Responsive design:**
```css
.shop-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
}

.shop-card {
  display: flex;
  flex-direction: column;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
}
```

## 🎨 **Frontend Usage**

### **Hiển thị ảnh cửa hàng:**
```javascript
const ShopImage = ({ shop }) => {
  if (!shop.imageUrl) {
    return (
      <div className="default-shop-image">
        <span>🏪</span>
      </div>
    );
  }
  
  return (
    <img 
      src={shop.imageUrl} 
      alt={`${shop.name} - ${shop.address}`}
      className="shop-image"
      loading="lazy"
    />
  );
};
```

### **Fallback image:**
```javascript
const handleImageError = (e) => {
  e.target.src = '/images/default-shop.jpg';
  e.target.alt = 'Cửa hàng';
};

// Sử dụng
<img 
  src={shop.imageUrl} 
  alt={shop.name}
  onError={handleImageError}
/>
```

## 📝 **Lưu ý**

- **Format ảnh**: Hỗ trợ tất cả format web (JPG, PNG, WebP)
- **Kích thước**: Nên optimize ảnh trước khi upload
- **Nullable**: Cột có thể NULL nếu cửa hàng chưa cập nhật ảnh
- **CDN**: Nên sử dụng CDN để tối ưu tốc độ tải ảnh

## 🔄 **So sánh với Product Image**

| Aspect | Product Image | Shop Image |
|--------|---------------|------------|
| **Mục đích** | Ảnh sản phẩm | Ảnh đại diện cửa hàng |
| **Kích thước** | Thường nhỏ hơn | Thường lớn hơn |
| **Nội dung** | Sản phẩm cụ thể | Không gian cửa hàng |
| **Sử dụng** | Trong danh sách sản phẩm | Trong thông tin cửa hàng |

Bây giờ tất cả API đều trả về ảnh đại diện cửa hàng! 🏪📸✨
