# Hướng Dẫn Password Encoder

## Tổng Quan

Hệ thống sử dụng **BCryptPasswordEncoder** để mã hóa mật khẩu một cách an toàn.

## Cấu Hình

### **SecurityConfig.java**
```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### **BackOfficeAuthService.java**
```java
private final PasswordEncoder passwordEncoder;
```

## Cách Hoạt Động

### **1. Mã Hóa Mật Khẩu (Khi Tạo User)**
```java
// Tạo seller
BackOfficeUser user = BackOfficeUser.builder()
    .name(request.getName())
    .email(request.getEmail())
    .passwordHash(passwordEncoder.encode(request.getPassword())) // Mã hóa password
    .role(UserRole.SELLER)
    .build();
```

### **2. Xác Thực Mật Khẩu (Khi Đăng Nhập)**
```java
// Kiểm tra password
if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
    return Result.error(ErrorCode.INVALID_CREDENTIALS, "Email hoặc mật khẩu không đúng");
}
```

## BCrypt Features

### **1. Salt Tự Động**
- BCrypt tự động tạo salt cho mỗi password
- Mỗi lần encode cùng 1 password sẽ cho kết quả khác nhau
- Salt được lưu trong hash, không cần lưu riêng

### **2. Work Factor**
- **Default**: 10 rounds (2^10 = 1024 iterations)
- **Có thể tăng**: Để tăng độ bảo mật
- **Trade-off**: Bảo mật cao hơn nhưng chậm hơn

### **3. Hash Format**
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
 |  |  |                     |
 |  |  |                     +-- Salt + Hash
 |  |  +-- Work Factor (10)
 |  +-- BCrypt Version (2a)
 +-- Algorithm (BCrypt)
```

## Ví Dụ Thực Tế

### **1. Password Gốc**
```java
String rawPassword = "Admin123456";
```

### **2. Sau Khi Encode**
```java
String encodedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
```

### **3. Verification**
```java
boolean matches = passwordEncoder.matches("Admin123456", encodedPassword);
// Result: true
```

## Test Cases

### **1. Đăng Ký User Mới**
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "TestPassword123"
  }'
```

### **2. Đăng Nhập Với Password Đúng**
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPassword123"
  }'
```

### **3. Đăng Nhập Với Password Sai**
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "WrongPassword"
  }'
```

## Security Best Practices

### **1. Password Requirements**
- **Minimum length**: 8 characters
- **Mix of cases**: Upper + Lower case
- **Numbers**: At least 1 digit
- **Special chars**: Optional but recommended

### **2. Storage**
- **Never store** raw passwords
- **Always hash** before storing
- **Use salt** (BCrypt tự động)
- **Secure database** access

### **3. Validation**
- **Client-side**: Basic validation
- **Server-side**: Strict validation
- **Rate limiting**: Prevent brute force
- **Account lockout**: After failed attempts

## Troubleshooting

### **1. Password Không Match**
- **Nguyên nhân**: Password gốc khác với password đã lưu
- **Giải pháp**: Kiểm tra input và database

### **2. Encoding Lỗi**
- **Nguyên nhân**: BCrypt configuration sai
- **Giải pháp**: Kiểm tra SecurityConfig

### **3. Performance Issues**
- **Nguyên nhân**: Work factor quá cao
- **Giải pháp**: Giảm work factor (trade-off security)

## Monitoring

### **1. Logs Quan Trọng**
```
User registered successfully: userId=1, email=test@example.com
User logged in successfully: userId=1, email=test@example.com
Invalid password for user: test@example.com
```

### **2. Metrics**
- Số lượng đăng ký thành công
- Số lượng đăng nhập thành công
- Số lượng đăng nhập thất bại
- Thời gian xử lý password

## Configuration Options

### **1. Custom Work Factor**
```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // 12 rounds
}
```

### **2. Custom Password Encoder**
```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2A, 10);
}
```

## Migration

### **1. Từ Plain Text**
```java
// Old way (DON'T DO THIS)
user.setPasswordHash(rawPassword);

// New way
user.setPasswordHash(passwordEncoder.encode(rawPassword));
```

### **2. Từ MD5/SHA1**
```java
// Migration script needed
// 1. Verify old password with old algorithm
// 2. Re-encode with BCrypt
// 3. Update database
```

## Testing

### **1. Unit Tests**
```java
@Test
void testPasswordEncoding() {
    String rawPassword = "TestPassword123";
    String encoded = passwordEncoder.encode(rawPassword);
    
    assertTrue(passwordEncoder.matches(rawPassword, encoded));
    assertFalse(passwordEncoder.matches("WrongPassword", encoded));
}
```

### **2. Integration Tests**
```bash
# Run test script
chmod +x test_password_encoder.sh
./test_password_encoder.sh
```
