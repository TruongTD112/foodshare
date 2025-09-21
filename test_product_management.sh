#!/bin/bash

# =====================================================
# SCRIPT TEST PRODUCT MANAGEMENT APIs
# =====================================================

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SCRIPT TEST PRODUCT MANAGEMENT APIs ===${NC}"
echo ""

# =====================================================
# 1. TẠO SẢN PHẨM MỚI - PIZZA MARGHERITA
# =====================================================
echo -e "${YELLOW}1. Tạo sản phẩm mới - Pizza Margherita${NC}"
echo ""

CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": 1,
    "name": "Pizza Margherita New",
    "description": "Pizza cổ điển với cà chua, mozzarella và húng quế tươi",
    "price": 150000.00,
    "originalPrice": 180000.00,
    "imageUrl": "https://example.com/pizza_margherita_new.jpg",
    "detailImageUrl": "https://example.com/pizza_margherita_detail.jpg",
    "quantityAvailable": 50,
    "status": "active"
  }')

echo "Create Product Response:"
echo "$CREATE_RESPONSE" | jq '.'

# Extract product ID
PRODUCT_ID=$(echo "$CREATE_RESPONSE" | jq -r '.data.id // empty')

if [ -n "$PRODUCT_ID" ] && [ "$PRODUCT_ID" != "null" ] && [ "$PRODUCT_ID" != "0" ]; then
    echo -e "${GREEN}✅ Sản phẩm tạo thành công - ID: $PRODUCT_ID${NC}"
else
    echo -e "${RED}❌ Không thể tạo sản phẩm${NC}"
    exit 1
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. LẤY THÔNG TIN SẢN PHẨM VỪA TẠO
# =====================================================
echo -e "${YELLOW}2. Lấy thông tin sản phẩm vừa tạo${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/$PRODUCT_ID" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. CẬP NHẬT THÔNG TIN SẢN PHẨM
# =====================================================
echo -e "${YELLOW}3. Cập nhật thông tin sản phẩm${NC}"
echo ""

UPDATE_RESPONSE=$(curl -s -X PUT "${BASE_URL}/api/admin/products/$PRODUCT_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pizza Margherita Updated",
    "description": "Pizza cổ điển đã được cập nhật với nguyên liệu cao cấp",
    "price": 160000.00,
    "originalPrice": 200000.00,
    "imageUrl": "https://example.com/pizza_margherita_updated.jpg",
    "detailImageUrl": "https://example.com/pizza_margherita_detail_updated.jpg",
    "quantityAvailable": 60,
    "status": "active"
  }')

echo "Update Product Response:"
echo "$UPDATE_RESPONSE" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. TẠO SẢN PHẨM THỨ 2 - BURGER DELUXE
# =====================================================
echo -e "${YELLOW}4. Tạo sản phẩm thứ 2 - Burger Deluxe${NC}"
echo ""

CREATE_RESPONSE_2=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 2,
    "categoryId": 2,
    "name": "Burger Deluxe New",
    "description": "Burger cao cấp với thịt bò wagyu và phô mai cheddar",
    "price": 120000.00,
    "originalPrice": 150000.00,
    "imageUrl": "https://example.com/burger_deluxe_new.jpg",
    "detailImageUrl": "https://example.com/burger_deluxe_detail.jpg",
    "quantityAvailable": 30,
    "status": "active"
  }')

echo "Create Product 2 Response:"
echo "$CREATE_RESPONSE_2" | jq '.'

# Extract product ID 2
PRODUCT_ID_2=$(echo "$CREATE_RESPONSE_2" | jq -r '.data.id // empty')

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 5. TẠO SẢN PHẨM VỚI TRẠNG THÁI OUT OF STOCK
# =====================================================
echo -e "${YELLOW}5. Tạo sản phẩm với trạng thái out of stock${NC}"
echo ""

CREATE_RESPONSE_3=$(curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": 3,
    "name": "Cafe Latte Special",
    "description": "Cafe latte đặc biệt với hương vị độc đáo",
    "price": 45000.00,
    "originalPrice": 50000.00,
    "imageUrl": "https://example.com/cafe_latte_special.jpg",
    "detailImageUrl": "https://example.com/cafe_latte_detail.jpg",
    "quantityAvailable": 0,
    "status": "out_of_stock"
  }')

echo "Create Product 3 Response:"
echo "$CREATE_RESPONSE_3" | jq '.'

# Extract product ID 3
PRODUCT_ID_3=$(echo "$CREATE_RESPONSE_3" | jq -r '.data.id // empty')

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 6. LẤY DANH SÁCH TẤT CẢ SẢN PHẨM
# =====================================================
echo -e "${YELLOW}6. Lấy danh sách tất cả sản phẩm${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 7. LẤY DANH SÁCH SẢN PHẨM THEO SHOP ID
# =====================================================
echo -e "${YELLOW}7. Lấy danh sách sản phẩm theo shop ID (shop 1)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/shop/1" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 8. LẤY DANH SÁCH SẢN PHẨM THEO TRẠNG THÁI ACTIVE
# =====================================================
echo -e "${YELLOW}8. Lấy danh sách sản phẩm theo trạng thái active${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/status/active" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 9. LẤY DANH SÁCH SẢN PHẨM THEO TRẠNG THÁI OUT OF STOCK
# =====================================================
echo -e "${YELLOW}9. Lấy danh sách sản phẩm theo trạng thái out_of_stock${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/status/out_of_stock" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 10. LẤY DANH SÁCH SẢN PHẨM THEO CATEGORY ID
# =====================================================
echo -e "${YELLOW}10. Lấy danh sách sản phẩm theo category ID (category 1)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/category/1" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 11. TEST LỖI - TẠO SẢN PHẨM THIẾU THÔNG TIN
# =====================================================
echo -e "${YELLOW}11. Test lỗi - Tạo sản phẩm thiếu thông tin${NC}"
echo ""

curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "price": 100000.00
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 12. TEST LỖI - TẠO SẢN PHẨM VỚI GIÁ GỐC NHỎ HƠN GIÁ HIỆN TẠI
# =====================================================
echo -e "${YELLOW}12. Test lỗi - Tạo sản phẩm với giá gốc nhỏ hơn giá hiện tại${NC}"
echo ""

curl -s -X POST "${BASE_URL}/api/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": 1,
    "name": "Invalid Price Product",
    "price": 200000.00,
    "originalPrice": 150000.00,
    "status": "active"
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 13. TEST LỖI - CẬP NHẬT SẢN PHẨM KHÔNG TỒN TẠI
# =====================================================
echo -e "${YELLOW}13. Test lỗi - Cập nhật sản phẩm không tồn tại${NC}"
echo ""

curl -s -X PUT "${BASE_URL}/api/admin/products/999" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Non-existent Product"
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 14. XÓA SẢN PHẨM THỨ 3 (OUT OF STOCK)
# =====================================================
if [ -n "$PRODUCT_ID_3" ] && [ "$PRODUCT_ID_3" != "null" ] && [ "$PRODUCT_ID_3" != "0" ]; then
    echo -e "${YELLOW}14. Xóa sản phẩm thứ 3 (ID: $PRODUCT_ID_3)${NC}"
    echo ""

    curl -s -X DELETE "${BASE_URL}/api/admin/products/$PRODUCT_ID_3" \
      -w "\nHTTP Status: %{http_code}\n" | jq '.'
else
    echo -e "${YELLOW}14. Không có sản phẩm thứ 3 để xóa${NC}"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 15. XÓA SẢN PHẨM CHÍNH
# =====================================================
echo -e "${YELLOW}15. Xóa sản phẩm chính (ID: $PRODUCT_ID)${NC}"
echo ""

curl -s -X DELETE "${BASE_URL}/api/admin/products/$PRODUCT_ID" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 16. KIỂM TRA SẢN PHẨM ĐÃ BỊ XÓA
# =====================================================
echo -e "${YELLOW}16. Kiểm tra sản phẩm đã bị xóa${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/products/$PRODUCT_ID" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

echo -e "${GREEN}=== KẾT QUẢ TEST PRODUCT MANAGEMENT APIs ===${NC}"
echo ""
echo -e "${GREEN}✅ Các APIs đã test:${NC}"
echo "1. ✅ POST /api/admin/products - Tạo sản phẩm"
echo "2. ✅ GET /api/admin/products/{id} - Lấy thông tin sản phẩm"
echo "3. ✅ PUT /api/admin/products/{id} - Cập nhật sản phẩm"
echo "4. ✅ GET /api/admin/products - Lấy danh sách tất cả sản phẩm"
echo "5. ✅ GET /api/admin/products/shop/{shopId} - Lấy sản phẩm theo shop"
echo "6. ✅ GET /api/admin/products/status/{status} - Lấy sản phẩm theo trạng thái"
echo "7. ✅ GET /api/admin/products/category/{categoryId} - Lấy sản phẩm theo category"
echo "8. ✅ DELETE /api/admin/products/{id} - Xóa sản phẩm"
echo "9. ✅ Error Testing - Test các trường hợp lỗi"
echo ""
echo -e "${BLUE}Response format:${NC}"
echo '{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,
    "shopId": 1,
    "categoryId": 1,
    "name": "Pizza Margherita",
    "description": "Pizza cổ điển với cà chua, mozzarella",
    "price": 150000.00,
    "originalPrice": 180000.00,
    "imageUrl": "https://example.com/pizza_margherita.jpg",
    "detailImageUrl": "https://example.com/pizza_margherita_detail.jpg",
    "quantityAvailable": 50,
    "quantityPending": 0,
    "status": "active"
  }
}'
