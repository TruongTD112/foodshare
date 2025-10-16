#!/bin/bash

# Script so sánh hiệu suất giữa implementation cũ và mới
# Cần có 2 version khác nhau của API để so sánh

echo "=== SO SÁNH HIỆU SUẤT NEARBY SEARCH ==="
echo ""

# Cấu hình
BASE_URL="http://localhost:8080"
LAT="10.762622"
LON="106.660172"
ITERATIONS=10

echo "📍 Tọa độ test: $LAT, $LON"
echo "🔄 Số lần test: $ITERATIONS"
echo ""

# Function để đo thời gian API
measure_api_time() {
    local url="$1"
    local description="$2"
    
    echo "🔍 Testing: $description"
    echo "URL: $url"
    
    total_time=0
    success_count=0
    error_count=0
    
    for i in $(seq 1 $ITERATIONS); do
        start_time=$(date +%s%3N)
        
        response=$(curl -s -X GET "$url" \
          -H "Content-Type: application/json" \
          -w "\n%{http_code}")
        
        end_time=$(date +%s%3N)
        duration=$((end_time - start_time))
        
        # Tách response body và status code
        http_code=$(echo "$response" | tail -n1)
        response_body=$(echo "$response" | head -n -1)
        
        if [ "$http_code" = "200" ]; then
            success_count=$((success_count + 1))
            total_time=$((total_time + duration))
            
            # Lấy số sản phẩm tìm thấy
            count=$(echo "$response_body" | jq -r '.data.content | length // 0')
            echo "  Lần $i: ${duration}ms (${count} sản phẩm)"
        else
            error_count=$((error_count + 1))
            echo "  Lần $i: ERROR (HTTP $http_code)"
        fi
        
        sleep 0.5
    done
    
    if [ $success_count -gt 0 ]; then
        avg_time=$((total_time / success_count))
        echo "  ✅ Thành công: $success_count/$ITERATIONS"
        echo "  ⏱️  Thời gian trung bình: ${avg_time}ms"
    else
        echo "  ❌ Tất cả đều lỗi"
    fi
    
    if [ $error_count -gt 0 ]; then
        echo "  ⚠️  Lỗi: $error_count/$ITERATIONS"
    fi
    
    echo ""
    echo "---"
    echo ""
}

# Test 1: Native Query Implementation (mới)
echo "🚀 TEST 1: Native Query Implementation (MỚI)"
measure_api_time "$BASE_URL/products/nearby?lat=$LAT&lon=$LON&page=0&size=20" "Native Query - 20 sản phẩm"

# Test 2: Test với kích thước khác nhau
echo "🚀 TEST 2: Test với kích thước khác nhau"
measure_api_time "$BASE_URL/products/nearby?lat=$LAT&lon=$LON&page=0&size=5" "Native Query - 5 sản phẩm"
measure_api_time "$BASE_URL/products/nearby?lat=$LAT&lon=$LON&page=0&size=50" "Native Query - 50 sản phẩm"

# Test 3: Test với tọa độ khác nhau
echo "🚀 TEST 3: Test với tọa độ khác nhau"

# Hồ Chí Minh
measure_api_time "$BASE_URL/products/nearby?lat=10.762622&lon=106.660172&page=0&size=10" "Hồ Chí Minh"

# Hà Nội
measure_api_time "$BASE_URL/products/nearby?lat=21.0285&lon=105.8542&page=0&size=10" "Hà Nội"

# Đà Nẵng
measure_api_time "$BASE_URL/products/nearby?lat=16.0544&lon=108.2022&page=0&size=10" "Đà Nẵng"

# Test 4: Test phân trang
echo "🚀 TEST 4: Test phân trang"
measure_api_time "$BASE_URL/products/nearby?lat=$LAT&lon=$LON&page=0&size=10" "Trang 0"
measure_api_time "$BASE_URL/products/nearby?lat=$LAT&lon=$LON&page=1&size=10" "Trang 1"
measure_api_time "$BASE_URL/products/nearby?lat=$LAT&lon=$LON&page=2&size=10" "Trang 2"

# Test 5: Kiểm tra tính chính xác của sắp xếp
echo "🚀 TEST 5: Kiểm tra tính chính xác của sắp xếp"
echo "Lấy 20 sản phẩm và kiểm tra distanceKm có tăng dần không..."

response=$(curl -s -X GET "$BASE_URL/products/nearby?lat=$LAT&lon=$LON&page=0&size=20" \
  -H "Content-Type: application/json")

if echo "$response" | jq -e '.success' > /dev/null; then
    echo "Khoảng cách các sản phẩm (km):"
    distances=$(echo "$response" | jq -r '.data.content[] | .distanceKm // "null"')
    
    prev_distance=""
    is_sorted=true
    
    for distance in $distances; do
        if [ "$distance" != "null" ]; then
            echo "  $distance km"
            if [ -n "$prev_distance" ] && (( $(echo "$distance < $prev_distance" | bc -l) )); then
                is_sorted=false
            fi
            prev_distance="$distance"
        fi
    done
    
    if [ "$is_sorted" = true ]; then
        echo "✅ Sắp xếp đúng: khoảng cách tăng dần"
    else
        echo "❌ Sắp xếp sai: khoảng cách không tăng dần"
    fi
else
    echo "❌ Không thể lấy dữ liệu để kiểm tra sắp xếp"
fi

echo ""
echo "=== KẾT THÚC SO SÁNH ==="
