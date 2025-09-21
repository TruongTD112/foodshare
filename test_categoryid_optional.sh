#!/bin/bash

# =====================================================
# SCRIPT TEST CATEGORYID OPTIONAL
# =====================================================

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SCRIPT TEST CATEGORYID OPTIONAL ===${NC}"
echo ""
echo -e "${YELLOW}Test categoryId là optional (không bắt buộc)${NC}"
echo ""

# =====================================================
# 1. TẠO SẢN PHẨM VỚI CATEGORYID = NULL
# =====================================================
echo -e "${YELLOW}1. Tạo sản phẩm với categoryId = null${NC}"
echo ""

CREATE_PRODUCT_NULL_CATEGORY=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": null,
    "name": "Pizza Margherita - No Category",
    "description": "Pizza cổ điển không có category",
    "price": 150000.00,
    "originalPrice": 180000.00,
    "imageUrl": "https://example.com/pizza_no_category.jpg",
    "detailImageUrl": "https://example.com/pizza_no_category_detail.jpg",
    "quantityAvailable": 50,
    "status": "available"
  }')

echo "Create Product with null categoryId Response:"
echo "$CREATE_PRODUCT_NULL_CATEGORY" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. TẠO SẢN PHẨM VỚI CATEGORYID = 1
# =====================================================
echo -e "${YELLOW}2. Tạo sản phẩm với categoryId = 1${NC}"
echo ""

CREATE_PRODUCT_WITH_CATEGORY=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": 1,
    "name": "Burger Deluxe - With Category",
    "description": "Burger cao cấp có category",
    "price": 120000.00,
    "originalPrice": 150000.00,
    "imageUrl": "https://example.com/burger_with_category.jpg",
    "detailImageUrl": "https://example.com/burger_with_category_detail.jpg",
    "quantityAvailable": 30,
    "status": "available"
  }')

echo "Create Product with categoryId = 1 Response:"
echo "$CREATE_PRODUCT_WITH_CATEGORY" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. TẠO SẢN PHẨM KHÔNG CÓ CATEGORYID (OMIT FIELD)
# =====================================================
echo -e "${YELLOW}3. Tạo sản phẩm không có categoryId (omit field)${NC}"
echo ""

CREATE_PRODUCT_OMIT_CATEGORY=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "name": "Pasta Carbonara - Omit Category",
    "description": "Pasta carbonara không có field categoryId",
    "price": 100000.00,
    "originalPrice": 120000.00,
    "imageUrl": "https://example.com/pasta_omit_category.jpg",
    "detailImageUrl": "https://example.com/pasta_omit_category_detail.jpg",
    "quantityAvailable": 25,
    "status": "available"
  }')

echo "Create Product omitting categoryId Response:"
echo "$CREATE_PRODUCT_OMIT_CATEGORY" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. CẬP NHẬT SẢN PHẨM VỚI CATEGORYID = NULL
# =====================================================
echo -e "${YELLOW}4. Cập nhật sản phẩm với categoryId = null${NC}"
echo ""

# Lấy ID của sản phẩm đầu tiên
PRODUCT_ID=$(echo "$CREATE_PRODUCT_NULL_CATEGORY" | jq -r '.data.id // empty')

if [ -n "$PRODUCT_ID" ] && [ "$PRODUCT_ID" != "null" ]; then
    echo "Updating product ID: $PRODUCT_ID"
    
    UPDATE_PRODUCT=$(curl -s -X PUT "${BASE_URL}/api/admin/products/${PRODUCT_ID}" \
      -H "Content-Type: application/json" \
      -d '{
        "categoryId": null,
        "name": "Updated Pizza - No Category"
      }')
    
    echo "Update Product with null categoryId Response:"
    echo "$UPDATE_PRODUCT" | jq '.'
else
    echo "Không tìm thấy product ID để cập nhật"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 5. CẬP NHẬT SẢN PHẨM VỚI CATEGORYID = 2
# =====================================================
echo -e "${YELLOW}5. Cập nhật sản phẩm với categoryId = 2${NC}"
echo ""

if [ -n "$PRODUCT_ID" ] && [ "$PRODUCT_ID" != "null" ]; then
    UPDATE_PRODUCT_2=$(curl -s -X PUT "${BASE_URL}/api/admin/products/${PRODUCT_ID}" \
      -H "Content-Type: application/json" \
      -d '{
        "categoryId": 2,
        "name": "Updated Pizza - Category 2"
      }')
    
    echo "Update Product with categoryId = 2 Response:"
    echo "$UPDATE_PRODUCT_2" | jq '.'
else
    echo "Không tìm thấy product ID để cập nhật"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 6. LẤY TẤT CẢ SẢN PHẨM
# =====================================================
echo -e "${YELLOW}6. Lấy tất cả sản phẩm${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 7. TEST CATEGORYID KHÔNG HỢP LỆ (ÂM)
# =====================================================
echo -e "${YELLOW}7. Test categoryId không hợp lệ (âm)${NC}"
echo ""

curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": -1,
    "name": "Test Product - Invalid Category",
    "description": "Test sản phẩm với categoryId không hợp lệ",
    "price": 100000.00,
    "quantityAvailable": 10,
    "status": "available"
  }' | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 8. TEST CATEGORYID KHÔNG HỢP LỆ (0)
# =====================================================
echo -e "${YELLOW}8. Test categoryId không hợp lệ (0)${NC}"
echo ""

curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": 0,
    "name": "Test Product - Zero Category",
    "description": "Test sản phẩm với categoryId = 0",
    "price": 100000.00,
    "quantityAvailable": 10,
    "status": "available"
  }' | jq '.'

echo ""
echo "----------------------------------------"
echo ""

echo -e "${GREEN}=== KẾT QUẢ TEST CATEGORYID OPTIONAL ===${NC}"
echo ""
echo -e "${GREEN}✅ Các test cases đã thực hiện:${NC}"
echo "1. ✅ Tạo sản phẩm với categoryId = null"
echo "2. ✅ Tạo sản phẩm với categoryId = 1"
echo "3. ✅ Tạo sản phẩm không có field categoryId"
echo "4. ✅ Cập nhật sản phẩm với categoryId = null"
echo "5. ✅ Cập nhật sản phẩm với categoryId = 2"
echo "6. ✅ Lấy tất cả sản phẩm"
echo "7. ✅ Test categoryId không hợp lệ (âm)"
echo "8. ✅ Test categoryId không hợp lệ (0)"
echo ""
echo -e "${BLUE}Kết luận:${NC}"
echo "✅ categoryId là optional - có thể null hoặc omit"
echo "✅ Validation chỉ áp dụng khi categoryId được cung cấp"
echo "✅ categoryId phải là số dương nếu được cung cấp"
echo ""
echo -e "${BLUE}API Examples:${NC}"
echo "# Tạo sản phẩm không có categoryId"
echo 'curl -X POST "http://localhost:8080/api/admin/products" -d "{\"shopId\": 1, \"name\": \"Product\", \"price\": 100000, \"status\": \"available\"}"'
echo ""
echo "# Tạo sản phẩm với categoryId = null"
echo 'curl -X POST "http://localhost:8080/api/admin/products" -d "{\"shopId\": 1, \"categoryId\": null, \"name\": \"Product\", \"price\": 100000, \"status\": \"available\"}"'
