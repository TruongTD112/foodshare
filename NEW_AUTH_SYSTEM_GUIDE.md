# Hướng Dẫn Hệ Thống Authentication Mới

## Tổng Quan

Hệ thống authentication mới được thiết kế với 2 loại người dùng:

1. **Seller**: Đăng ký và đăng nhập qua email
2. **Admin**: Chỉ admin mới có thể tạo admin khác, admin đầu tiên tạo bằng script

## Cài Đặt

### 1. Tạo admin đầu tiên bằng script
```bash
# Chạy script SQL để tạo admin đầu tiên
mysql -u root -p < create_first_admin.sql
```

**Thông tin admin đầu tiên:**
- Email: `admin@foodshare.com`
- Password: `Admin123456`
- Role: `ADMIN`

### 2. Test hệ thống
```bash
# Chạy test script
chmod +x test_new_auth_system.sh
./test_new_auth_system.sh
```

## API Endpoints

### Seller APIs

#### 1. Đăng ký Seller
```
POST /api/seller/auth/register
```

**Request Body:**
```json
{
  "name": "Seller User",
  "email": "seller@foodshare.com",
  "password": "Seller123456"
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
    "name": "Seller User",
    "email": "seller@foodshare.com",
    "role": "SELLER",
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresAt": "2024-01-02T10:30:00",
    "message": "Đăng ký seller thành công"
  }
}
```

#### 2. Đăng nhập Seller
```
POST /api/seller/auth/login
```

**Request Body:**
```json
{
  "email": "seller@foodshare.com",
  "password": "Seller123456"
}
```

#### 3. Lấy thông tin Seller
```
GET /api/seller/auth/me
Authorization: Bearer <seller_token>
```

#### 4. Đăng xuất Seller
```
POST /api/seller/auth/logout
Authorization: Bearer <seller_token>
```

### Admin APIs

#### 1. Đăng nhập Admin
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
    "name": "Super Admin",
    "email": "admin@foodshare.com",
    "role": "ADMIN",
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresAt": "2024-01-02T10:30:00",
    "message": "Đăng nhập admin thành công"
  }
}
```

#### 2. Lấy thông tin Admin
```
GET /api/auth/backoffice/me
Authorization: Bearer <admin_token>
```

#### 3. Tạo Admin mới
```
POST /api/auth/backoffice/create-admin
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

#### 4. Đăng xuất Admin
```
POST /api/auth/backoffice/logout
Authorization: Bearer <admin_token>
```

## Bảo Mật

### 1. Phân quyền rõ ràng
- **Seller**: Chỉ có thể đăng ký và đăng nhập
- **Admin**: Chỉ admin mới có thể tạo admin khác
- **Admin đầu tiên**: Tạo bằng script SQL

### 2. Validation
- **Password Policy**: Phải chứa chữ hoa, chữ thường và số
- **Email Format**: Kiểm tra định dạng email hợp lệ
- **Role Validation**: Kiểm tra role hợp lệ

### 3. JWT Token
- **Expiry**: Token hết hạn sau 24 giờ
- **Signing**: HMAC SHA-512
- **Role-based**: Token chứa thông tin role

## Lưu Ý Quan Trọng

### 1. Admin đầu tiên
- **Tạo bằng script**: Không qua API
- **Thông tin cố định**: admin@foodshare.com / Admin123456
- **Quyền cao nhất**: Có thể tạo admin khác

### 2. Seller
- **Đăng ký tự do**: Không cần admin tạo
- **Role mặc định**: SELLER
- **Chỉ truy cập**: Seller APIs

### 3. Admin mới
- **Chỉ admin tạo**: Không thể đăng ký tự do
- **Role bắt buộc**: ADMIN
- **Có thể tạo**: Admin khác

## Ví Dụ Sử Dụng

### 1. Tạo admin đầu tiên
```bash
# Chạy script SQL
mysql -u root -p < create_first_admin.sql
```

### 2. Seller đăng ký
```bash
curl -X POST "http://localhost:8080/api/seller/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Seller User",
    "email": "seller@foodshare.com",
    "password": "Seller123456"
  }'
```

### 3. Seller đăng nhập
```bash
curl -X POST "http://localhost:8080/api/seller/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "seller@foodshare.com",
    "password": "Seller123456"
  }'
```

### 4. Admin đăng nhập
```bash
curl -X POST "http://localhost:8080/api/auth/backoffice/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@foodshare.com",
    "password": "Admin123456"
  }'
```

### 5. Admin tạo admin mới
```bash
curl -X POST "http://localhost:8080/api/auth/backoffice/create-admin" \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Another Admin",
    "email": "admin2@foodshare.com",
    "password": "Admin123456",
    "role": "ADMIN"
  }'
```

## Troubleshooting

### 1. Lỗi "Tài khoản này không phải seller"
- **Nguyên nhân**: Đăng nhập bằng admin vào seller API
- **Giải pháp**: Sử dụng đúng API cho từng role

### 2. Lỗi "Tài khoản này không phải admin"
- **Nguyên nhân**: Đăng nhập bằng seller vào admin API
- **Giải pháp**: Sử dụng đúng API cho từng role

### 3. Lỗi "Chỉ admin mới có thể tạo admin"
- **Nguyên nhân**: Seller cố gắng tạo admin
- **Giải pháp**: Chỉ admin mới có thể tạo admin

### 4. Lỗi "Email đã tồn tại"
- **Nguyên nhân**: Email đã được sử dụng
- **Giải pháp**: Sử dụng email khác

## Lợi Ích

1. **Bảo mật cao**: Phân quyền rõ ràng
2. **Dễ quản lý**: Admin kiểm soát toàn bộ
3. **Linh hoạt**: Seller có thể đăng ký tự do
4. **Scalable**: Dễ dàng mở rộng
