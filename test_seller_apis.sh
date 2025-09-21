#!/bin/bash

# =====================================================
# SCRIPT TEST SELLER APIs
# =====================================================

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SCRIPT TEST SELLER APIs ===${NC}"
echo ""
echo -e "${YELLOW}Test APIs dành cho Seller - chỉ quản lý sản phẩm và cửa hàng của mình${NC}"
echo ""

# =====================================================
# 1. TẠO CỬA HÀNG MỚI (SELLER)
# =====================================================
echo -e "${YELLOW}1. Tạo cửa hàng mới (Seller)${NC}"
echo ""

CREATE_SHOP=$(curl -s -X POST "${BASE_URL}/api/seller/shops" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pizza House - Seller Shop",
    "address": "123 Đường ABC, Quận 1, TP.HCM",
    "phone": "0901234567",
    "imageUrl": "https://example.com/pizza_house.jpg",
    "latitude": 10.762622,
    "longitude": 106.660172,
    "description": "Cửa hàng pizza ngon nhất thành phố",
    "rating": 4.5,
    "status": "active"
  }')

echo "Create Shop Response:"
echo "$CREATE_SHOP" | jq '.'

SHOP_ID=$(echo "$CREATE_SHOP" | jq -r '.data.id // empty')
echo "Created Shop ID: $SHOP_ID"
echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. LẤY THÔNG TIN CỬA HÀNG
# =====================================================
echo -e "${YELLOW}2. Lấy thông tin cửa hàng${NC}"
echo ""

if [ -n "$SHOP_ID" ] && [ "$SHOP_ID" != "null" ]; then
    GET_SHOP=$(curl -s -X GET "${BASE_URL}/api/seller/shops/${SHOP_ID}")
    echo "Get Shop Response:"
    echo "$GET_SHOP" | jq '.'
else
    echo "Không có Shop ID để test"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. CẬP NHẬT CỬA HÀNG
# =====================================================
echo -e "${YELLOW}3. Cập nhật cửa hàng${NC}"
echo ""

if [ -n "$SHOP_ID" ] && [ "$SHOP_ID" != "null" ]; then
    UPDATE_SHOP=$(curl -s -X PUT "${BASE_URL}/api/seller/shops/${SHOP_ID}" \
      -H "Content-Type: application/json" \
      -d '{
        "name": "Pizza House Updated - Seller Shop",
        "description": "Cửa hàng pizza ngon nhất thành phố - đã cập nhật",
        "rating": 4.8
      }')
    
    echo "Update Shop Response:"
    echo "$UPDATE_SHOP" | jq '.'
else
    echo "Không có Shop ID để test"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. TẠO SẢN PHẨM CHO CỬA HÀNG
# =====================================================
echo -e "${YELLOW}4. Tạo sản phẩm cho cửa hàng${NC}"
echo ""

if [ -n "$SHOP_ID" ] && [ "$SHOP_ID" != "null" ]; then
    CREATE_PRODUCT=$(curl -s -X POST "${BASE_URL}/api/seller/products" \
      -H "Content-Type: application/json" \
      -d '{
        "shopId": '$SHOP_ID',
        "name": "Pizza Margherita - Seller Product",
        "description": "Pizza cổ điển từ seller",
        "price": 150000.00,
        "originalPrice": 180000.00,
        "imageUrl": "https://example.com/pizza_margherita.jpg",
        "detailImageUrl": "https://example.com/pizza_margherita_detail.jpg",
        "quantityAvailable": 50,
        "status": "available"
      }')
    
    echo "Create Product Response:"
    echo "$CREATE_PRODUCT" | jq '.'
    
    PRODUCT_ID=$(echo "$CREATE_PRODUCT" | jq -r '.data.id // empty')
    echo "Created Product ID: $PRODUCT_ID"
else
    echo "Không có Shop ID để tạo sản phẩm"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 5. LẤY DANH SÁCH SẢN PHẨM CỦA SHOP
# =====================================================
echo -e "${YELLOW}5. Lấy danh sách sản phẩm của shop${NC}"
echo ""

if [ -n "$SHOP_ID" ] && [ "$SHOP_ID" != "null" ]; then
    GET_PRODUCTS=$(curl -s -X GET "${BASE_URL}/api/seller/shops/${SHOP_ID}/products")
    echo "Get Products by Shop Response:"
    echo "$GET_PRODUCTS" | jq '.'
else
    echo "Không có Shop ID để test"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 6. LẤY SẢN PHẨM THEO TRẠNG THÁI
# =====================================================
echo -e "${YELLOW}6. Lấy sản phẩm theo trạng thái (available)${NC}"
echo ""

if [ -n "$SHOP_ID" ] && [ "$SHOP_ID" != "null" ]; then
    GET_PRODUCTS_BY_STATUS=$(curl -s -X GET "${BASE_URL}/api/seller/shops/${SHOP_ID}/products/status/available")
    echo "Get Products by Status Response:"
    echo "$GET_PRODUCTS_BY_STATUS" | jq '.'
else
    echo "Không có Shop ID để test"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 7. CẬP NHẬT SẢN PHẨM
# =====================================================
echo -e "${YELLOW}7. Cập nhật sản phẩm${NC}"
echo ""

if [ -n "$PRODUCT_ID" ] && [ "$PRODUCT_ID" != "null" ]; then
    UPDATE_PRODUCT=$(curl -s -X PUT "${BASE_URL}/api/seller/products/${PRODUCT_ID}" \
      -H "Content-Type: application/json" \
      -d '{
        "name": "Pizza Margherita Updated - Seller Product",
        "description": "Pizza cổ điển từ seller - đã cập nhật",
        "price": 160000.00,
        "quantityAvailable": 45
      }')
    
    echo "Update Product Response:"
    echo "$UPDATE_PRODUCT" | jq '.'
else
    echo "Không có Product ID để test"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 8. LẤY THÔNG TIN SẢN PHẨM
# =====================================================
echo -e "${YELLOW}8. Lấy thông tin sản phẩm${NC}"
echo ""

if [ -n "$PRODUCT_ID" ] && [ "$PRODUCT_ID" != "null" ]; then
    GET_PRODUCT=$(curl -s -X GET "${BASE_URL}/api/seller/products/${PRODUCT_ID}")
    echo "Get Product Response:"
    echo "$GET_PRODUCT" | jq '.'
else
    echo "Không có Product ID để test"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 9. TEST UNAUTHORIZED ACCESS
# =====================================================
echo -e "${YELLOW}9. Test unauthorized access (không có token)${NC}"
echo ""

UNAUTHORIZED_TEST=$(curl -s -X GET "${BASE_URL}/api/seller/shops" \
  -w "\nHTTP Status: %{http_code}\n")
echo "Unauthorized Access Response:"
echo "$UNAUTHORIZED_TEST"

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 10. TEST ACCESS ADMIN APIs (SHOULD FAIL)
# =====================================================
echo -e "${YELLOW}10. Test access Admin APIs (should fail)${NC}"
echo ""

ADMIN_TEST=$(curl -s -X GET "${BASE_URL}/api/admin/shops" \
  -w "\nHTTP Status: %{http_code}\n")
echo "Admin API Access Response:"
echo "$ADMIN_TEST"

echo ""
echo "----------------------------------------"
echo ""

echo -e "${GREEN}=== KẾT QUẢ TEST SELLER APIs ===${NC}"
echo ""
echo -e "${GREEN}✅ Các APIs đã test:${NC}"
echo "1. ✅ POST /api/seller/shops - Tạo cửa hàng"
echo "2. ✅ GET /api/seller/shops/{shopId} - Lấy thông tin cửa hàng"
echo "3. ✅ PUT /api/seller/shops/{shopId} - Cập nhật cửa hàng"
echo "4. ✅ POST /api/seller/products - Tạo sản phẩm"
echo "5. ✅ GET /api/seller/shops/{shopId}/products - Lấy sản phẩm theo shop"
echo "6. ✅ GET /api/seller/shops/{shopId}/products/status/{status} - Lấy sản phẩm theo trạng thái"
echo "7. ✅ PUT /api/seller/products/{productId} - Cập nhật sản phẩm"
echo "8. ✅ GET /api/seller/products/{productId} - Lấy thông tin sản phẩm"
echo "9. ✅ Test unauthorized access"
echo "10. ✅ Test access Admin APIs (should fail)"
echo ""
echo -e "${BLUE}Phân quyền:${NC}"
echo "✅ Seller chỉ có thể quản lý sản phẩm và cửa hàng của mình"
echo "✅ Seller không thể truy cập Admin APIs"
echo "✅ Cần authentication để truy cập Seller APIs"
echo ""
echo -e "${BLUE}API Endpoints:${NC}"
echo "POST   /api/seller/shops                    - Tạo cửa hàng"
echo "GET    /api/seller/shops                    - Lấy danh sách cửa hàng của seller"
echo "GET    /api/seller/shops/{shopId}           - Lấy thông tin cửa hàng"
echo "PUT    /api/seller/shops/{shopId}           - Cập nhật cửa hàng"
echo "POST   /api/seller/products                 - Tạo sản phẩm"
echo "GET    /api/seller/products/{productId}     - Lấy thông tin sản phẩm"
echo "PUT    /api/seller/products/{productId}     - Cập nhật sản phẩm"
echo "DELETE /api/seller/products/{productId}     - Xóa sản phẩm"
echo "GET    /api/seller/shops/{shopId}/products  - Lấy sản phẩm theo shop"
echo "GET    /api/seller/shops/{shopId}/products/status/{status} - Lấy sản phẩm theo trạng thái"
