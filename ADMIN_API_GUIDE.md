# Hướng Dẫn API Admin

## Tổng Quan

**1 AdminController duy nhất** với prefix `/api/admin` quản lý toàn bộ hệ thống:

- **Quản lý Admin**: Tạo admin mới
- **Quản lý Cửa hàng**: Xem danh sách, chi tiết cửa hàng
- **Quản lý Sản phẩm**: Xem danh sách, chi tiết sản phẩm

## Authentication

Tất cả API admin yêu cầu:
- **JWT Token** trong header `Authorization: Bearer <token>`
- **Role ADMIN** trong token

## API Endpoints

### 1. Quản Lý Admin

#### Tạo Admin Mới
```
POST /api/admin/create-admin
Authorization: Bearer <admin_token>
```

**Request Body:**
```json
{
  "name": "New Admin",
  "email": "newadmin@foodshare.com",
  "password": "Admin123456",
  "role": "ADMIN"
}
```

#### Cập Nhật Admin
```
PUT /api/admin/admins/{adminId}
Authorization: Bearer <admin_token>
```

**Request Body:**
```json
{
  "name": "Tên admin mới",
  "email": "admin2@newdomain.com",
  "password": "newpassword123"
}
```

#### Xóa Admin
```
DELETE /api/admin/admins/{adminId}
Authorization: Bearer <admin_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "name": "New Admin",
    "email": "newadmin@foodshare.com",
    "role": "ADMIN",
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresAt": "2024-01-02T10:30:00",
    "message": "Tạo admin thành công"
  }
}
```

### 2. Quản Lý Cửa Hàng

#### Lấy Danh Sách Tất Cả Cửa Hàng
```
GET /api/admin/shops?page=0&size=20
Authorization: Bearer <admin_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Cửa hàng ABC",
        "address": "123 Đường ABC",
        "phone": "0123456789",
        "imageUrl": "https://example.com/shop1.jpg",
        "latitude": 10.123456,
        "longitude": 106.123456,
        "description": "Mô tả cửa hàng",
        "rating": 4.5,
        "status": "ACTIVE",
        "createdAt": "2024-01-01T10:00:00",
        "updatedAt": "2024-01-01T10:00:00",
        "totalProducts": 15
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

#### Lấy Chi Tiết Cửa Hàng
```
GET /api/admin/shops/{shopId}
Authorization: Bearer <admin_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Cửa hàng ABC",
    "address": "123 Đường ABC",
    "phone": "0123456789",
    "imageUrl": "https://example.com/shop1.jpg",
    "latitude": 10.123456,
    "longitude": 106.123456,
    "description": "Mô tả cửa hàng",
    "rating": 4.5,
    "status": "ACTIVE",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00",
    "products": [
      {
        "id": 1,
        "categoryId": 1,
        "name": "Sản phẩm 1",
        "description": "Mô tả sản phẩm",
        "price": 50000,
        "originalPrice": 60000,
        "imageUrl": "https://example.com/product1.jpg",
        "detailImageUrl": "https://example.com/product1-detail.jpg",
        "quantityAvailable": 10,
        "quantityPending": 5,
        "status": "ACTIVE",
        "createdAt": "2024-01-01T10:00:00",
        "updatedAt": "2024-01-01T10:00:00"
      }
    ]
  }
}
```

#### Tạo Cửa Hàng Mới
```
POST /api/admin/shops
Authorization: Bearer <admin_token>
```

**Request Body:**
```json
{
  "name": "Cửa hàng mới",
  "address": "123 Đường ABC",
  "phone": "0123456789",
  "imageUrl": "https://example.com/shop.jpg",
  "latitude": 10.123456,
  "longitude": 106.123456,
  "description": "Mô tả cửa hàng",
  "rating": 4.5,
  "status": "ACTIVE"
}
```

#### Cập Nhật Cửa Hàng
```
PUT /api/admin/shops/{shopId}
Authorization: Bearer <admin_token>
```

**Request Body:**
```json
{
  "name": "Tên cửa hàng mới",
  "address": "Địa chỉ mới",
  "phone": "0987654321",
  "status": "INACTIVE"
}
```

#### Xóa Cửa Hàng
```
DELETE /api/admin/shops/{shopId}
Authorization: Bearer <admin_token>
```

### 3. Quản Lý Sản Phẩm

#### Lấy Danh Sách Tất Cả Sản Phẩm
```
GET /api/admin/products?page=0&size=20
Authorization: Bearer <admin_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "shopId": 1,
        "shopName": "Cửa hàng ABC",
        "categoryId": 1,
        "name": "Sản phẩm 1",
        "description": "Mô tả sản phẩm",
        "price": 50000,
        "originalPrice": 60000,
        "imageUrl": "https://example.com/product1.jpg",
        "detailImageUrl": "https://example.com/product1-detail.jpg",
        "quantityAvailable": 10,
        "quantityPending": 5,
        "status": "ACTIVE",
        "createdAt": "2024-01-01T10:00:00",
        "updatedAt": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

#### Lấy Chi Tiết Sản Phẩm
```
GET /api/admin/products/{productId}
Authorization: Bearer <admin_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "shopId": 1,
    "shopName": "Cửa hàng ABC",
    "shopAddress": "123 Đường ABC",
    "shopPhone": "0123456789",
    "categoryId": 1,
    "name": "Sản phẩm 1",
    "description": "Mô tả sản phẩm",
    "price": 50000,
    "originalPrice": 60000,
    "imageUrl": "https://example.com/product1.jpg",
    "detailImageUrl": "https://example.com/product1-detail.jpg",
    "quantityAvailable": 10,
    "quantityPending": 5,
    "status": "ACTIVE",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

#### Lấy Danh Sách Sản Phẩm Trong Cửa Hàng
```
GET /api/admin/products/shop/{shopId}?page=0&size=20
Authorization: Bearer <admin_token>
```

#### Tạo Sản Phẩm Mới
```
POST /api/admin/products
Authorization: Bearer <admin_token>
```

**Request Body:**
```json
{
  "shopId": 1,
  "categoryId": 1,
  "name": "Sản phẩm mới",
  "description": "Mô tả sản phẩm",
  "price": 50000,
  "originalPrice": 60000,
  "imageUrl": "https://example.com/product.jpg",
  "detailImageUrl": "https://example.com/product-detail.jpg",
  "quantityAvailable": 10,
  "quantityPending": 5,
  "status": "ACTIVE"
}
```

#### Cập Nhật Sản Phẩm
```
PUT /api/admin/products/{productId}
Authorization: Bearer <admin_token>
```

**Request Body:**
```json
{
  "name": "Tên sản phẩm mới",
  "price": 55000,
  "quantityAvailable": 15,
  "status": "INACTIVE"
}
```

#### Xóa Sản Phẩm
```
DELETE /api/admin/products/{productId}
Authorization: Bearer <admin_token>
```

## Ví Dụ Sử Dụng

### 1. Đăng nhập Admin
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@foodshare.com",
    "password": "Admin123456"
  }'
```

### 2. Lấy danh sách cửa hàng
```bash
curl -X GET "http://localhost:8080/api/admin/shops?page=0&size=10" \
  -H "Authorization: Bearer <admin_token>"
```

### 3. Lấy chi tiết cửa hàng
```bash
curl -X GET "http://localhost:8080/api/admin/shops/1" \
  -H "Authorization: Bearer <admin_token>"
```

### 4. Lấy danh sách sản phẩm
```bash
curl -X GET "http://localhost:8080/api/admin/products?page=0&size=10" \
  -H "Authorization: Bearer <admin_token>"
```

### 5. Tạo admin mới
```bash
curl -X POST "http://localhost:8080/api/admin/create-admin" \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Admin",
    "email": "newadmin@foodshare.com",
    "password": "Admin123456",
    "role": "ADMIN"
  }'
```

## Tổng Kết API Endpoints

| Method | Endpoint | Mô Tả |
|--------|----------|-------|
| POST | `/api/admin/create-admin` | Tạo admin mới |
| PUT | `/api/admin/admins/{adminId}` | Cập nhật admin |
| DELETE | `/api/admin/admins/{adminId}` | Xóa admin |
| GET | `/api/admin/shops` | Lấy danh sách tất cả cửa hàng |
| GET | `/api/admin/shops/{shopId}` | Lấy chi tiết cửa hàng |
| POST | `/api/admin/shops` | Tạo cửa hàng mới |
| PUT | `/api/admin/shops/{shopId}` | Cập nhật cửa hàng |
| DELETE | `/api/admin/shops/{shopId}` | Xóa cửa hàng |
| GET | `/api/admin/products` | Lấy danh sách tất cả sản phẩm |
| GET | `/api/admin/products/{productId}` | Lấy chi tiết sản phẩm |
| GET | `/api/admin/products/shop/{shopId}` | Lấy sản phẩm trong cửa hàng |
| POST | `/api/admin/products` | Tạo sản phẩm mới |
| PUT | `/api/admin/products/{productId}` | Cập nhật sản phẩm |
| DELETE | `/api/admin/products/{productId}` | Xóa sản phẩm |

## Lưu Ý

- **Chỉ Admin**: Tất cả API đều yêu cầu role ADMIN
- **JWT Token**: Cần token hợp lệ trong header Authorization
- **Pagination**: Hỗ trợ phân trang với `page` và `size`
- **Error Handling**: Trả về mã lỗi chi tiết khi có lỗi
- **Logging**: Ghi log đầy đủ cho việc debug và monitoring
- **CRUD Complete**: Admin có thể quản lý toàn bộ hệ thống (Create, Read, Update, Delete)
