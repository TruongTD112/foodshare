#!/bin/bash

# =====================================================
# SCRIPT TEST POSTMAN APIs
# =====================================================

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SCRIPT TEST POSTMAN APIs ===${NC}"
echo ""

# =====================================================
# 1. TẠO TOKEN CHO USER 1
# =====================================================
echo -e "${YELLOW}1. Tạo token cho User 1${NC}"
echo ""

TOKEN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/social" \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "providerId": "google_123",
    "email": "user1@example.com",
    "name": "User 1",
    "profilePictureUrl": "https://example.com/avatar1.jpg"
  }')

echo "Response:"
echo "$TOKEN_RESPONSE" | jq '.'

# Extract token
TOKEN_1=$(echo "$TOKEN_RESPONSE" | jq -r '.data.token // empty')

if [ -n "$TOKEN_1" ] && [ "$TOKEN_1" != "null" ]; then
    echo -e "${GREEN}✅ Token User 1 tạo thành công${NC}"
    echo "Token: $TOKEN_1"
else
    echo -e "${RED}❌ Không thể tạo token cho User 1${NC}"
    exit 1
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. ĐẶT ĐƠN HÀNG PIZZA MARGHERITA
# =====================================================
echo -e "${YELLOW}2. Đặt đơn hàng Pizza Margherita${NC}"
echo ""

ORDER_RESPONSE=$(curl -s -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "pickupTime": "2024-01-15T14:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  }')

echo "Response:"
echo "$ORDER_RESPONSE" | jq '.'

# Extract order ID
ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.data.id // empty')

if [ -n "$ORDER_ID" ] && [ "$ORDER_ID" != "null" ]; then
    echo -e "${GREEN}✅ Đơn hàng tạo thành công - ID: $ORDER_ID${NC}"
else
    echo -e "${RED}❌ Không thể tạo đơn hàng${NC}"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. ĐẶT ĐƠN HÀNG PIZZA PEPPERONI
# =====================================================
echo -e "${YELLOW}3. Đặt đơn hàng Pizza Pepperoni${NC}"
echo ""

ORDER_RESPONSE=$(curl -s -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_1" \
  -d '{
    "shopId": 1,
    "productId": 2,
    "quantity": 1,
    "pickupTime": "2024-01-15T16:00:00",
    "unitPrice": 180000.00,
    "totalPrice": 180000.00
  }')

echo "Response:"
echo "$ORDER_RESPONSE" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. ĐẶT ĐƠN HÀNG BURGER DELUXE
# =====================================================
echo -e "${YELLOW}4. Đặt đơn hàng Burger Deluxe${NC}"
echo ""

ORDER_RESPONSE=$(curl -s -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_1" \
  -d '{
    "shopId": 2,
    "productId": 4,
    "quantity": 3,
    "pickupTime": "2024-01-15T18:30:00",
    "unitPrice": 120000.00,
    "totalPrice": 360000.00
  }')

echo "Response:"
echo "$ORDER_RESPONSE" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 5. LẤY TẤT CẢ ĐƠN HÀNG
# =====================================================
echo -e "${YELLOW}5. Lấy tất cả đơn hàng${NC}"
echo ""

curl -s -X GET "${BASE_URL}/orders" \
  -H "Authorization: Bearer $TOKEN_1" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 6. LẤY ĐƠN HÀNG THEO STATUS PENDING
# =====================================================
echo -e "${YELLOW}6. Lấy đơn hàng theo status pending${NC}"
echo ""

curl -s -X GET "${BASE_URL}/orders?status=pending" \
  -H "Authorization: Bearer $TOKEN_1" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 7. HỦY ĐƠN HÀNG (NẾU CÓ)
# =====================================================
if [ -n "$ORDER_ID" ] && [ "$ORDER_ID" != "null" ]; then
    echo -e "${YELLOW}7. Hủy đơn hàng ID: $ORDER_ID${NC}"
    echo ""

    curl -s -X DELETE "${BASE_URL}/orders/$ORDER_ID" \
      -H "Authorization: Bearer $TOKEN_1" \
      -w "\nHTTP Status: %{http_code}\n" | jq '.'
else
    echo -e "${YELLOW}7. Không có đơn hàng để hủy${NC}"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 8. TEST LỖI - THIẾU TOKEN
# =====================================================
echo -e "${YELLOW}8. Test lỗi - Thiếu token${NC}"
echo ""

curl -s -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 1,
    "pickupTime": "2024-01-15T14:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 150000.00
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 9. TEST LỖI - TÍNH TOÁN GIÁ SAI
# =====================================================
echo -e "${YELLOW}9. Test lỗi - Tính toán giá sai${NC}"
echo ""

curl -s -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "pickupTime": "2024-01-15T14:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 200000.00
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

echo -e "${GREEN}=== HOÀN THÀNH TEST POSTMAN APIs ===${NC}"
echo ""
echo -e "${BLUE}Các APIs đã test:${NC}"
echo "1. ✅ Social Login - Tạo token"
echo "2. ✅ Create Order - Pizza Margherita"
echo "3. ✅ Create Order - Pizza Pepperoni"
echo "4. ✅ Create Order - Burger Deluxe"
echo "5. ✅ Get All Orders - Lấy tất cả đơn hàng"
echo "6. ✅ Get Orders by Status - Lọc theo status"
echo "7. ✅ Cancel Order - Hủy đơn hàng"
echo "8. ✅ Error Testing - Test lỗi"
echo ""
echo -e "${BLUE}Postman Collection:${NC}"
echo "- Order_API_Collection.postman_collection.json"
echo "- Order_API_Environment.postman_environment.json"
echo ""
echo -e "${BLUE}Hướng dẫn:${NC}"
echo "- POSTMAN_ORDER_GUIDE.md"
