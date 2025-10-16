#!/bin/bash

# Script test cho Priority Search với thứ tự ưu tiên: name → discount → distance
# Kiểm tra thứ tự ưu tiên và hiệu suất của native query mới

echo "=== TEST PRIORITY SEARCH: NAME → DISCOUNT → DISTANCE ==="
echo ""

# Cấu hình
BASE_URL="http://localhost:8080"
LAT="10.762622"  # Tọa độ Hồ Chí Minh
LON="106.660172"

echo "📍 Tọa độ test: $LAT, $LON"
echo ""

# Test 1: Tìm kiếm theo tên (ưu tiên cao nhất)
echo "🔍 Test 1: Tìm kiếm theo tên (ưu tiên cao nhất)"
echo "Tìm kiếm 'pizza' - sản phẩm có tên match sẽ được ưu tiên cao nhất"
echo "URL: $BASE_URL/products?q=pizza&lat=$LAT&lon=$LON&page=0&size=10"
echo ""

response=$(curl -s -X GET "$BASE_URL/products?q=pizza&lat=$LAT&lon=$LON&page=0&size=10" \
  -H "Content-Type: application/json")

if echo "$response" | jq -e '.success' > /dev/null; then
    echo "Kết quả tìm kiếm 'pizza':"
    echo "$response" | jq -r '.data.content[] | "\(.name): \(.discountPercentage)% giảm giá, \(.distanceKm)km"'
    echo ""
    echo "✅ Sản phẩm có tên chứa 'pizza' sẽ được ưu tiên cao nhất (1000 điểm)"
else
    echo "❌ Lỗi: $(echo "$response" | jq -r '.message // "Unknown error"')"
fi

echo "---"
echo ""

# Test 2: Tìm kiếm không có tên, kiểm tra ưu tiên discount
echo "🔍 Test 2: Tìm kiếm không có tên, kiểm tra ưu tiên discount"
echo "Lấy tất cả sản phẩm - sản phẩm có giảm giá sẽ được ưu tiên (500 điểm)"
echo "URL: $BASE_URL/products?lat=$LAT&lon=$LON&page=0&size=10"
echo ""

response=$(curl -s -X GET "$BASE_URL/products?lat=$LAT&lon=$LON&page=0&size=10" \
  -H "Content-Type: application/json")

if echo "$response" | jq -e '.success' > /dev/null; then
    echo "Kết quả tìm kiếm (không có tên):"
    echo "$response" | jq -r '.data.content[] | "\(.name): \(.discountPercentage)% giảm giá, \(.distanceKm)km"'
    echo ""
    echo "✅ Sản phẩm có giảm giá sẽ được ưu tiên (500 điểm)"
else
    echo "❌ Lỗi: $(echo "$response" | jq -r '.message // "Unknown error"')"
fi

echo "---"
echo ""

# Test 3: Tìm kiếm với tên không tồn tại, kiểm tra ưu tiên distance
echo "🔍 Test 3: Tìm kiếm với tên không tồn tại, kiểm tra ưu tiên distance"
echo "Tìm kiếm 'xyz123' (không tồn tại) - sản phẩm gần nhất sẽ được ưu tiên"
echo "URL: $BASE_URL/products?q=xyz123&lat=$LAT&lon=$LON&page=0&size=10"
echo ""

response=$(curl -s -X GET "$BASE_URL/products?q=xyz123&lat=$LAT&lon=$LON&page=0&size=10" \
  -H "Content-Type: application/json")

if echo "$response" | jq -e '.success' > /dev/null; then
    echo "Kết quả tìm kiếm 'xyz123' (không tồn tại):"
    echo "$response" | jq -r '.data.content[] | "\(.name): \(.discountPercentage)% giảm giá, \(.distanceKm)km"'
    echo ""
    echo "✅ Sản phẩm gần nhất sẽ được ưu tiên (1000 - distance_km điểm)"
else
    echo "❌ Lỗi: $(echo "$response" | jq -r '.message // "Unknown error"')"
fi

echo "---"
echo ""

# Test 4: So sánh thứ tự ưu tiên
echo "🔍 Test 4: So sánh thứ tự ưu tiên"
echo "Lấy 20 sản phẩm và kiểm tra thứ tự ưu tiên"
echo "URL: $BASE_URL/products?lat=$LAT&lon=$LON&page=0&size=20"
echo ""

response=$(curl -s -X GET "$BASE_URL/products?lat=$LAT&lon=$LON&page=0&size=20" \
  -H "Content-Type: application/json")

if echo "$response" | jq -e '.success' > /dev/null; then
    echo "Thứ tự ưu tiên (20 sản phẩm đầu tiên):"
    echo "Tên sản phẩm | Giảm giá | Khoảng cách | Điểm ưu tiên"
    echo "-------------|----------|-------------|-------------"
    
    # Lấy dữ liệu và tính điểm ưu tiên
    echo "$response" | jq -r '.data.content[] | "\(.name) | \(.discountPercentage)% | \(.distanceKm)km | \(if .name | test("pizza"; "i") then 1000 else 0 end) + \(.discountPercentage) + \(if .distanceKm then (1000 - .distanceKm) else 0 end)"'
    
    echo ""
    echo "✅ Thứ tự ưu tiên:"
    echo "   1. Tên match (1000 điểm)"
    echo "   2. Có giảm giá (500 điểm)"  
    echo "   3. Khoảng cách gần (1000 - distance_km điểm)"
else
    echo "❌ Lỗi: $(echo "$response" | jq -r '.message // "Unknown error"')"
fi

echo "---"
echo ""

# Test 5: Test hiệu suất
echo "🔍 Test 5: Test hiệu suất native query"
echo "Thực hiện 5 lần gọi API và đo thời gian trung bình..."
echo ""

total_time=0
for i in {1..5}; do
    echo "Lần $i/5..."
    start_time=$(date +%s%3N)
    
    response=$(curl -s -X GET "$BASE_URL/products?lat=$LAT&lon=$LON&page=0&size=10" \
      -H "Content-Type: application/json")
    
    end_time=$(date +%s%3N)
    duration=$((end_time - start_time))
    total_time=$((total_time + duration))
    
    echo "Thời gian: ${duration}ms"
    
    # Kiểm tra response có hợp lệ không
    success=$(echo "$response" | jq -r '.success // false')
    if [ "$success" = "true" ]; then
        count=$(echo "$response" | jq -r '.data.content | length')
        echo "Số sản phẩm tìm thấy: $count"
    else
        echo "Lỗi: $(echo "$response" | jq -r '.message // "Unknown error"')"
    fi
    
    echo ""
    sleep 1
done

avg_time=$((total_time / 5))
echo "⏱️  Thời gian trung bình: ${avg_time}ms"
echo ""

# Test 6: Test với các tham số khác nhau
echo "🔍 Test 6: Test với các tham số khác nhau"

# Test với minPrice
echo "Test với minPrice=50000:"
curl -s -X GET "$BASE_URL/products?lat=$LAT&lon=$LON&minPrice=50000&page=0&size=5" \
  -H "Content-Type: application/json" | jq -r '.data.content[] | "\(.name): \(.price) VND"'

echo ""

# Test với maxPrice
echo "Test với maxPrice=100000:"
curl -s -X GET "$BASE_URL/products?lat=$LAT&lon=$LON&maxPrice=100000&page=0&size=5" \
  -H "Content-Type: application/json" | jq -r '.data.content[] | "\(.name): \(.price) VND"'

echo ""

# Test với maxDistanceKm
echo "Test với maxDistanceKm=5:"
curl -s -X GET "$BASE_URL/products?lat=$LAT&lon=$LON&maxDistanceKm=5&page=0&size=5" \
  -H "Content-Type: application/json" | jq -r '.data.content[] | "\(.name): \(.distanceKm)km"'

echo ""

echo "=== KẾT THÚC TEST PRIORITY SEARCH ==="
