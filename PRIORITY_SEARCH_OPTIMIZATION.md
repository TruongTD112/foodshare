# Tối Ưu Hóa API SearchProducts Với Thứ Tự Ưu Tiên

## 🎯 **Mục tiêu**
Tối ưu hóa API `searchProducts` với thứ tự ưu tiên thông minh: **name → discount → distance** sử dụng native query để tăng hiệu suất và trải nghiệm người dùng.

## 🧠 **Thứ tự ưu tiên**

### **1. Tên sản phẩm (1000 điểm)**
- Sản phẩm có tên chứa từ khóa tìm kiếm được ưu tiên cao nhất
- Case-insensitive search
- Sử dụng `LIKE` với `%keyword%`

### **2. Có giảm giá (500 điểm)**
- Sản phẩm có `originalPrice > price` được ưu tiên
- Khuyến khích người dùng mua sản phẩm giảm giá

### **3. Khoảng cách gần (1000 - distance_km điểm)**
- Sản phẩm gần vị trí người dùng được ưu tiên
- Điểm tối đa: 1000 (khoảng cách 0km)
- Điểm tối thiểu: 0 (khoảng cách ≥1000km)

## 📊 **Công thức tính điểm ưu tiên**

```sql
priority_score = 
  -- Ưu tiên 1: Tên sản phẩm (1000 điểm nếu match)
  CASE 
    WHEN nameQuery IS NOT NULL AND nameQuery != '' 
         AND LOWER(p.name) LIKE LOWER(CONCAT('%', nameQuery, '%'))
    THEN 1000
    ELSE 0
  END +
  -- Ưu tiên 2: Có giảm giá (500 điểm)
  CASE 
    WHEN p.original_price IS NOT NULL 
         AND p.price IS NOT NULL 
         AND p.original_price > p.price
    THEN 500
    ELSE 0
  END +
  -- Ưu tiên 3: Khoảng cách gần (1000 - distance_km điểm)
  CASE 
    WHEN latitude IS NOT NULL AND longitude IS NOT NULL 
         AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL
    THEN GREATEST(0, 1000 - ROUND(distance_km, 0))
    ELSE 0
  END
```

## 🚀 **Native Query tối ưu**

```sql
SELECT 
    p.id, p.name, p.price, p.original_price, p.image_url, p.shop_id,
    s.name as shop_name, s.address as shop_address, s.phone as shop_phone,
    s.image_url as shop_image_url, s.latitude as shop_latitude,
    s.longitude as shop_longitude, s.description as shop_description,
    s.rating as shop_rating, s.status as shop_status,
    -- Tính khoảng cách (nếu có tọa độ)
    CASE 
        WHEN :latitude IS NOT NULL AND :longitude IS NOT NULL 
             AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL
        THEN ROUND(6371 * ACOS(...), 0)
        ELSE NULL
    END as distance_km,
    -- Tính điểm ưu tiên
    (
        -- Ưu tiên 1: Tên sản phẩm (1000 điểm)
        CASE 
            WHEN :nameQuery IS NOT NULL AND :nameQuery != '' 
                 AND LOWER(p.name) LIKE LOWER(CONCAT('%', :nameQuery, '%'))
            THEN 1000
            ELSE 0
        END +
        -- Ưu tiên 2: Có giảm giá (500 điểm)
        CASE 
            WHEN p.original_price IS NOT NULL 
                 AND p.price IS NOT NULL 
                 AND p.original_price > p.price
            THEN 500
            ELSE 0
        END +
        -- Ưu tiên 3: Khoảng cách gần (1000 - distance_km điểm)
        CASE 
            WHEN :latitude IS NOT NULL AND :longitude IS NOT NULL 
                 AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL
            THEN GREATEST(0, 1000 - ROUND(6371 * ACOS(...), 0))
            ELSE 0
        END
    ) as priority_score
FROM Product p
INNER JOIN Shop s ON p.shop_id = s.id
WHERE p.status = '1'
  AND s.status = '1'
  AND s.latitude IS NOT NULL 
  AND s.longitude IS NOT NULL
  -- Lọc theo tên (nếu có)
  AND (:nameQuery IS NULL OR :nameQuery = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :nameQuery, '%')))
  -- Lọc theo khoảng cách (nếu có)
  AND (:latitude IS NULL OR :longitude IS NULL OR :maxDistanceKm IS NULL OR distance_km <= :maxDistanceKm)
  -- Lọc theo giá (nếu có)
  AND (:minPrice IS NULL OR p.price >= :minPrice)
  AND (:maxPrice IS NULL OR p.price <= :maxPrice)
ORDER BY 
    priority_score DESC,  -- Sắp xếp theo điểm ưu tiên giảm dần
    distance_km ASC       -- Nếu cùng điểm, sắp xếp theo khoảng cách tăng dần
```

## 📁 **Files đã cập nhật**

### **1. ProductRepository.java**
```java
@Query(value = """
    SELECT 
        p.id, p.name, p.price, p.original_price, p.image_url, p.shop_id,
        s.name as shop_name, s.address as shop_address, s.phone as shop_phone,
        s.image_url as shop_image_url, s.latitude as shop_latitude,
        s.longitude as shop_longitude, s.description as shop_description,
        s.rating as shop_rating, s.status as shop_status,
        -- Tính khoảng cách và điểm ưu tiên
        CASE WHEN :latitude IS NOT NULL AND :longitude IS NOT NULL 
             AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL
        THEN ROUND(6371 * ACOS(...), 0)
        ELSE NULL END as distance_km,
        -- Tính điểm ưu tiên: name → discount → distance
        (...) as priority_score
    FROM Product p
    INNER JOIN Shop s ON p.shop_id = s.id
    WHERE p.status = '1' AND s.status = '1'
      AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL
      -- Các điều kiện lọc khác
    ORDER BY priority_score DESC, distance_km ASC
    """, nativeQuery = true)
List<Object[]> searchProductsWithPriority(
    @Param("nameQuery") String nameQuery,
    @Param("latitude") Double latitude,
    @Param("longitude") Double longitude,
    @Param("maxDistanceKm") Double maxDistanceKm,
    @Param("minPrice") BigDecimal minPrice,
    @Param("maxPrice") BigDecimal maxPrice,
    Pageable pageable
);
```

### **2. ProductService.java**
- **Thay thế method `searchProducts`** để sử dụng native query với priority scoring
- **Map kết quả Object[]** thành ProductSearchItem với đầy đủ thông tin
- **Xử lý pagination** và error handling
- **Validation** tọa độ và tham số

## 🧪 **Testing**

### **Scripts test:**
1. **`test_priority_search.sh`** - Test thứ tự ưu tiên và hiệu suất

### **Chạy test:**
```bash
# Test thứ tự ưu tiên
chmod +x test_priority_search.sh
./test_priority_search.sh
```

### **Test cases:**
1. **Tìm kiếm theo tên** - kiểm tra ưu tiên cao nhất
2. **Tìm kiếm không có tên** - kiểm tra ưu tiên discount
3. **Tìm kiếm tên không tồn tại** - kiểm tra ưu tiên distance
4. **So sánh thứ tự ưu tiên** - kiểm tra điểm số
5. **Test hiệu suất** - đo thời gian response
6. **Test với tham số khác nhau** - minPrice, maxPrice, maxDistanceKm

## 📈 **Kết quả mong đợi**

### **Thứ tự ưu tiên:**
1. **Sản phẩm có tên match** (1000 điểm)
2. **Sản phẩm có giảm giá** (500 điểm)
3. **Sản phẩm gần nhất** (1000 - distance_km điểm)

### **Hiệu suất:**
- ⚡ **Giảm thời gian response** đáng kể
- ⚡ **Tối ưu database query** với native SQL
- ⚡ **Sắp xếp thông minh** theo điểm ưu tiên

### **Trải nghiệm người dùng:**
- ✅ **Kết quả phù hợp nhất** được hiển thị đầu tiên
- ✅ **Sản phẩm giảm giá** được ưu tiên
- ✅ **Sản phẩm gần nhất** được ưu tiên khi không có tên match

## 🔧 **Cấu hình**

### **Điểm ưu tiên có thể điều chỉnh:**
```java
// Trong native query
CASE WHEN name_match THEN 1000 ELSE 0 END +  // Tên sản phẩm
CASE WHEN has_discount THEN 500 ELSE 0 END + // Có giảm giá  
CASE WHEN has_coords THEN (1000 - distance) ELSE 0 END // Khoảng cách
```

### **Database indexes cần thiết:**
```sql
-- Index cho tìm kiếm tên
CREATE INDEX idx_product_name ON Product(name);

-- Index cho tọa độ cửa hàng
CREATE INDEX idx_shop_coordinates ON Shop(latitude, longitude);

-- Index cho giá sản phẩm
CREATE INDEX idx_product_price ON Product(price);

-- Index cho trạng thái
CREATE INDEX idx_product_status ON Product(status);
CREATE INDEX idx_shop_status ON Shop(status);
```

## ⚠️ **Lưu ý**

### **Hạn chế:**
1. **Pagination:** Cần xử lý total count riêng với native query
2. **Complexity:** Query phức tạp hơn, khó maintain
3. **Database dependency:** Phụ thuộc vào MySQL functions

### **Tối ưu thêm:**
1. **Caching** kết quả cho từ khóa phổ biến
2. **Index optimization** cho các trường tìm kiếm
3. **Connection pooling** cho concurrent requests
4. **Monitoring** performance metrics

## 🚀 **Deployment**

### **Bước 1: Backup database**
```bash
mysqldump -u username -p database_name > backup_before_priority_search.sql
```

### **Bước 2: Deploy code**
```bash
./gradlew build
# Deploy theo quy trình của bạn
```

### **Bước 3: Test production**
```bash
# Test API với thứ tự ưu tiên
curl "https://your-api.com/products?q=pizza&lat=10.762622&lon=106.660172&page=0&size=10"
```

## 📊 **Metrics để theo dõi**

1. **Response time** của API `/products`
2. **Database query time** cho native query
3. **User satisfaction** với kết quả tìm kiếm
4. **Conversion rate** từ tìm kiếm đến mua hàng
5. **Error rate** trong mapping Object[]

## 🔄 **Rollback Plan**

Nếu có vấn đề:
1. **Revert code** về version cũ
2. **Redeploy** application
3. **Restore database** nếu cần

---

**Tác giả:** AI Assistant  
**Ngày tạo:** $(date)  
**Version:** 1.0
