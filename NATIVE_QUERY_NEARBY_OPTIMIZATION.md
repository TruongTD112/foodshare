# Tối Ưu Hóa API SearchNearBy Với Native Query

## 🎯 **Mục tiêu**
Tối ưu hóa API `searchNearBy` để tính khoảng cách trực tiếp trong database thay vì lọc sau khi load dữ liệu vào memory, giúp tăng hiệu suất và tránh miss case.

## ❌ **Vấn đề hiện tại**

### **Implementation cũ:**
1. **Lấy tất cả sản phẩm theo status trước** (dòng 96-100 trong ProductService)
2. **Sau đó mới tính khoảng cách** (dòng 162-164) 
3. **Cuối cùng mới lọc theo khoảng cách** (dòng 166-168)

### **Vấn đề:**
- ❌ Lấy quá nhiều sản phẩm không cần thiết từ database
- ❌ Chỉ lọc theo khoảng cách sau khi đã load hết vào memory
- ❌ Không tối ưu hiệu suất, đặc biệt với dataset lớn
- ❌ Có thể miss case khi có quá nhiều sản phẩm

## ✅ **Giải pháp: Native Query**

### **Native Query SQL:**
```sql
SELECT 
    p.id,
    p.name,
    p.price,
    p.original_price,
    p.image_url,
    p.shop_id,
    s.name as shop_name,
    s.address as shop_address,
    s.phone as shop_phone,
    s.image_url as shop_image_url,
    s.latitude as shop_latitude,
    s.longitude as shop_longitude,
    s.description as shop_description,
    s.rating as shop_rating,
    s.status as shop_status,
    -- Tính khoảng cách bằng công thức Haversine (km)
    ROUND(
        6371 * ACOS(
            COS(RADIANS(:latitude)) * 
            COS(RADIANS(s.latitude)) * 
            COS(RADIANS(s.longitude) - RADIANS(:longitude)) + 
            SIN(RADIANS(:latitude)) * 
            SIN(RADIANS(s.latitude))
        ), 0
    ) as distance_km
FROM Product p
INNER JOIN Shop s ON p.shop_id = s.id
WHERE p.status = '1'  -- Chỉ sản phẩm active
  AND s.status = '1'  -- Chỉ cửa hàng active
  AND s.latitude IS NOT NULL 
  AND s.longitude IS NOT NULL
  -- Lọc theo khoảng cách tối đa
  AND (
    6371 * ACOS(
        COS(RADIANS(:latitude)) * 
        COS(RADIANS(s.latitude)) * 
        COS(RADIANS(s.longitude) - RADIANS(:longitude)) + 
        SIN(RADIANS(:latitude)) * 
        SIN(RADIANS(s.latitude))
    )
  ) <= :maxDistanceKm
ORDER BY distance_km ASC  -- Sắp xếp theo khoảng cách tăng dần
LIMIT :limit OFFSET :offset
```

### **Ưu điểm:**
- ✅ **Tính khoảng cách trực tiếp trong database**
- ✅ **Lọc theo khoảng cách ngay trong WHERE clause**
- ✅ **Sắp xếp theo khoảng cách trong ORDER BY**
- ✅ **Chỉ lấy dữ liệu cần thiết**
- ✅ **Tối ưu hiệu suất với dataset lớn**
- ✅ **Không miss case**

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
        ROUND(
            6371 * ACOS(
                COS(RADIANS(:latitude)) * 
                COS(RADIANS(s.latitude)) * 
                COS(RADIANS(s.longitude) - RADIANS(:longitude)) + 
                SIN(RADIANS(:latitude)) * 
                SIN(RADIANS(s.latitude))
            ), 0
        ) as distance_km
    FROM Product p
    INNER JOIN Shop s ON p.shop_id = s.id
    WHERE p.status = '1'
      AND s.status = '1'
      AND s.latitude IS NOT NULL 
      AND s.longitude IS NOT NULL
      AND (
        6371 * ACOS(
            COS(RADIANS(:latitude)) * 
            COS(RADIANS(s.latitude)) * 
            COS(RADIANS(s.longitude) - RADIANS(:longitude)) + 
            SIN(RADIANS(:latitude)) * 
            SIN(RADIANS(s.latitude))
        )
      ) <= :maxDistanceKm
    ORDER BY distance_km ASC
    """, nativeQuery = true)
List<Object[]> findNearbyProductsWithDistance(
    @Param("latitude") Double latitude,
    @Param("longitude") Double longitude,
    @Param("maxDistanceKm") Double maxDistanceKm,
    Pageable pageable
);
```

### **2. ProductService.java**
- **Thay thế method `searchNearbyProducts`** để sử dụng native query
- **Map kết quả Object[]** thành ProductSearchItem
- **Xử lý pagination** với native query
- **Error handling** cho mapping dữ liệu

## 🧪 **Testing**

### **Scripts test:**
1. **`test_native_nearby_query.sh`** - Test cơ bản native query
2. **`compare_nearby_performance.sh`** - So sánh hiệu suất

### **Chạy test:**
```bash
# Test cơ bản
chmod +x test_native_nearby_query.sh
./test_native_nearby_query.sh

# So sánh hiệu suất
chmod +x compare_nearby_performance.sh
./compare_nearby_performance.sh
```

## 📊 **Kết quả mong đợi**

### **Hiệu suất:**
- ⚡ **Giảm thời gian response** đáng kể
- ⚡ **Giảm memory usage** (không load dữ liệu không cần thiết)
- ⚡ **Tăng throughput** với dataset lớn

### **Độ chính xác:**
- ✅ **Không miss case** nào
- ✅ **Sắp xếp đúng** theo khoảng cách
- ✅ **Lọc chính xác** theo khoảng cách tối đa

## 🔧 **Cấu hình**

### **Constants:**
- `Constants.Distance.DEFAULT_MAX_DISTANCE_KM` - Khoảng cách mặc định (50km)
- `Constants.Pagination.MAX_PAGE_SIZE` - Kích thước trang tối đa (100)

### **Database:**
- Cần index trên `Shop.latitude` và `Shop.longitude` để tối ưu
- Cần index trên `Product.status` và `Shop.status`

## ⚠️ **Lưu ý**

### **Hạn chế của native query:**
1. **Pagination:** Cần xử lý total count riêng
2. **Mapping:** Cần map Object[] thành DTO thủ công
3. **Maintenance:** Khó maintain hơn JPQL

### **Tối ưu thêm:**
1. **Index database** trên latitude/longitude
2. **Caching** kết quả cho tọa độ phổ biến
3. **Connection pooling** để xử lý concurrent requests

## 🚀 **Deployment**

### **Bước 1: Backup database**
```bash
# Backup trước khi deploy
mysqldump -u username -p database_name > backup_before_native_query.sql
```

### **Bước 2: Deploy code**
```bash
# Build và deploy
./gradlew build
# Deploy theo quy trình của bạn
```

### **Bước 3: Test production**
```bash
# Test API sau khi deploy
curl "https://your-api.com/products/nearby?lat=10.762622&lon=106.660172&page=0&size=10"
```

### **Bước 4: Monitor**
- Monitor response time
- Monitor error rate
- Monitor database performance

## 📈 **Metrics để theo dõi**

1. **Response time** của API `/products/nearby`
2. **Database query time** cho native query
3. **Memory usage** của application
4. **Error rate** trong mapping Object[]
5. **Throughput** (requests/second)

## 🔄 **Rollback Plan**

Nếu có vấn đề, có thể rollback bằng cách:
1. **Revert code** về version cũ
2. **Redeploy** application
3. **Restore database** nếu cần

---

**Tác giả:** AI Assistant  
**Ngày tạo:** $(date)  
**Version:** 1.0
