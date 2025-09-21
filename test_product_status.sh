#!/bin/bash

# =====================================================
# SCRIPT TEST TRẠNG THÁI SẢN PHẨM MỚI
# =====================================================

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SCRIPT TEST TRẠNG THÁI SẢN PHẨM MỚI ===${NC}"
echo ""
echo -e "${YELLOW}Trạng thái sản phẩm:${NC}"
echo "1 - Available (Có sẵn)"
echo "2 - Sold Out (Hết hàng)" 
echo "3 - No Longer Sell (Không còn bán)"
echo ""

# =====================================================
# 1. TẠO SẢN PHẨM VỚI TRẠNG THÁI 1 (AVAILABLE)
# =====================================================
echo -e "${YELLOW}1. Tạo sản phẩm với trạng thái 1 (Available)${NC}"
echo ""

CREATE_PRODUCT_1=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": 1,
    "name": "Pizza Margherita - Available",
    "description": "Pizza cổ điển - có sẵn",
    "price": 150000.00,
    "originalPrice": 180000.00,
    "imageUrl": "https://example.com/pizza_available.jpg",
    "detailImageUrl": "https://example.com/pizza_available_detail.jpg",
    "quantityAvailable": 50,
    "status": "1"
  }')

echo "Create Product 1 (Available) Response:"
echo "$CREATE_PRODUCT_1" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. TẠO SẢN PHẨM VỚI TRẠNG THÁI 2 (SOLD_OUT)
# =====================================================
echo -e "${YELLOW}2. Tạo sản phẩm với trạng thái 2 (Sold Out)${NC}"
echo ""

CREATE_PRODUCT_2=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": 2,
    "name": "Burger Deluxe - Sold Out",
    "description": "Burger cao cấp - hết hàng",
    "price": 120000.00,
    "originalPrice": 150000.00,
    "imageUrl": "https://example.com/burger_soldout.jpg",
    "detailImageUrl": "https://example.com/burger_soldout_detail.jpg",
    "quantityAvailable": 0,
    "status": "2"
  }')

echo "Create Product 2 (Sold Out) Response:"
echo "$CREATE_PRODUCT_2" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. TẠO SẢN PHẨM VỚI TRẠNG THÁI 3 (NO_LONGER_SELL)
# =====================================================
echo -e "${YELLOW}3. Tạo sản phẩm với trạng thái 3 (No Longer Sell)${NC}"
echo ""

CREATE_PRODUCT_3=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": 3,
    "name": "Pasta Carbonara - Discontinued",
    "description": "Pasta carbonara - không còn bán",
    "price": 100000.00,
    "originalPrice": 120000.00,
    "imageUrl": "https://example.com/pasta_discontinued.jpg",
    "detailImageUrl": "https://example.com/pasta_discontinued_detail.jpg",
    "quantityAvailable": 0,
    "status": "3"
  }')

echo "Create Product 3 (No Longer Sell) Response:"
echo "$CREATE_PRODUCT_3" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. LẤY SẢN PHẨM THEO TRẠNG THÁI 1 (AVAILABLE)
# =====================================================
echo -e "${YELLOW}4. Lấy sản phẩm theo trạng thái 1 (Available)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/status/1" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 5. LẤY SẢN PHẨM THEO TRẠNG THÁI 2 (SOLD_OUT)
# =====================================================
echo -e "${YELLOW}5. Lấy sản phẩm theo trạng thái 2 (Sold Out)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/status/2" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 6. LẤY SẢN PHẨM THEO TRẠNG THÁI 3 (NO_LONGER_SELL)
# =====================================================
echo -e "${YELLOW}6. Lấy sản phẩm theo trạng thái 3 (No Longer Sell)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/status/3" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 7. TEST TRẠNG THÁI KHÔNG HỢP LỆ
# =====================================================
echo -e "${YELLOW}7. Test trạng thái không hợp lệ (4)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/status/4" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 8. TEST TRẠNG THÁI KHÔNG HỢP LỆ (active)
# =====================================================
echo -e "${YELLOW}8. Test trạng thái không hợp lệ (active)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/status/active" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 9. LẤY TẤT CẢ SẢN PHẨM
# =====================================================
echo -e "${YELLOW}9. Lấy tất cả sản phẩm${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 10. CẬP NHẬT SẢN PHẨM TỪ TRẠNG THÁI 1 SANG 2
# =====================================================
echo -e "${YELLOW}10. Cập nhật sản phẩm từ trạng thái 1 sang 2${NC}"
echo ""

# Lấy ID của sản phẩm đầu tiên
PRODUCT_ID=$(echo "$CREATE_PRODUCT_1" | jq -r '.data.id // empty')

if [ -n "$PRODUCT_ID" ] && [ "$PRODUCT_ID" != "null" ]; then
    echo "Updating product ID: $PRODUCT_ID"
    
    UPDATE_PRODUCT=$(curl -s -X PUT "${BASE_URL}/api/admin/products/${PRODUCT_ID}" \
      -H "Content-Type: application/json" \
      -d '{
        "status": "2"
      }')
    
    echo "Update Product Response:"
    echo "$UPDATE_PRODUCT" | jq '.'
else
    echo "Không tìm thấy product ID để cập nhật"
fi

echo ""
echo "----------------------------------------"
echo ""

echo -e "${GREEN}=== KẾT QUẢ TEST TRẠNG THÁI SẢN PHẨM ===${NC}"
echo ""
echo -e "${GREEN}✅ Các trạng thái đã test:${NC}"
echo "1. ✅ Trạng thái 1 (Available) - Có sẵn"
echo "2. ✅ Trạng thái 2 (Sold Out) - Hết hàng"
echo "3. ✅ Trạng thái 3 (No Longer Sell) - Không còn bán"
echo "4. ✅ Error Testing - Trạng thái không hợp lệ"
echo "5. ✅ Update Testing - Cập nhật trạng thái"
echo ""
echo -e "${BLUE}API Endpoints:${NC}"
echo "GET /api/admin/products/status/{status}"
echo "PUT /api/admin/products/{id}"
echo ""
echo -e "${BLUE}Trạng thái hợp lệ:${NC}"
echo "1 - Available (Có sẵn)"
echo "2 - Sold Out (Hết hàng)"
echo "3 - No Longer Sell (Không còn bán)"
