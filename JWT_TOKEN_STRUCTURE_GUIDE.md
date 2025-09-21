# Hướng Dẫn Cấu Trúc JWT Token

## Tổng Quan

JWT token được tạo bởi `JwtService.generateToken()` với cấu trúc claims cụ thể cho BackOffice users.

## Cấu Trúc Token

### **Method Signature**
```java
public static String generateToken(String subject, Map<String, Object> claims)
```

### **Claims Structure**
```java
Map<String, Object> claims = new HashMap<>();
claims.put("uid", user.getId());                    // User ID
claims.put("email", user.getEmail());              // Email
claims.put("role", user.getRole().getCode());      // Role (ADMIN/SELLER)
claims.put("type", "backoffice");                  // Token type
```

### **Subject Format**
```java
String subject = "user:" + user.getId();
// Ví dụ: "user:1", "user:2"
```

## Token Payload Example

### **Admin Token**
```json
{
  "sub": "user:1",
  "uid": 1,
  "email": "admin@foodshare.com",
  "role": "ADMIN",
  "type": "backoffice",
  "iat": 1234567890,
  "exp": 1234654290
}
```

### **Seller Token**
```json
{
  "sub": "user:2",
  "uid": 2,
  "email": "seller@foodshare.com",
  "role": "SELLER",
  "type": "backoffice",
  "iat": 1234567890,
  "exp": 1234654290
}
```

## Cách Sử Dụng

### **1. Tạo Token (BackOfficeAuthService)**
```java
// Login
Map<String, Object> claims = new HashMap<>();
claims.put("uid", user.getId());
claims.put("email", user.getEmail());
claims.put("role", user.getRole().getCode());
claims.put("type", "backoffice");
String accessToken = JwtService.generateToken("user:" + user.getId(), claims);
```

### **2. Parse Token (EndpointAuthFilter)**
```java
Jws<Claims> claims = jwtService.parse(token);
String role = claims.getBody().get("role", String.class);
Integer uid = claims.getBody().get("uid", Integer.class);
```

## Security Features

### **1. Token Expiration**
- **Default**: 240 hours (10 days)
- **Configurable**: Thay đổi `EXP_MS` trong JwtService

### **2. Token Type Validation**
- **BackOffice tokens**: `type = "backoffice"`
- **Social tokens**: `type = "social"` (khác)

### **3. Role-based Access**
- **Admin APIs**: `role = "ADMIN"`
- **Seller APIs**: `role = "SELLER"` hoặc `"ADMIN"`

## Test Token

### **1. Decode Token (nếu có jq)**
```bash
# Decode header
echo $TOKEN | cut -d. -f1 | base64 -d | jq

# Decode payload
echo $TOKEN | cut -d. -f2 | base64 -d | jq
```

### **2. Test với curl**
```bash
# Login để lấy token
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@foodshare.com",
    "password": "Admin123456"
  }'

# Sử dụng token
curl -X GET "http://localhost:8080/api/admin/shops" \
  -H "Authorization: Bearer $TOKEN"
```

## Troubleshooting

### **1. Token không hợp lệ**
- **Nguyên nhân**: Signature không đúng hoặc token bị modify
- **Giải pháp**: Kiểm tra secret key và token integrity

### **2. Token hết hạn**
- **Nguyên nhân**: `exp` claim đã qua thời gian hiện tại
- **Giải pháp**: Đăng nhập lại để lấy token mới

### **3. Role không đúng**
- **Nguyên nhân**: Token không chứa role phù hợp
- **Giải pháp**: Kiểm tra claims trong token

### **4. Token type không đúng**
- **Nguyên nhân**: `type` claim không phải "backoffice"
- **Giải pháp**: Sử dụng đúng service để tạo token

## Best Practices

1. **Luôn validate token** trước khi sử dụng
2. **Kiểm tra expiration** để tránh token hết hạn
3. **Validate role** cho từng API endpoint
4. **Log security events** để monitoring
5. **Sử dụng HTTPS** trong production
6. **Rotate secret key** định kỳ

## Configuration

### **JwtService Settings**
```java
private static final String SECRET = "your-secret-key";
private static final long EXP_MS = 1000L * 60 * 60 * 240; // 240h
```

### **Token Claims**
- **uid**: Integer - User ID
- **email**: String - User email
- **role**: String - User role (ADMIN/SELLER)
- **type**: String - Token type (backoffice)
- **iat**: Long - Issued at timestamp
- **exp**: Long - Expiration timestamp
