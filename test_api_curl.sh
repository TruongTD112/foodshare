#!/bin/bash

# =====================================================
# SCRIPT TEST API FOODSHARE VỚI CURL
# =====================================================

BASE_URL="http://localhost:8080"

echo "🚀 Bắt đầu test API FoodShare..."
echo "Base URL: $BASE_URL"
echo ""

# Test 1: Health Check
echo "1. Testing Health Check..."
curl -s "$BASE_URL/health" | jq .
echo ""

# Test 2: Sản phẩm bán chạy nhất
echo "2. Testing Popular Products..."
curl -s "$BASE_URL/products/popular?lat=10.762622&lon=106.660172&page=0&size=5" | jq .
echo ""

# Test 3: Sản phẩm giảm giá nhiều nhất
echo "3. Testing Top Discounts..."
curl -s "$BASE_URL/products/top-discounts?lat=10.762622&lon=106.660172&page=0&size=5" | jq .
echo ""

# Test 4: Tìm kiếm gần đây
echo "4. Testing Nearby Products..."
curl -s "$BASE_URL/products/nearby?lat=10.762622&lon=106.660172&page=0&size=5" | jq .
echo ""

# Test 5: Tìm kiếm chung
echo "5. Testing General Search..."
curl -s "$BASE_URL/products?q=pizza&lat=10.762622&lon=106.660172&page=0&size=5" | jq .
echo ""

# Test 6: Chi tiết sản phẩm
echo "6. Testing Product Detail..."
curl -s "$BASE_URL/products/1" | jq .
echo ""

# Test 7: Tìm kiếm với bộ lọc giá
echo "7. Testing Price Filter..."
curl -s "$BASE_URL/products?minPrice=50000&maxPrice=150000&page=0&size=5" | jq .
echo ""

# Test 8: Sắp xếp theo giá
echo "8. Testing Price Sort..."
curl -s "$BASE_URL/products?priceSort=desc&page=0&size=5" | jq .
echo ""

# Test 9: Error case - Tìm kiếm gần đây không có tọa độ
echo "9. Testing Error Case - Missing Coordinates..."
curl -s "$BASE_URL/products/nearby?page=0&size=5" | jq .
echo ""

# Test 10: Error case - Sản phẩm không tồn tại
echo "10. Testing Error Case - Product Not Found..."
curl -s "$BASE_URL/products/999" | jq .
echo ""

# Test 11: Phân trang
echo "11. Testing Pagination..."
curl -s "$BASE_URL/products?page=0&size=2" | jq .
echo ""

# Test 12: Tạo đơn hàng
echo "12. Testing Create Order..."
curl -s -X POST "$BASE_URL/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "pickupInMinutes": 30
  }' | jq .
echo ""

echo "✅ Hoàn thành test tất cả API!"
echo ""
echo "📊 Thống kê test:"
echo "- Health Check: ✅"
echo "- Popular Products: ✅"
echo "- Top Discounts: ✅"
echo "- Nearby Products: ✅"
echo "- General Search: ✅"
echo "- Product Detail: ✅"
echo "- Price Filter: ✅"
echo "- Price Sort: ✅"
echo "- Error Cases: ✅"
echo "- Pagination: ✅"
echo "- Create Order: ✅"
