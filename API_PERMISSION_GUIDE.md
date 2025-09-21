# Hướng Dẫn Phân Quyền API

## Tổng Quan

Hệ thống được phân quyền thành 3 loại người dùng:

- **ADMIN**: Quản lý toàn bộ hệ thống
- **SELLER**: Quản lý sản phẩm và cửa hàng của mình
- **CUSTOMER**: Chỉ xem và đặt hàng

## Cấu Trúc Database

### Bảng BackOffice_User
```sql
CREATE TABLE BackOffice_User (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'SELLER') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Bảng Shop_Member
```sql
CREATE TABLE Shop_Member (
    id INT PRIMARY KEY AUTO_INCREMENT,
    shop_id INT NOT NULL,
    backoffice_user_id INT NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shop_id) REFERENCES Shop(id),
    FOREIGN KEY (backoffice_user_id) REFERENCES BackOffice_User(id)
);
```

## API Endpoints

### 1. Admin APIs (`/api/admin/*`)

**Chỉ dành cho ADMIN**

#### Shop Management
- `POST /api/admin/shops` - Tạo cửa hàng
- `GET /api/admin/shops` - Lấy tất cả cửa hàng
- `GET /api/admin/shops/{shopId}` - Lấy thông tin cửa hàng
- `PUT /api/admin/shops/{shopId}` - Cập nhật cửa hàng
- `DELETE /api/admin/shops/{shopId}` - Xóa cửa hàng
- `GET /api/admin/shops/status/{status}` - Lấy cửa hàng theo trạng thái

#### Product Management
- `POST /api/admin/products` - Tạo sản phẩm
- `GET /api/admin/products` - Lấy tất cả sản phẩm
- `GET /api/admin/products/{productId}` - Lấy thông tin sản phẩm
- `PUT /api/admin/products/{productId}` - Cập nhật sản phẩm
- `DELETE /api/admin/products/{productId}` - Xóa sản phẩm
- `GET /api/admin/products/shop/{shopId}` - Lấy sản phẩm theo shop
- `GET /api/admin/products/shop/{shopId}/status/{status}` - Lấy sản phẩm theo shop và trạng thái

### 2. Seller APIs (`/api/seller/*`)

**Chỉ dành cho SELLER - chỉ quản lý sản phẩm và cửa hàng của mình**

#### Shop Management
- `POST /api/seller/shops` - Tạo cửa hàng mới
- `GET /api/seller/shops` - Lấy danh sách cửa hàng của seller
- `GET /api/seller/shops/{shopId}` - Lấy thông tin cửa hàng của seller
- `PUT /api/seller/shops/{shopId}` - Cập nhật cửa hàng của seller

#### Product Management
- `POST /api/seller/products` - Tạo sản phẩm cho shop của seller
- `GET /api/seller/products/{productId}` - Lấy thông tin sản phẩm của seller
- `PUT /api/seller/products/{productId}` - Cập nhật sản phẩm của seller
- `DELETE /api/seller/products/{productId}` - Xóa sản phẩm của seller
- `GET /api/seller/shops/{shopId}/products` - Lấy sản phẩm theo shop của seller
- `GET /api/seller/shops/{shopId}/products/status/{status}` - Lấy sản phẩm theo shop và trạng thái

### 3. Customer APIs (`/api/*`)

**Dành cho CUSTOMER - chỉ xem và đặt hàng**

#### Product Search
- `GET /api/products/search` - Tìm kiếm sản phẩm
- `GET /api/products/top-discounts` - Sản phẩm giảm giá nhiều nhất
- `GET /api/products/popular` - Sản phẩm bán chạy nhất
- `GET /api/products/nearby` - Sản phẩm gần đây
- `GET /api/products/{id}` - Chi tiết sản phẩm

#### Shop Search
- `GET /api/shops/search` - Tìm kiếm cửa hàng
- `GET /api/shops/{id}` - Chi tiết cửa hàng

#### Order Management
- `POST /api/orders` - Tạo đơn hàng
- `GET /api/orders` - Lấy danh sách đơn hàng
- `DELETE /api/orders/{id}` - Hủy đơn hàng
- `PUT /api/orders/{id}/status` - Cập nhật trạng thái đơn hàng

#### User Management
- `PUT /api/users/{userId}` - Cập nhật thông tin người dùng
- `GET /api/users/{userId}` - Lấy thông tin người dùng

## Authentication & Authorization

### 1. Authentication
- Sử dụng JWT token
- Token được gửi trong header `Authorization: Bearer <token>`
- Token chứa thông tin user ID và role

### 2. Authorization
- **ADMIN**: Có thể truy cập tất cả APIs
- **SELLER**: Chỉ có thể truy cập Seller APIs và Customer APIs
- **CUSTOMER**: Chỉ có thể truy cập Customer APIs

### 3. Permission Check
- Kiểm tra role từ JWT token
- Kiểm tra quyền truy cập resource (ví dụ: seller chỉ có thể quản lý shop của mình)
- Sử dụng `ShopMember` table để kiểm tra seller có quyền truy cập shop không

## Cài Đặt Database

### 1. Thêm cột role vào BackOffice_User
```sql
ALTER TABLE BackOffice_User 
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'SELLER';
```

### 2. Tạo dữ liệu mẫu
```sql
-- Tạo admin user
INSERT INTO BackOffice_User (name, email, password_hash, role) 
VALUES ('Admin User', 'admin@foodshare.com', '$2a$10$example_hash', 'ADMIN');

-- Tạo seller user
INSERT INTO BackOffice_User (name, email, password_hash, role) 
VALUES ('Seller User', 'seller@foodshare.com', '$2a$10$example_hash', 'SELLER');

-- Liên kết seller với shop
INSERT INTO Shop_Member (shop_id, backoffice_user_id, role) 
VALUES (1, (SELECT id FROM BackOffice_User WHERE email = 'seller@foodshare.com'), 'OWNER');
```

## Test APIs

### 1. Test Seller APIs
```bash
chmod +x test_seller_apis.sh
./test_seller_apis.sh
```

### 2. Test Admin APIs
```bash
chmod +x test_shop_management.sh
./test_shop_management.sh

chmod +x test_product_management.sh
./test_product_management.sh
```

### 3. Test Customer APIs
```bash
chmod +x test_product_search.sh
./test_product_search.sh
```

## Postman Collections

1. **Seller Management API Collection** - `Seller_Management_API_Collection.postman_collection.json`
2. **Shop Management API Collection** - `Shop_Management_API_Collection.postman_collection.json`
3. **Product Management API Collection** - `Product_Management_API_Collection.postman_collection.json`
4. **Product Search API Collection** - `Product_Search_API_Collection.postman_collection.json`

## Lưu Ý Quan Trọng

1. **Security**: Tất cả APIs cần authentication
2. **Permission**: Kiểm tra quyền truy cập resource
3. **Validation**: Validate dữ liệu đầu vào
4. **Logging**: Log tất cả hoạt động quan trọng
5. **Error Handling**: Xử lý lỗi một cách nhất quán

## Ví Dụ Sử Dụng

### 1. Seller tạo cửa hàng
```bash
curl -X POST "http://localhost:8080/api/seller/shops" \
  -H "Authorization: Bearer <seller_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Pizza Shop",
    "address": "123 Main St",
    "phone": "0901234567",
    "latitude": 10.762622,
    "longitude": 106.660172,
    "status": "active"
  }'
```

### 2. Seller tạo sản phẩm
```bash
curl -X POST "http://localhost:8080/api/seller/products" \
  -H "Authorization: Bearer <seller_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "name": "Pizza Margherita",
    "price": 150000.00,
    "status": "available"
  }'
```

### 3. Admin quản lý toàn bộ
```bash
curl -X GET "http://localhost:8080/api/admin/shops" \
  -H "Authorization: Bearer <admin_token>"
```
