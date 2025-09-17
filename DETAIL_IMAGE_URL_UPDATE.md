# Cập Nhật Thêm Detail Image URL

## 🎯 **Mục tiêu**
Thêm cột `detail_image_url` vào bảng `Product` để lưu ảnh chi tiết sản phẩm và trả về cho tất cả API.

## 📋 **Thay đổi thực hiện**

### **1. Database Schema**
```sql
-- Thêm cột detail_image_url vào bảng Product
ALTER TABLE Product ADD COLUMN detail_image_url TEXT NULL;
```

### **2. Entity Product.java**
```java
@Column(name = "detail_image_url", columnDefinition = "TEXT")
private String detailImageUrl;
```

### **3. DTO Updates**

#### **ProductSearchItem.java**
```java
String detailImageUrl; // Ảnh chi tiết sản phẩm
```

#### **ProductDetailResponse.java**
```java
String detailImageUrl; // Ảnh chi tiết sản phẩm
```

### **4. Service Updates**
- **Tất cả API** đều trả về `detailImageUrl`
- **ProductService** cập nhật tất cả method tạo response

## 📊 **Response Format**

### **API Search Results:**
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
        "detailImageUrl": "https://example.com/images/pizza-margherita-detail1.jpg,https://example.com/images/pizza-margherita-detail2.jpg,https://example.com/images/pizza-margherita-detail3.jpg",
        "shopName": "Pizza Corner",
        "shopLatitude": 10.76262200,
        "shopLongitude": 106.66017200,
        "distanceKm": 2.5,
        "totalOrders": 25
      }
    ]
  }
}
```

### **API Product Detail:**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Pizza Margherita",
    "description": "Pizza cổ điển với phô mai mozzarella và cà chua",
    "price": 120000,
    "originalPrice": 150000,
    "imageUrl": "https://example.com/images/pizza-margherita.jpg",
    "detailImageUrl": "https://example.com/images/pizza-margherita-detail1.jpg,https://example.com/images/pizza-margherita-detail2.jpg,https://example.com/images/pizza-margherita-detail3.jpg",
    "quantityAvailable": 50,
    "quantityPending": 0,
    "status": "1",
    "shop": {
      "id": 1,
      "name": "Pizza Corner",
      "address": "123 Đường ABC, Quận 1, TP.HCM",
      "latitude": 10.76262200,
      "longitude": 106.66017200
    }
  }
}
```

## 🔧 **Cách sử dụng Detail Image URL**

### **Format dữ liệu:**
- **Ảnh đơn**: `https://example.com/image.jpg`
- **Nhiều ảnh**: `https://example.com/image1.jpg,https://example.com/image2.jpg,https://example.com/image3.jpg`

### **Frontend parsing:**
```javascript
// Parse detail images từ string
const detailImages = product.detailImageUrl 
  ? product.detailImageUrl.split(',') 
  : [];

// Hiển thị ảnh chi tiết
detailImages.forEach((imageUrl, index) => {
  console.log(`Detail image ${index + 1}: ${imageUrl}`);
});
```

### **React component example:**
```jsx
const ProductDetail = ({ product }) => {
  const detailImages = product.detailImageUrl 
    ? product.detailImageUrl.split(',') 
    : [];

  return (
    <div>
      <img src={product.imageUrl} alt={product.name} />
      
      {/* Detail images gallery */}
      <div className="detail-images">
        {detailImages.map((imageUrl, index) => (
          <img 
            key={index} 
            src={imageUrl} 
            alt={`${product.name} detail ${index + 1}`}
            className="detail-image"
          />
        ))}
      </div>
    </div>
  );
};
```

## 📁 **Files đã cập nhật**

1. **`Product.java`** - Thêm field `detailImageUrl`
2. **`ProductSearchItem.java`** - Thêm field `detailImageUrl`
3. **`ProductDetailResponse.java`** - Thêm field `detailImageUrl`
4. **`ProductService.java`** - Cập nhật tất cả method trả về `detailImageUrl`
5. **`create_tables.sql`** - Cập nhật schema và dữ liệu mẫu
6. **`add_detail_image_url.sql`** - Script migration cho database hiện tại

## 🚀 **Migration Steps**

### **Cho database mới:**
```bash
# Chạy file create_tables.sql (đã cập nhật)
mysql -u root -p foodshare < create_tables.sql
```

### **Cho database hiện tại:**
```bash
# Chạy file migration
mysql -u root -p foodshare < add_detail_image_url.sql
```

## 🧪 **Test Cases**

### **Test tất cả API:**
```bash
# Test search products
curl "http://localhost:8080/products?page=0&size=5"

# Test nearby products
curl "http://localhost:8080/products/nearby?lat=10.762622&lon=106.660172&page=0&size=5"

# Test popular products
curl "http://localhost:8080/products/popular?page=0&size=5"

# Test top discounts
curl "http://localhost:8080/products/top-discounts?page=0&size=5"

# Test product detail
curl "http://localhost:8080/products/1"
```

### **Kiểm tra response:**
```json
{
  "imageUrl": "https://example.com/images/pizza-margherita.jpg",
  "detailImageUrl": "https://example.com/images/pizza-margherita-detail1.jpg,https://example.com/images/pizza-margherita-detail2.jpg,https://example.com/images/pizza-margherita-detail3.jpg"
}
```

## ✅ **Kết quả**

- **Tất cả API** đều trả về `detailImageUrl`
- **Format linh hoạt**: Hỗ trợ 1 hoặc nhiều ảnh chi tiết
- **Tương thích ngược**: Không ảnh hưởng API hiện tại
- **Dữ liệu mẫu**: Đã có sẵn ảnh chi tiết để test

## 🎨 **Lợi ích cho Frontend**

### **1. Gallery ảnh chi tiết:**
- Hiển thị nhiều góc nhìn của sản phẩm
- Tăng trải nghiệm người dùng
- Tăng tỷ lệ chuyển đổi

### **2. Responsive design:**
- Ảnh chính cho thumbnail
- Ảnh chi tiết cho gallery
- Tối ưu cho mobile và desktop

### **3. SEO và Performance:**
- Alt text cho từng ảnh
- Lazy loading cho ảnh chi tiết
- Optimize kích thước ảnh

Bây giờ tất cả API đều trả về ảnh chi tiết sản phẩm! 🖼️✨
