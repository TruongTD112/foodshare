#!/bin/bash

# =====================================================
# SCRIPT TEST ORDER ID TRONG RESPONSE
# =====================================================

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== TEST ORDER ID TRONG RESPONSE ===${NC}"
echo ""

# =====================================================
# 1. TẠO TOKEN
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

echo "Token Response:"
echo "$TOKEN_RESPONSE" | jq '.'

# Extract token
TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.data.token // empty')

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    echo -e "${GREEN}✅ Token tạo thành công${NC}"
else
    echo -e "${RED}❌ Không thể tạo token${NC}"
    exit 1
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. TẠO ĐƠN HÀNG VÀ KIỂM TRA ORDER ID
# =====================================================
echo -e "${YELLOW}2. Tạo đơn hàng và kiểm tra Order ID${NC}"
echo ""

ORDER_RESPONSE=$(curl -s -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "pickupTime": "2024-01-15T14:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  }')

echo "Order Response:"
echo "$ORDER_RESPONSE" | jq '.'

# Extract order ID
ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.data.id // empty')

if [ -n "$ORDER_ID" ] && [ "$ORDER_ID" != "null" ] && [ "$ORDER_ID" != "0" ]; then
    echo ""
    echo -e "${GREEN}✅ Order ID được trả về: $ORDER_ID${NC}"
    echo -e "${GREEN}✅ API create order đã trả về order ID cho client${NC}"
else
    echo -e "${RED}❌ Order ID không được trả về hoặc null${NC}"
    echo "Response data:"
    echo "$ORDER_RESPONSE" | jq '.data'
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. KIỂM TRA TẤT CẢ FIELDS TRONG RESPONSE
# =====================================================
echo -e "${YELLOW}3. Kiểm tra tất cả fields trong OrderResponse${NC}"
echo ""

echo "Order ID: $(echo "$ORDER_RESPONSE" | jq -r '.data.id // "null"')"
echo "User ID: $(echo "$ORDER_RESPONSE" | jq -r '.data.userId // "null"')"
echo "Shop ID: $(echo "$ORDER_RESPONSE" | jq -r '.data.shopId // "null"')"
echo "Product ID: $(echo "$ORDER_RESPONSE" | jq -r '.data.productId // "null"')"
echo "Quantity: $(echo "$ORDER_RESPONSE" | jq -r '.data.quantity // "null"')"
echo "Status: $(echo "$ORDER_RESPONSE" | jq -r '.data.status // "null"')"
echo "Pickup Time: $(echo "$ORDER_RESPONSE" | jq -r '.data.pickupTime // "null"')"
echo "Expires At: $(echo "$ORDER_RESPONSE" | jq -r '.data.expiresAt // "null"')"
echo "Unit Price: $(echo "$ORDER_RESPONSE" | jq -r '.data.unitPrice // "null"')"
echo "Total Price: $(echo "$ORDER_RESPONSE" | jq -r '.data.totalPrice // "null"')"

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. TEST VỚI ĐƠN HÀNG KHÁC
# =====================================================
echo -e "${YELLOW}4. Test với đơn hàng khác${NC}"
echo ""

ORDER_RESPONSE_2=$(curl -s -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "shopId": 1,
    "productId": 2,
    "quantity": 1,
    "pickupTime": "2024-01-15T16:00:00",
    "unitPrice": 180000.00,
    "totalPrice": 180000.00
  }')

echo "Order 2 Response:"
echo "$ORDER_RESPONSE_2" | jq '.'

# Extract order ID 2
ORDER_ID_2=$(echo "$ORDER_RESPONSE_2" | jq -r '.data.id // empty')

if [ -n "$ORDER_ID_2" ] && [ "$ORDER_ID_2" != "null" ] && [ "$ORDER_ID_2" != "0" ]; then
    echo ""
    echo -e "${GREEN}✅ Order 2 ID: $ORDER_ID_2${NC}"
else
    echo -e "${RED}❌ Order 2 ID không được trả về${NC}"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 5. KIỂM TRA DANH SÁCH ĐƠN HÀNG
# =====================================================
echo -e "${YELLOW}5. Kiểm tra danh sách đơn hàng${NC}"
echo ""

curl -s -X GET "${BASE_URL}/orders" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

echo -e "${GREEN}=== KẾT QUẢ TEST ===${NC}"
echo ""
if [ -n "$ORDER_ID" ] && [ "$ORDER_ID" != "null" ] && [ "$ORDER_ID" != "0" ]; then
    echo -e "${GREEN}✅ PASS: API create order đã trả về order ID cho client${NC}"
    echo -e "${GREEN}✅ Order ID: $ORDER_ID${NC}"
else
    echo -e "${RED}❌ FAIL: API create order không trả về order ID${NC}"
fi

if [ -n "$ORDER_ID_2" ] && [ "$ORDER_ID_2" != "null" ] && [ "$ORDER_ID_2" != "0" ]; then
    echo -e "${GREEN}✅ Order 2 ID: $ORDER_ID_2${NC}"
else
    echo -e "${RED}❌ Order 2 ID không được trả về${NC}"
fi

echo ""
echo -e "${BLUE}Response format:${NC}"
echo '{
  "success": true,
  "code": "200", 
  "message": "Success",
  "data": {
    "id": 1,                    // ← Order ID được trả về
    "userId": 1,
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "status": "pending",
    "pickupTime": "2024-01-15T14:30:00",
    "expiresAt": "2024-01-15T15:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  }
}'
