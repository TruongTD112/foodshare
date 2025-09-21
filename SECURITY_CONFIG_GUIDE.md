# Hướng Dẫn Cấu Hình Security

## Tổng Quan

SecurityConfig sử dụng pattern-based protection để quản lý quyền truy cập API một cách linh hoạt và dễ bảo trì.

## Cấu Trúc Phân Quyền

### 🔓 **PUBLIC_PATTERNS** - Không Cần Authentication

```java
private static final String[] PUBLIC_PATTERNS = new String[] {
    "/api/auth/login",         // Login endpoint
    "/api/products/**",        // Product APIs - public
    "/api/shops/**",           // Shop APIs - public
    "/swagger-ui/**",          // Swagger UI
    "/api-docs/**",            // API docs
    "/v3/api-docs/**",         // OpenAPI docs
    "/actuator/**"             // Actuator endpoints
};
```

**Đặc điểm:**
- Không cần JWT token
- Ai cũng có thể truy cập
- Chủ yếu là API công khai và documentation

### 🔒 **PROTECTED_PATTERNS** - Cần Authentication

```java
private static final String[] PROTECTED_PATTERNS = new String[] {
    "/api/admin/**",           // Admin APIs - chỉ ADMIN
    "/api/seller/**",          // Seller APIs - ADMIN hoặc SELLER
    "/api/orders/**",          // Order APIs - cần authentication
    "/api/users/**"            // User APIs - cần authentication
};
```

**Đặc điểm:**
- Cần JWT token hợp lệ
- Phân quyền theo role cụ thể
- Bảo vệ dữ liệu nhạy cảm

## Chi Tiết Phân Quyền

### 1. **Admin APIs** - `/api/admin/**`
- **Quyền:** Chỉ `ADMIN`
- **Mô tả:** Quản lý toàn bộ hệ thống
- **Ví dụ:**
  - `GET /api/admin/shops` - Xem danh sách cửa hàng
  - `POST /api/admin/shops` - Tạo cửa hàng mới
  - `PUT /api/admin/shops/{id}` - Cập nhật cửa hàng
  - `DELETE /api/admin/shops/{id}` - Xóa cửa hàng

### 2. **Seller APIs** - `/api/seller/**`
- **Quyền:** `ADMIN` hoặc `SELLER`
- **Mô tả:** Quản lý cửa hàng và sản phẩm của seller
- **Ví dụ:**
  - `GET /api/seller/shops` - Xem cửa hàng của seller
  - `POST /api/seller/products` - Tạo sản phẩm mới
  - `PUT /api/seller/products/{id}` - Cập nhật sản phẩm

### 3. **Order APIs** - `/api/orders/**`
- **Quyền:** Bất kỳ user đã đăng nhập
- **Mô tả:** Quản lý đơn hàng
- **Ví dụ:**
  - `POST /api/orders` - Tạo đơn hàng mới
  - `GET /api/orders/{id}` - Xem chi tiết đơn hàng
  - `PUT /api/orders/{id}/status` - Cập nhật trạng thái đơn hàng

### 4. **User APIs** - `/api/users/**`
- **Quyền:** Bất kỳ user đã đăng nhập
- **Mô tả:** Quản lý thông tin user
- **Ví dụ:**
  - `GET /api/users/profile` - Xem profile
  - `PUT /api/users/profile` - Cập nhật profile

## Cách Thêm API Mới

### Bước 1: Xác định loại API
- **Public:** Không cần authentication (sản phẩm, cửa hàng)
- **Protected:** Cần authentication (đơn hàng, user)
- **Role-based:** Cần role cụ thể (admin, seller)

### Bước 2: Thêm vào pattern tương ứng

**Nếu là Public API:**
```java
// Thêm vào PUBLIC_PATTERNS
"/api/new-public/**"
```

**Nếu cần authentication:**
```java
// Thêm vào PROTECTED_PATTERNS
"/api/new-protected/**"

// Và thêm rule trong filterChain
.requestMatchers("/api/new-protected/**").authenticated()
```

**Nếu cần role cụ thể:**
```java
// Thêm vào PROTECTED_PATTERNS
"/api/new-role-based/**"

// Và thêm rule trong filterChain
.requestMatchers("/api/new-role-based/**").hasRole("SPECIFIC_ROLE")
```

## Ví Dụ Thực Tế

### Thêm API Quản Lý Category (Chỉ Admin)

```java
// 1. Thêm vào PROTECTED_PATTERNS
"/api/categories/**"

// 2. Thêm rule trong filterChain
.requestMatchers("/api/categories/**").hasRole("ADMIN")
```

### Thêm API Notification (Tất Cả User)

```java
// 1. Thêm vào PROTECTED_PATTERNS
"/api/notifications/**"

// 2. Thêm rule trong filterChain
.requestMatchers("/api/notifications/**").authenticated()
```

## Lưu Ý Quan Trọng

1. **Thứ tự quan trọng:** Các rule được kiểm tra theo thứ tự từ trên xuống
2. **Pattern matching:** Sử dụng `/**` để match tất cả sub-paths
3. **Role names:** Phải match với role trong JWT token (ADMIN, SELLER, CUSTOMER)
4. **Testing:** Luôn test với các role khác nhau để đảm bảo phân quyền đúng

## Debug Security

Nếu gặp vấn đề với phân quyền:

1. **Kiểm tra JWT token:** Đảm bảo token hợp lệ và chứa role đúng
2. **Kiểm tra pattern:** Đảm bảo URL match với pattern đã định nghĩa
3. **Kiểm tra thứ tự:** Rule đầu tiên match sẽ được áp dụng
4. **Log security:** Bật debug logging để xem quá trình authentication

```properties
# application.properties
logging.level.org.springframework.security=DEBUG
```
