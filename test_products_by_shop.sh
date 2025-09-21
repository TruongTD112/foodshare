#!/bin/bash

# =====================================================
# SCRIPT TEST LẤY SẢN PHẨM THEO SHOP ID
# =====================================================

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SCRIPT TEST LẤY SẢN PHẨM THEO SHOP ID ===${NC}"
echo ""

# =====================================================
# 1. TẠO SẢN PHẨM CHO SHOP 1
# =====================================================
echo -e "${YELLOW}1. Tạo sản phẩm cho Shop 1${NC}"
echo ""

CREATE_PRODUCT_1=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": null,
    "name": "Pizza Margherita Shop 1",
    "description": "Pizza cổ điển từ shop 1",
    "price": 150000.00,
    "originalPrice": 180000.00,
    "imageUrl": "https://example.com/pizza_shop1.jpg",
    "detailImageUrl": "https://example.com/pizza_shop1_detail.jpg",
    "quantityAvailable": 50,
    "status": "available"
  }')

echo "Create Product 1 Response:"
echo "$CREATE_PRODUCT_1" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. TẠO SẢN PHẨM CHO SHOP 1 (THÊM)
# =====================================================
echo -e "${YELLOW}2. Tạo sản phẩm thứ 2 cho Shop 1${NC}"
echo ""

CREATE_PRODUCT_2=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": null,
    "name": "Burger Deluxe Shop 1",
    "description": "Burger cao cấp từ shop 1",
    "price": 120000.00,
    "originalPrice": 150000.00,
    "imageUrl": "https://example.com/burger_shop1.jpg",
    "detailImageUrl": "https://example.com/burger_shop1_detail.jpg",
    "quantityAvailable": 30,
    "status": "available"
  }')

echo "Create Product 2 Response:"
echo "$CREATE_PRODUCT_2" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. TẠO SẢN PHẨM CHO SHOP 2
# =====================================================
echo -e "${YELLOW}3. Tạo sản phẩm cho Shop 2${NC}"
echo ""

CREATE_PRODUCT_3=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 2,
    "categoryId": null,
    "name": "Pizza Margherita Shop 2",
    "description": "Pizza cổ điển từ shop 2",
    "price": 160000.00,
    "originalPrice": 200000.00,
    "imageUrl": "https://example.com/pizza_shop2.jpg",
    "detailImageUrl": "https://example.com/pizza_shop2_detail.jpg",
    "quantityAvailable": 40,
    "status": "available"
  }')

echo "Create Product 3 Response:"
echo "$CREATE_PRODUCT_3" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. LẤY SẢN PHẨM THEO SHOP ID 1
# =====================================================
echo -e "${YELLOW}4. Lấy sản phẩm theo Shop ID 1${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/shop/1" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 5. LẤY SẢN PHẨM THEO SHOP ID 2
# =====================================================
echo -e "${YELLOW}5. Lấy sản phẩm theo Shop ID 2${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/shop/2" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 6. LẤY SẢN PHẨM THEO SHOP ID 3 (KHÔNG TỒN TẠI)
# =====================================================
echo -e "${YELLOW}6. Lấy sản phẩm theo Shop ID 3 (không tồn tại)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/shop/3" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 7. LẤY TẤT CẢ SẢN PHẨM
# =====================================================
echo -e "${YELLOW}7. Lấy tất cả sản phẩm${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""


echo -e "${GREEN}=== KẾT QUẢ TEST LẤY SẢN PHẨM THEO SHOP ID ===${NC}"
echo ""
echo -e "${GREEN}✅ Các APIs đã test:${NC}"
echo "1. ✅ POST /api/admin/products - Tạo sản phẩm"
echo "2. ✅ GET /api/admin/products/shop/{shopId} - Lấy sản phẩm theo shop"
echo "3. ✅ GET /api/admin/products - Lấy tất cả sản phẩm"
echo "4. ✅ Error Testing - Test shop không tồn tại"
echo ""
echo -e "${BLUE}API Endpoint:${NC}"
echo "GET /api/admin/products/shop/{shopId}"
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
      "name": "Pizza Margherita Shop 1",
      "description": "Pizza cổ điển từ shop 1",
      "price": 150000.00,
      "originalPrice": 180000.00,
      "imageUrl": "https://example.com/pizza_shop1.jpg",
      "detailImageUrl": "https://example.com/pizza_shop1_detail.jpg",
      "quantityAvailable": 50,
      "quantityPending": 0,
      "status": "available"
    }
  ]
}'
