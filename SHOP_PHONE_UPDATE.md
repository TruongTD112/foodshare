# Thêm Cột Số Điện Thoại Vào Bảng Cửa Hàng

## 🎯 **Mục tiêu**
Thêm cột `phone` vào bảng `Shop` để lưu số điện thoại liên hệ của cửa hàng và trả về cho tất cả API liên quan.

## 📋 **Thay đổi thực hiện**

### **1. Database Schema**
```sql
-- Thêm cột phone vào bảng Shop
ALTER TABLE Shop ADD COLUMN phone VARCHAR(20) NULL;
```

### **2. Entity Shop.java**
```java
@Column(name = "phone", length = 20)
private String phone;
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
    String phone; // Thêm mới
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
    String phone; // Thêm mới
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
    .phone(shop.getPhone()) // Thêm mới
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
    .phone(shop.getPhone()) // Thêm mới
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
- **Cột**: `phone VARCHAR(20)`
- **Nullable**: Có thể NULL
- **Length**: Tối đa 20 ký tự (đủ cho số điện thoại Việt Nam)

### **Dữ liệu mẫu:**
```sql
INSERT INTO Shop (name, address, phone, latitude, longitude, description, rating, status) VALUES 
('Pizza Corner', '123 Đường ABC, Quận 1, TP.HCM', '0123456789', 10.762622, 106.660172, 'Pizza ngon nhất thành phố', 4.5, '1'),
('Burger King', '456 Đường XYZ, Quận 2, TP.HCM', '0987654321', 10.763000, 106.661000, 'Burger thơm ngon', 4.2, '1'),
('Cafe Central', '789 Đường DEF, Quận 3, TP.HCM', '0369258147', 10.764000, 106.662000, 'Cà phê chất lượng cao', 4.8, '1');
```

## 🧪 **Test Cases**

### **Test API với số điện thoại:**
```bash
# Test product detail (có shop info với phone)
curl "http://localhost:8080/products/1"

# Test shop detail (có phone)
curl "http://localhost:8080/shops/1"

# Test search products (có shop info với phone)
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
    "latitude": 10.76262200,
    "longitude": 106.66017200
  }
}
```

## 📁 **Files đã cập nhật**

1. **`Shop.java`** - Thêm field `phone`
2. **`ProductDetailResponse.java`** - Thêm `phone` vào `ShopInfo`
3. **`ShopDetailResponse.java`** - Thêm field `phone`
4. **`ProductService.java`** - Cập nhật tạo `ShopInfo` với `phone`
5. **`ShopService.java`** - Cập nhật tạo `ShopDetailResponse` với `phone`
6. **`create_tables.sql`** - Cập nhật schema và dữ liệu mẫu
7. **`add_shop_phone.sql`** - Script migration cho database hiện tại

## 🚀 **Migration Steps**

### **Cho database mới:**
```bash
# Chạy file create_tables.sql (đã cập nhật)
mysql -u root -p foodshare < create_tables.sql
```

### **Cho database hiện tại:**
```bash
# Chạy file migration
mysql -u root -p foodshare < add_shop_phone.sql
```

## ✅ **Lợi ích cho Frontend**

### **1. Liên hệ trực tiếp:**
- Hiển thị số điện thoại cửa hàng
- Tạo link gọi điện: `tel:0123456789`
- Tăng trải nghiệm người dùng

### **2. Thông tin đầy đủ:**
- Địa chỉ + Số điện thoại
- Dễ dàng liên hệ với cửa hàng
- Tăng độ tin cậy

### **3. UI/UX tốt hơn:**
```jsx
const ShopInfo = ({ shop }) => {
  return (
    <div className="shop-info">
      <h3>{shop.name}</h3>
      <p>📍 {shop.address}</p>
      <p>📞 <a href={`tel:${shop.phone}`}>{shop.phone}</a></p>
      <p>⭐ {shop.rating}/5</p>
    </div>
  );
};
```

## 🎨 **Frontend Usage**

### **Hiển thị số điện thoại:**
```javascript
const formatPhone = (phone) => {
  if (!phone) return 'Chưa cập nhật';
  return phone.replace(/(\d{4})(\d{3})(\d{3})/, '$1 $2 $3');
};

// Sử dụng
const phone = formatPhone(shop.phone);
console.log(`Số điện thoại: ${phone}`); // "Số điện thoại: 0123 456 789"
```

### **Link gọi điện:**
```jsx
const PhoneLink = ({ phone }) => {
  if (!phone) return <span>Chưa cập nhật</span>;
  
  return (
    <a 
      href={`tel:${phone}`}
      className="phone-link"
      style={{ color: 'blue', textDecoration: 'none' }}
    >
      📞 {phone}
    </a>
  );
};
```

## 📝 **Lưu ý**

- **Format số điện thoại**: Hỗ trợ format Việt Nam (10-11 số)
- **Validation**: Có thể thêm validation cho format số điện thoại
- **Nullable**: Cột có thể NULL nếu cửa hàng chưa cập nhật

Bây giờ tất cả API đều trả về số điện thoại cửa hàng! 📞✨
