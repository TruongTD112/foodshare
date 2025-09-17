# Hướng Dẫn Test API với Postman

## 🚀 **Cách import Collection**

### **Bước 1: Mở Postman**
- Mở ứng dụng Postman
- Click **Import** ở góc trái trên

### **Bước 2: Import file**
- Chọn **File** tab
- Browse và chọn file `FoodShare_API_Collection.postman_collection.json`
- Click **Import**

### **Bước 3: Setup Environment**
- Click **Environments** ở sidebar trái
- Click **Create Environment**
- Tên: `FoodShare Local`
- Thêm variable: `base_url` = `http://localhost:8080`
- Click **Save**

## 📋 **Danh sách API cần test**

### **1. Tìm kiếm sản phẩm chung** - `GET /products`
```
URL: {{base_url}}/products?q=pizza&lat=10.762622&lon=106.660172&maxDistanceKm=5.0&minPrice=10000&maxPrice=200000&priceSort=asc&page=0&size=10
```

**Tham số:**
- `q`: Tên sản phẩm (tùy chọn)
- `lat`, `lon`: Tọa độ (tùy chọn)
- `maxDistanceKm`: Khoảng cách tối đa (tùy chọn)
- `minPrice`, `maxPrice`: Khoảng giá (tùy chọn)
- `priceSort`: Sắp xếp giá (asc/desc)
- `page`, `size`: Phân trang

### **2. Tìm kiếm sản phẩm gần đây** - `GET /products/nearby`
```
URL: {{base_url}}/products/nearby?lat=10.762622&lon=106.660172&page=0&size=10
```

**Tham số bắt buộc:**
- `lat`, `lon`: Tọa độ người dùng

### **3. Sản phẩm giảm giá nhiều nhất** - `GET /products/top-discounts`
```
URL: {{base_url}}/products/top-discounts?lat=10.762622&lon=106.660172&page=0&size=10
```

**Tham số:**
- `lat`, `lon`: Tọa độ người dùng (tùy chọn)
- `page`, `size`: Phân trang

### **4. Sản phẩm bán chạy nhất** - `GET /products/popular`
```
URL: {{base_url}}/products/popular?lat=10.762622&lon=106.660172&page=0&size=10
```

**Tham số:**
- `lat`, `lon`: Tọa độ người dùng (tùy chọn)
- `page`, `size`: Phân trang

### **5. Chi tiết sản phẩm** - `GET /products/{id}`
```
URL: {{base_url}}/products/1
```

## 🧪 **Test Cases**

### **Test Case 1: API cơ bản**
1. Chạy `GET /products/popular` - Kiểm tra có dữ liệu
2. Chạy `GET /products/top-discounts` - Kiểm tra sản phẩm giảm giá
3. Chạy `GET /products/1` - Kiểm tra chi tiết sản phẩm

### **Test Case 2: Tìm kiếm với tọa độ**
1. Chạy `GET /products/nearby` với tọa độ hợp lệ
2. Kiểm tra response có `distanceKm`
3. Kiểm tra sắp xếp theo khoảng cách

### **Test Case 3: Tìm kiếm với bộ lọc**
1. Chạy `GET /products?q=pizza` - Tìm theo tên
2. Chạy `GET /products?minPrice=50000&maxPrice=150000` - Lọc theo giá
3. Chạy `GET /products?priceSort=desc` - Sắp xếp giá

### **Test Case 4: Phân trang**
1. Chạy `GET /products?page=0&size=5` - Trang đầu
2. Chạy `GET /products?page=1&size=5` - Trang 2
3. Kiểm tra `hasNext`, `hasPrevious`

### **Test Case 5: Error handling**
1. Chạy `GET /products/nearby` không có tọa độ - Phải báo lỗi 400
2. Chạy `GET /products/999` - Sản phẩm không tồn tại - Phải báo lỗi 404
3. Chạy `GET /products?page=-1` - Phân trang không hợp lệ - Phải báo lỗi 400

## 📊 **Kiểm tra Response**

### **Response thành công:**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "content": [...],
    "page": 0,
    "size": 10,
    "totalElements": 5,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

### **Response lỗi:**
```json
{
  "success": false,
  "code": "400",
  "message": "Latitude and longitude are required for nearby search"
}
```

## 🔧 **Setup Database trước khi test**

### **Chạy SQL setup:**
```bash
# Chạy file tạo bảng và dữ liệu mẫu
mysql -u root -p foodshare < create_tables.sql
```

### **Kiểm tra dữ liệu:**
```sql
-- Kiểm tra có sản phẩm
SELECT COUNT(*) FROM Product;

-- Kiểm tra có stats
SELECT COUNT(*) FROM ProductSalesStats;

-- Kiểm tra có order
SELECT COUNT(*) FROM `Order`;
```

## 🚀 **Chạy ứng dụng**

### **Start Spring Boot app:**
```bash
# Trong thư mục project
mvn spring-boot:run

# Hoặc
java -jar target/foodshare-0.0.1-SNAPSHOT.jar
```

### **Kiểm tra app đang chạy:**
```bash
curl http://localhost:8080/health
```

## 📝 **Checklist Test**

- [ ] App đang chạy trên port 8080
- [ ] Database đã setup với dữ liệu mẫu
- [ ] Postman collection đã import
- [ ] Environment variable `base_url` đã set
- [ ] Test tất cả 5 API endpoints
- [ ] Test các trường hợp lỗi
- [ ] Kiểm tra response format đúng
- [ ] Kiểm tra phân trang hoạt động
- [ ] Kiểm tra sắp xếp theo khoảng cách
- [ ] Kiểm tra sắp xếp theo giá

## 🐛 **Troubleshooting**

### **Lỗi Connection refused:**
- Kiểm tra app có đang chạy không
- Kiểm tra port 8080 có bị chiếm không

### **Lỗi 404 Not Found:**
- Kiểm tra URL có đúng không
- Kiểm tra context path

### **Lỗi 500 Internal Server Error:**
- Kiểm tra database connection
- Kiểm tra log của ứng dụng

### **Không có dữ liệu:**
- Chạy lại file `create_tables.sql`
- Kiểm tra database có dữ liệu mẫu không
