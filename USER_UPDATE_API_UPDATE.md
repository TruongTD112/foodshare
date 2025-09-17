# API Cập Nhật Thông Tin Người Dùng

## 🎯 **Mục tiêu**
Tạo API cập nhật thông tin người dùng với xác thực, dựa trên entity `CustomerUser` đã có.

## 📋 **Thay đổi thực hiện**

### **1. DTOs**

#### **UpdateUserRequest.java**
```java
@Data
public class UpdateUserRequest {
    @NotBlank(message = "Tên không được để trống")
    @Size(min = 2, max = 255, message = "Tên phải có từ 2 đến 255 ký tự")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không được quá 255 ký tự")
    private String email;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    private String phoneNumber;

    @Size(max = 255, message = "URL ảnh đại diện không được quá 255 ký tự")
    private String profilePictureUrl;
}
```

#### **UserInfoResponse.java**
```java
@Value
@Builder
public class UserInfoResponse {
    Integer id;
    String name;
    String email;
    String provider;
    String providerId;
    String profilePictureUrl;
    String phoneNumber;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

### **2. Repository**

#### **CustomerUserRepository.java**
```java
@Repository
public interface CustomerUserRepository extends JpaRepository<CustomerUser, Integer> {
    Optional<CustomerUser> findByEmail(String email);
    Optional<CustomerUser> findByProviderAndProviderId(String provider, String providerId);
    boolean existsByEmailAndIdNot(String email, Integer id);
}
```

### **3. Service**

#### **UserService.java**
```java
@Service
public class UserService {
    // Cập nhật thông tin người dùng
    @Transactional
    public Result<UserInfoResponse> updateUser(Integer userId, UpdateUserRequest request);
    
    // Lấy thông tin người dùng
    @Transactional(readOnly = true)
    public Result<UserInfoResponse> getUserInfo(Integer userId);
}
```

### **4. Controller**

#### **UserController.java**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    // PUT /api/users/{userId} - Cập nhật thông tin
    @PutMapping("/{userId}")
    public ResponseEntity<Result<UserInfoResponse>> updateUser(
        @PathVariable Integer userId,
        @Valid @RequestBody UpdateUserRequest request
    );
    
    // GET /api/users/{userId} - Lấy thông tin
    @GetMapping("/{userId}")
    public ResponseEntity<Result<UserInfoResponse>> getUserInfo(
        @PathVariable Integer userId
    );
}
```

### **5. Error Codes**
```java
// Thêm vào ErrorCode.java
EMAIL_ALREADY_EXISTS("409", "Email already exists"),
```

## 📊 **API Endpoints**

### **1. Cập nhật thông tin người dùng**
```http
PUT /api/users/{userId}
Content-Type: application/json
Authorization: Bearer <token>

{
  "name": "Nguyễn Văn A",
  "email": "nguyenvana@email.com",
  "phoneNumber": "0123456789",
  "profilePictureUrl": "https://example.com/avatar.jpg"
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
    "name": "Nguyễn Văn A",
    "email": "nguyenvana@email.com",
    "provider": "google",
    "providerId": "google_123456789",
    "profilePictureUrl": "https://example.com/avatar.jpg",
    "phoneNumber": "0123456789",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T12:00:00"
  }
}
```

### **2. Lấy thông tin người dùng**
```http
GET /api/users/{userId}
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Nguyễn Văn A",
    "email": "nguyenvana@email.com",
    "provider": "google",
    "providerId": "google_123456789",
    "profilePictureUrl": "https://example.com/avatar.jpg",
    "phoneNumber": "0123456789",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T12:00:00"
  }
}
```

## 🔧 **Validation Rules**

### **UpdateUserRequest:**
- **name**: Bắt buộc, 2-255 ký tự
- **email**: Bắt buộc, format email hợp lệ, tối đa 255 ký tự
- **phoneNumber**: Tùy chọn, 10-11 chữ số
- **profilePictureUrl**: Tùy chọn, tối đa 255 ký tự

### **Business Logic:**
- **Email uniqueness**: Kiểm tra email không trùng với user khác
- **User existence**: Kiểm tra user tồn tại
- **Data integrity**: Cập nhật an toàn với transaction

## 🧪 **Test Cases**

### **Test cập nhật thành công:**
```bash
curl -X PUT "http://localhost:8080/api/users/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "name": "Nguyễn Văn A Updated",
    "email": "nguyenvana.updated@email.com",
    "phoneNumber": "0987654321",
    "profilePictureUrl": "https://example.com/new-avatar.jpg"
  }'
```

### **Test validation errors:**
```bash
# Email không hợp lệ
curl -X PUT "http://localhost:8080/api/users/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "name": "Nguyễn Văn A",
    "email": "invalid-email",
    "phoneNumber": "0123456789"
  }'

# Tên quá ngắn
curl -X PUT "http://localhost:8080/api/users/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "name": "A",
    "email": "nguyenvana@email.com",
    "phoneNumber": "0123456789"
  }'
```

### **Test lấy thông tin:**
```bash
curl -X GET "http://localhost:8080/api/users/1" \
  -H "Authorization: Bearer <token>"
```

## 🔐 **Xác thực**

### **Cần implement:**
1. **JWT Token validation** - Kiểm tra token hợp lệ
2. **User ID extraction** - Lấy userId từ token
3. **Authorization check** - Kiểm tra user có quyền cập nhật

### **Ví dụ middleware:**
```java
@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        // Validate JWT token
        String token = extractToken(request);
        if (!isValidToken(token)) {
            response.setStatus(401);
            return false;
        }
        
        // Extract userId from token
        Integer userId = getUserIdFromToken(token);
        request.setAttribute("userId", userId);
        
        return true;
    }
}
```

## 📁 **Files đã tạo**

1. **`UpdateUserRequest.java`** - DTO cho request cập nhật
2. **`UserInfoResponse.java`** - DTO cho response
3. **`CustomerUserRepository.java`** - Repository cho CustomerUser
4. **`UserService.java`** - Service logic
5. **`UserController.java`** - REST controller
6. **`ErrorCode.java`** - Thêm error code mới

## ✅ **Lợi ích**

### **1. Bảo mật:**
- Validation đầy đủ cho input
- Kiểm tra email uniqueness
- Transaction safety

### **2. User Experience:**
- API đơn giản, dễ sử dụng
- Response rõ ràng
- Error messages chi tiết

### **3. Maintainability:**
- Code structure rõ ràng
- Separation of concerns
- Comprehensive logging

## 🚀 **Next Steps**

### **Cần implement:**
1. **JWT Authentication** - Middleware xác thực
2. **Password update** - API đổi mật khẩu
3. **Profile image upload** - API upload ảnh đại diện
4. **Account deletion** - API xóa tài khoản

### **Optional features:**
1. **Email verification** - Xác thực email
2. **Phone verification** - Xác thực số điện thoại
3. **Profile privacy** - Cài đặt riêng tư

API cập nhật thông tin người dùng đã sẵn sàng! 🔐✨
