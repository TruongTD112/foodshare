# Hướng Dẫn BackOffice Authentication APIs

## Tổng Quan

Hệ thống BackOffice Authentication cung cấp các API để đăng ký và đăng nhập cho Admin và Seller.

**Lưu ý quan trọng:**
- Chỉ có 1 admin đầu tiên duy nhất có thể đăng ký
- Sau khi có admin, chỉ admin mới có thể tạo user mới
- Không cho phép đăng ký bừa bãi

## API Endpoints

### 1. Đăng ký admin đầu tiên
```
POST /api/auth/backoffice/register
```

**Request Body:**
```json
{
  "name": "Admin User",
  "email": "admin@foodshare.com",
  "password": "Admin123456"
}
```

**Response:**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Admin User",
    "email": "admin@foodshare.com",
    "role": "ADMIN",
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresAt": "2024-01-02T10:30:00",
    "message": "Đăng ký admin đầu tiên thành công"
  }
}
```

### 2. Đăng nhập
```
POST /api/auth/backoffice/login
```

**Request Body:**
```json
{
  "email": "admin@foodshare.com",
  "password": "Admin123456"
}
```

**Response:**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Admin User",
    "email": "admin@foodshare.com",
    "role": "ADMIN",
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresAt": "2024-01-02T10:30:00",
    "message": "Đăng nhập thành công"
  }
}
```

### 3. Lấy thông tin user hiện tại
```
GET /api/auth/backoffice/me
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Admin User",
    "email": "admin@foodshare.com",
    "role": "ADMIN",
    "accessToken": null,
    "tokenType": null,
    "expiresAt": null,
    "message": "Lấy thông tin thành công"
  }
}
```

### 4. Admin tạo user mới
```
POST /api/auth/backoffice/create-user
Authorization: Bearer <admin_token>
```

**Request Body:**
```json
{
  "name": "Seller User",
  "email": "seller@foodshare.com",
  "password": "Seller123456",
  "role": "SELLER"
}
```

**Response:**
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
    "message": "Tạo user thành công"
  }
}
```

### 5. Đăng xuất
```
POST /api/auth/backoffice/logout
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": "Đăng xuất thành công"
}
```

## Validation Rules

### Đăng ký (Register)
- **name**: Bắt buộc, tối đa 255 ký tự
- **email**: Bắt buộc, định dạng email hợp lệ, tối đa 255 ký tự
- **password**: Bắt buộc, 6-50 ký tự, phải chứa ít nhất 1 chữ thường, 1 chữ hoa và 1 số
- **role**: Bắt buộc, phải là "ADMIN" hoặc "SELLER"

### Đăng nhập (Login)
- **email**: Bắt buộc, định dạng email hợp lệ, tối đa 255 ký tự
- **password**: Bắt buộc, 6-50 ký tự

## Error Codes

| Code | Message | Mô tả |
|------|---------|-------|
| 400 | Invalid request | Dữ liệu không hợp lệ |
| 401 | Unauthorized | Chưa đăng nhập hoặc token không hợp lệ |
| 404 | User not found | Không tìm thấy user |
| 409 | Email already exists | Email đã tồn tại |
| 500 | Internal server error | Lỗi server |

## Cài Đặt

### 1. Cập nhật database
```sql
-- Thêm cột role vào bảng BackOffice_User
ALTER TABLE BackOffice_User 
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'SELLER';

-- Tạo index cho cột role
CREATE INDEX idx_backoffice_user_role ON BackOffice_User(role);
```

### 2. Cấu hình JWT
Thêm vào `application.properties`:
```properties
# JWT Configuration
jwt.secret=mySecretKey
jwt.expiration=86400
```

### 3. Thêm PasswordEncoder Bean
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

## Test APIs

### 1. Chạy test script
```bash
chmod +x test_backoffice_auth.sh
./test_backoffice_auth.sh
```

### 2. Import Postman Collection
File: `BackOffice_Auth_API_Collection.postman_collection.json`

## Ví Dụ Sử Dụng

### 1. Đăng ký Admin
```bash
curl -X POST "http://localhost:8080/api/auth/backoffice/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin User",
    "email": "admin@foodshare.com",
    "password": "Admin123456",
    "role": "ADMIN"
  }'
```

### 2. Đăng nhập
```bash
curl -X POST "http://localhost:8080/api/auth/backoffice/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@foodshare.com",
    "password": "Admin123456"
  }'
```

### 3. Lấy thông tin user hiện tại
```bash
curl -X GET "http://localhost:8080/api/auth/backoffice/me" \
  -H "Authorization: Bearer <access_token>"
```

### 4. Đăng xuất
```bash
curl -X POST "http://localhost:8080/api/auth/backoffice/logout" \
  -H "Authorization: Bearer <access_token>"
```

## Bảo Mật

### 1. Password Hashing
- Sử dụng BCrypt để hash mật khẩu
- Salt tự động được tạo cho mỗi password

### 2. JWT Token
- Token chứa thông tin user ID và role
- Token hết hạn sau 24 giờ
- Sử dụng HMAC SHA-512 để ký token

### 3. Validation
- Validate tất cả input
- Kiểm tra email format
- Kiểm tra password strength
- Kiểm tra role hợp lệ

## Lưu Ý Quan Trọng

1. **Password Policy**: Mật khẩu phải chứa ít nhất 1 chữ thường, 1 chữ hoa và 1 số
2. **Token Expiry**: Token hết hạn sau 24 giờ, cần đăng nhập lại
3. **Role Validation**: Chỉ chấp nhận role "ADMIN" hoặc "SELLER"
4. **Email Uniqueness**: Email phải duy nhất trong hệ thống
5. **Error Handling**: Tất cả lỗi đều được xử lý và trả về thông báo rõ ràng

## Troubleshooting

### 1. Lỗi "Email already exists"
- Kiểm tra email đã được sử dụng chưa
- Sử dụng email khác để đăng ký

### 2. Lỗi "Invalid credentials"
- Kiểm tra email và mật khẩu có đúng không
- Đảm bảo user đã được đăng ký

### 3. Lỗi "Unauthorized"
- Kiểm tra token có hợp lệ không
- Kiểm tra token có hết hạn không
- Đăng nhập lại để lấy token mới

### 4. Lỗi validation
- Kiểm tra tất cả trường bắt buộc
- Kiểm tra format email
- Kiểm tra password strength
- Kiểm tra role hợp lệ
