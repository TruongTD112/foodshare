#!/bin/bash

# Script test cho Native Query Nearby Search
# Kiểm tra hiệu quả của native query mới so với implementation cũ

echo "=== TEST NATIVE QUERY NEARBY SEARCH ==="
echo ""

# Cấu hình
BASE_URL="http://localhost:8080"
LAT="10.762622"  # Tọa độ Hồ Chí Minh
LON="106.660172"

echo "📍 Tọa độ test: $LAT, $LON"
echo ""

# Test 1: Tìm kiếm cơ bản với native query
echo "🔍 Test 1: Tìm kiếm cơ bản với native query"
echo "URL: $BASE_URL/products/nearby?lat=$LAT&lon=$LON&page=0&size=5"
echo ""

curl -s -X GET "$BASE_URL/products/nearby?lat=$LAT&lon=$LON&page=0&size=5" \
  -H "Content-Type: application/json" | jq '.'

echo ""
echo "---"
echo ""

# Test 2: Test với phân trang
echo "🔍 Test 2: Test với phân trang"
echo "URL: $BASE_URL/products/nearby?lat=$LAT&lon=$LON&page=0&size=3"
echo ""

curl -s -X GET "$BASE_URL/products/nearby?lat=$LAT&lon=$LON&page=0&size=3" \
  -H "Content-Type: application/json" | jq '.'

echo ""
echo "---"
echo ""

# Test 3: Test với tọa độ khác (Hà Nội)
echo "🔍 Test 3: Test với tọa độ Hà Nội"
LAT_HN="21.0285"
LON_HN="105.8542"
echo "URL: $BASE_URL/products/nearby?lat=$LAT_HN&lon=$LON_HN&page=0&size=5"
echo ""

curl -s -X GET "$BASE_URL/products/nearby?lat=$LAT_HN&lon=$LON_HN&page=0&size=5" \
  -H "Content-Type: application/json" | jq '.'

echo ""
echo "---"
echo ""

# Test 4: Test validation - thiếu tọa độ
echo "🔍 Test 4: Test validation - thiếu tọa độ"
echo "URL: $BASE_URL/products/nearby?page=0&size=5"
echo ""

curl -s -X GET "$BASE_URL/products/nearby?page=0&size=5" \
  -H "Content-Type: application/json" | jq '.'

echo ""
echo "---"
echo ""

# Test 5: Test validation - tọa độ không hợp lệ
echo "🔍 Test 5: Test validation - tọa độ không hợp lệ"
echo "URL: $BASE_URL/products/nearby?lat=999&lon=999&page=0&size=5"
echo ""

curl -s -X GET "$BASE_URL/products/nearby?lat=999&lon=999&page=0&size=5" \
  -H "Content-Type: application/json" | jq '.'

echo ""
echo "---"
echo ""

# Test 6: So sánh hiệu suất (đo thời gian)
echo "🔍 Test 6: Đo hiệu suất native query"
echo "Thực hiện 5 lần gọi API và đo thời gian trung bình..."
echo ""

total_time=0
for i in {1..5}; do
    echo "Lần $i/5..."
    start_time=$(date +%s%3N)
    
    response=$(curl -s -X GET "$BASE_URL/products/nearby?lat=$LAT&lon=$LON&page=0&size=10" \
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

# Test 7: Kiểm tra sắp xếp theo khoảng cách
echo "🔍 Test 7: Kiểm tra sắp xếp theo khoảng cách"
echo "Lấy 10 sản phẩm đầu tiên và kiểm tra distanceKm có tăng dần không"
echo ""

response=$(curl -s -X GET "$BASE_URL/products/nearby?lat=$LAT&lon=$LON&page=0&size=10" \
  -H "Content-Type: application/json")

echo "Khoảng cách các sản phẩm (km):"
echo "$response" | jq -r '.data.content[] | "\(.name): \(.distanceKm)km"' | head -10

echo ""
echo "=== KẾT THÚC TEST ==="
