# Hướng Dẫn Test Authentication Cho API Admin

## Tổng Quan

API `/api/admin/**` đã được bảo vệ với 2 lớp security:

1. **EndpointAuthFilter**: Kiểm tra JWT token và role
2. **SecurityConfig**: Cấu hình Spring Security

## Cách Hoạt Động

### 1. **EndpointAuthFilter** (Lớp 1)
- Kiểm tra JWT token có hợp lệ không
- Kiểm tra role trong token có phải `ADMIN` không
- Trả về `401` nếu không có token hoặc token không hợp lệ
- Trả về `403` nếu token hợp lệ nhưng không phải admin

### 2. **SecurityConfig** (Lớp 2)
- Cấu hình Spring Security
- Chỉ cho phép role `ADMIN` truy cập `/api/admin/**`
- Fallback nếu filter không xử lý được

## Test Cases

### ✅ **Test 1: Không có token**
```bash
curl -X GET "http://localhost:8080/api/admin/shops"
# Expected: 401 Unauthorized
```

### ✅ **Test 2: Token không hợp lệ**
```bash
curl -X GET "http://localhost:8080/api/admin/shops" \
  -H "Authorization: Bearer invalid_token"
# Expected: 401 Unauthorized
```

### ✅ **Test 3: Token hợp lệ nhưng không phải admin**
```bash
# Với token của user thường hoặc seller
curl -X GET "http://localhost:8080/api/admin/shops" \
  -H "Authorization: Bearer <user_token>"
# Expected: 403 Forbidden
```

### ✅ **Test 4: Token admin hợp lệ**
```bash
# 1. Đăng nhập admin
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@foodshare.com",
    "password": "Admin123456"
  }'

# 2. Sử dụng token từ response
curl -X GET "http://localhost:8080/api/admin/shops" \
  -H "Authorization: Bearer <admin_token>"
# Expected: 200 OK
```

## Chạy Test Script

```bash
# Cấp quyền thực thi
chmod +x test_admin_auth.sh

# Chạy test
./test_admin_auth.sh
```

## Debug Authentication

### 1. **Kiểm tra JWT token**
```bash
# Decode JWT token (cần jq)
echo "YOUR_JWT_TOKEN" | cut -d. -f2 | base64 -d | jq
```

### 2. **Kiểm tra logs**
```properties
# application.properties
logging.level.com.miniapp.foodshare.security=DEBUG
logging.level.org.springframework.security=DEBUG
```

### 3. **Kiểm tra response headers**
```bash
curl -v -X GET "http://localhost:8080/api/admin/shops" \
  -H "Authorization: Bearer <token>"
```

## Các Lỗi Thường Gặp

### 1. **401 Unauthorized**
- **Nguyên nhân**: Không có token hoặc token không hợp lệ
- **Giải pháp**: Kiểm tra header `Authorization: Bearer <token>`

### 2. **403 Forbidden**
- **Nguyên nhân**: Token hợp lệ nhưng không có quyền admin
- **Giải pháp**: Đăng nhập với tài khoản admin

### 3. **500 Internal Server Error**
- **Nguyên nhân**: Lỗi trong quá trình xử lý token
- **Giải pháp**: Kiểm tra logs và cấu hình JWT

## JWT Token Structure

Token admin phải chứa:
```json
{
  "userId": 1,
  "role": "ADMIN",
  "type": "backoffice",
  "exp": 1234567890
}
```

## Security Headers

Response sẽ chứa các header:
- `WWW-Authenticate: Bearer` (khi 401)
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`

## Monitoring

### 1. **Logs quan trọng**
```
Authentication successful for user: 1 on path: /api/admin/shops
Access denied - Admin role required for path: /api/admin/shops
```

### 2. **Metrics**
- Số lượng request 401/403
- Thời gian xử lý authentication
- Số lượng admin login

## Best Practices

1. **Luôn sử dụng HTTPS** trong production
2. **Set token expiration** hợp lý (24h)
3. **Log security events** để monitoring
4. **Validate token** ở cả client và server
5. **Refresh token** khi cần thiết
