#!/bin/bash

# =====================================================
# SCRIPT TEST LẤY SẢN PHẨM THEO SHOP ID VÀ TRẠNG THÁI
# =====================================================

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SCRIPT TEST LẤY SẢN PHẨM THEO SHOP ID VÀ TRẠNG THÁI ===${NC}"
echo ""
echo -e "${YELLOW}Trạng thái sản phẩm:${NC}"
echo "available - Available (Có sẵn)"
echo "sold_out - Sold Out (Hết hàng)" 
echo "no_longer_sell - No Longer Sell (Không còn bán)"
echo ""

# =====================================================
# 1. TẠO SẢN PHẨM CHO SHOP 1 - TRẠNG THÁI 1 (AVAILABLE)
# =====================================================
echo -e "${YELLOW}1. Tạo sản phẩm cho Shop 1 - Trạng thái available (Available)${NC}"
echo ""

CREATE_PRODUCT_1=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": null,
    "name": "Pizza Margherita Shop 1 - Available",
    "description": "Pizza cổ điển từ shop 1 - có sẵn",
    "price": 150000.00,
    "originalPrice": 180000.00,
    "imageUrl": "https://example.com/pizza_shop1_available.jpg",
    "detailImageUrl": "https://example.com/pizza_shop1_available_detail.jpg",
    "quantityAvailable": 50,
    "status": "available"
  }')

echo "Create Product 1 (Shop 1, Available) Response:"
echo "$CREATE_PRODUCT_1" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. TẠO SẢN PHẨM CHO SHOP 1 - TRẠNG THÁI 2 (SOLD_OUT)
# =====================================================
echo -e "${YELLOW}2. Tạo sản phẩm cho Shop 1 - Trạng thái sold_out (Sold Out)${NC}"
echo ""

CREATE_PRODUCT_2=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": null,
    "name": "Burger Deluxe Shop 1 - Sold Out",
    "description": "Burger cao cấp từ shop 1 - hết hàng",
    "price": 120000.00,
    "originalPrice": 150000.00,
    "imageUrl": "https://example.com/burger_shop1_soldout.jpg",
    "detailImageUrl": "https://example.com/burger_shop1_soldout_detail.jpg",
    "quantityAvailable": 0,
    "status": "sold_out"
  }')

echo "Create Product 2 (Shop 1, Sold Out) Response:"
echo "$CREATE_PRODUCT_2" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. TẠO SẢN PHẨM CHO SHOP 1 - TRẠNG THÁI 3 (NO_LONGER_SELL)
# =====================================================
echo -e "${YELLOW}3. Tạo sản phẩm cho Shop 1 - Trạng thái no_longer_sell (No Longer Sell)${NC}"
echo ""

CREATE_PRODUCT_3=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": null,
    "name": "Pasta Carbonara Shop 1 - Discontinued",
    "description": "Pasta carbonara từ shop 1 - không còn bán",
    "price": 100000.00,
    "originalPrice": 120000.00,
    "imageUrl": "https://example.com/pasta_shop1_discontinued.jpg",
    "detailImageUrl": "https://example.com/pasta_shop1_discontinued_detail.jpg",
    "quantityAvailable": 0,
    "status": "no_longer_sell"
  }')

echo "Create Product 3 (Shop 1, No Longer Sell) Response:"
echo "$CREATE_PRODUCT_3" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. TẠO SẢN PHẨM CHO SHOP 2 - TRẠNG THÁI 1 (AVAILABLE)
# =====================================================
echo -e "${YELLOW}4. Tạo sản phẩm cho Shop 2 - Trạng thái available (Available)${NC}"
echo ""

CREATE_PRODUCT_4=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 2,
    "categoryId": null,
    "name": "Pizza Margherita Shop 2 - Available",
    "description": "Pizza cổ điển từ shop 2 - có sẵn",
    "price": 160000.00,
    "originalPrice": 200000.00,
    "imageUrl": "https://example.com/pizza_shop2_available.jpg",
    "detailImageUrl": "https://example.com/pizza_shop2_available_detail.jpg",
    "quantityAvailable": 40,
    "status": "available"
  }')

echo "Create Product 4 (Shop 2, Available) Response:"
echo "$CREATE_PRODUCT_4" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 5. LẤY SẢN PHẨM SHOP 1 - TRẠNG THÁI 1 (AVAILABLE)
# =====================================================
echo -e "${YELLOW}5. Lấy sản phẩm Shop 1 - Trạng thái available (Available)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/shop/1/status/available" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 6. LẤY SẢN PHẨM SHOP 1 - TRẠNG THÁI 2 (SOLD_OUT)
# =====================================================
echo -e "${YELLOW}6. Lấy sản phẩm Shop 1 - Trạng thái sold_out (Sold Out)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/shop/1/status/sold_out" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 7. LẤY SẢN PHẨM SHOP 1 - TRẠNG THÁI 3 (NO_LONGER_SELL)
# =====================================================
echo -e "${YELLOW}7. Lấy sản phẩm Shop 1 - Trạng thái no_longer_sell (No Longer Sell)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/shop/1/status/no_longer_sell" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 8. LẤY SẢN PHẨM SHOP 2 - TRẠNG THÁI 1 (AVAILABLE)
# =====================================================
echo -e "${YELLOW}8. Lấy sản phẩm Shop 2 - Trạng thái available (Available)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/shop/2/status/available" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 9. LẤY SẢN PHẨM SHOP 2 - TRẠNG THÁI 2 (SOLD_OUT) - KHÔNG CÓ
# =====================================================
echo -e "${YELLOW}9. Lấy sản phẩm Shop 2 - Trạng thái sold_out (Sold Out) - Không có${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/shop/2/status/sold_out" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 10. TEST SHOP KHÔNG TỒN TẠI
# =====================================================
echo -e "${YELLOW}10. Test Shop không tồn tại (Shop 999)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/shop/999/status/available" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 11. TEST TRẠNG THÁI KHÔNG HỢP LỆ
# =====================================================
echo -e "${YELLOW}11. Test trạng thái không hợp lệ (invalid)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/shop/1/status/invalid" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 12. TEST TRẠNG THÁI KHÔNG HỢP LỆ (active)
# =====================================================
echo -e "${YELLOW}12. Test trạng thái không hợp lệ (active)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/shop/1/status/active" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 13. LẤY TẤT CẢ SẢN PHẨM CỦA SHOP 1
# =====================================================
echo -e "${YELLOW}13. Lấy tất cả sản phẩm của Shop 1${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/shop/1" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""


echo -e "${GREEN}=== KẾT QUẢ TEST LẤY SẢN PHẨM THEO SHOP ID VÀ TRẠNG THÁI ===${NC}"
echo ""
echo -e "${GREEN}✅ Các APIs đã test:${NC}"
echo "1. ✅ POST /api/admin/products - Tạo sản phẩm"
echo "2. ✅ GET /api/admin/products/shop/{shopId}/status/{status} - Lấy sản phẩm theo shop và trạng thái"
echo "3. ✅ GET /api/admin/products/shop/{shopId} - Lấy sản phẩm theo shop"
echo "4. ✅ Error Testing - Test shop không tồn tại"
echo "5. ✅ Error Testing - Test trạng thái không hợp lệ"
echo ""
echo -e "${BLUE}API Endpoint mới:${NC}"
echo "GET /api/admin/products/shop/{shopId}/status/{status}"
echo ""
echo -e "${BLUE}Trạng thái hợp lệ:${NC}"
echo "available - Available (Có sẵn)"
echo "sold_out - Sold Out (Hết hàng)"
echo "no_longer_sell - No Longer Sell (Không còn bán)"
echo ""
echo -e "${BLUE}Response Format:${NC}"
echo '{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": [
    {
      "id": 1,
      "shopId": 1,
      "categoryId": null,
      "name": "Pizza Margherita Shop 1 - Available",
      "description": "Pizza cổ điển từ shop 1 - có sẵn",
      "price": 150000.00,
      "originalPrice": 180000.00,
      "imageUrl": "https://example.com/pizza_shop1_available.jpg",
      "detailImageUrl": "https://example.com/pizza_shop1_available_detail.jpg",
      "quantityAvailable": 50,
      "quantityPending": 0,
      "status": "available"
    }
  ]
}'
