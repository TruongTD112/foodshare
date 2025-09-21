# Hướng Dẫn API BackOffice Authentication

## Tổng Quan

Hệ thống BackOffice Authentication với **CHỈ 1 API đăng nhập duy nhất** cho cả Admin và Seller:

- **1 API đăng nhập**: `POST /api/auth/login` - trả về role khác nhau
- **1 API đăng ký**: `POST /api/auth/register` - chỉ dành cho Seller
- **Admin**: Tạo bằng script SQL, chỉ admin mới có thể tạo admin khác

## API Endpoints

### 1. Đăng nhập thống nhất (Admin/Seller)
```
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "user@foodshare.com",
  "password": "Password123"
}
```

**Response (Admin):**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Super Admin",
    "email": "admin@foodshare.com",
    "role": "ADMIN",
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresAt": "2024-01-02T10:30:00",
    "message": "Đăng nhập thành công"
  }
}
```

**Response (Seller):**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 2,
    "name": "Seller User",
    "email": "seller@foodshare.com",
    "role": "SELLER",
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresAt": "2024-01-02T10:30:00",
    "message": "Đăng nhập thành công"
  }
}
```

### 2. Đăng ký Seller
```
POST /api/auth/register
```

**Request Body:**
```json
{
  "name": "Seller User",
  "email": "seller@foodshare.com",
  "password": "Seller123456"
}
```

### 3. Lấy thông tin user hiện tại
```
GET /api/auth/me
Authorization: Bearer <token>
```

### 4. Đăng xuất
```
POST /api/auth/logout
Authorization: Bearer <token>
```

### 5. Admin tạo admin mới
```
POST /api/auth/admin/create-admin
Authorization: Bearer <admin_token>
```

**Request Body:**
```json
{
  "name": "Another Admin",
  "email": "admin2@foodshare.com",
  "password": "Admin123456",
  "role": "ADMIN"
}
```

## Ví Dụ Sử Dụng

### 1. Tạo admin đầu tiên
```bash
mysql -u root -p < create_first_admin.sql
```

### 2. Đăng nhập (cả Admin và Seller)
```bash
# Admin đăng nhập
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@foodshare.com",
    "password": "Admin123456"
  }'

# Seller đăng nhập
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "seller@foodshare.com",
    "password": "Seller123456"
  }'
```

### 3. Đăng ký Seller mới
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Seller",
    "email": "newseller@foodshare.com",
    "password": "Seller123456"
  }'
```

### 4. Lấy thông tin user
```bash
curl -X GET "http://localhost:8080/api/auth/me" \
  -H "Authorization: Bearer <token>"
```

### 5. Admin tạo admin mới
```bash
curl -X POST "http://localhost:8080/api/auth/admin/create-admin" \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Admin",
    "email": "newadmin@foodshare.com",
    "password": "Admin123456",
    "role": "ADMIN"
  }'
```

## Lợi Ích

1. **Đơn giản**: Chỉ 1 API đăng nhập duy nhất
2. **Linh hoạt**: Tự động trả về role (ADMIN/SELLER) trong response
3. **Bảo mật**: JWT token với thông tin role
4. **Dễ sử dụng**: Frontend chỉ cần gọi 1 endpoint
5. **Thống nhất**: Không còn API trùng lặp

## Cấu Trúc File

- **Controller**: `BackOfficeAuthController.java`
- **Service**: `BackOfficeAuthService.java`
- **DTO**: `UnifiedLoginRequest.java`, `SellerRegisterRequest.java`
- **Response**: `BackOfficeAuthResponse.java`

## Lưu Ý

- **Admin đầu tiên**: Tạo bằng script SQL
- **Seller**: Có thể đăng ký tự do qua `/api/auth/register`
- **Admin mới**: Chỉ admin mới có thể tạo qua `/api/auth/admin/create-admin`
- **Role**: Được trả về trong response đăng nhập
- **Token**: JWT token chứa thông tin user ID và role
