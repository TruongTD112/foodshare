#!/bin/bash

# =====================================================
# SCRIPT TẠO TOKEN CHO TESTING
# =====================================================

# Base URL
BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SCRIPT TẠO TOKEN CHO TESTING ===${NC}"
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
    echo ""
    echo -e "${GREEN}✅ Token User 1:${NC}"
    echo "$TOKEN_1"
    echo ""
    echo -e "${BLUE}Sử dụng token này trong header:${NC}"
    echo "Authorization: Bearer $TOKEN_1"
else
    echo -e "${RED}❌ Không thể tạo token cho User 1${NC}"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. TẠO TOKEN CHO USER 2
# =====================================================
echo -e "${YELLOW}2. Tạo token cho User 2${NC}"
echo ""

TOKEN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/social" \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "providerId": "google_456",
    "email": "user2@example.com",
    "name": "User 2",
    "profilePictureUrl": "https://example.com/avatar2.jpg"
  }')

echo "Response:"
echo "$TOKEN_RESPONSE" | jq '.'

# Extract token
TOKEN_2=$(echo "$TOKEN_RESPONSE" | jq -r '.data.token // empty')

if [ -n "$TOKEN_2" ] && [ "$TOKEN_2" != "null" ]; then
    echo ""
    echo -e "${GREEN}✅ Token User 2:${NC}"
    echo "$TOKEN_2"
    echo ""
    echo -e "${BLUE}Sử dụng token này trong header:${NC}"
    echo "Authorization: Bearer $TOKEN_2"
else
    echo -e "${RED}❌ Không thể tạo token cho User 2${NC}"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. TẠO TOKEN CHO USER 3
# =====================================================
echo -e "${YELLOW}3. Tạo token cho User 3${NC}"
echo ""

TOKEN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/social" \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "providerId": "google_789",
    "email": "user3@example.com",
    "name": "User 3",
    "profilePictureUrl": "https://example.com/avatar3.jpg"
  }')

echo "Response:"
echo "$TOKEN_RESPONSE" | jq '.'

# Extract token
TOKEN_3=$(echo "$TOKEN_RESPONSE" | jq -r '.data.token // empty')

if [ -n "$TOKEN_3" ] && [ "$TOKEN_3" != "null" ]; then
    echo ""
    echo -e "${GREEN}✅ Token User 3:${NC}"
    echo "$TOKEN_3"
    echo ""
    echo -e "${BLUE}Sử dụng token này trong header:${NC}"
    echo "Authorization: Bearer $TOKEN_3"
else
    echo -e "${RED}❌ Không thể tạo token cho User 3${NC}"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. TEST TOKEN VỚI API ĐẶT ĐƠN HÀNG
# =====================================================
if [ -n "$TOKEN_1" ] && [ "$TOKEN_1" != "null" ]; then
    echo -e "${YELLOW}4. Test token với API đặt đơn hàng${NC}"
    echo ""

    curl -X POST "${BASE_URL}/orders" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN_1" \
      -d '{
        "shopId": 1,
        "productId": 1,
        "quantity": 2,
        "pickupTime": "2024-01-15T14:30:00",
        "unitPrice": 150000.00,
        "totalPrice": 300000.00
      }' \
      -w "\nHTTP Status: %{http_code}\n" \
      -s | jq '.'
else
    echo -e "${RED}❌ Không có token để test${NC}"
fi

echo ""
echo "----------------------------------------"
echo ""

echo -e "${GREEN}=== HOÀN THÀNH TẠO TOKEN ===${NC}"
echo ""
echo -e "${BLUE}Cách sử dụng token:${NC}"
echo "1. Copy token từ output trên"
echo "2. Sử dụng trong header: Authorization: Bearer <token>"
echo "3. Test với API đặt đơn hàng"
echo ""
echo -e "${BLUE}Ví dụ cURL với token:${NC}"
echo 'curl -X POST "http://localhost:8080/orders" \'
echo '  -H "Content-Type: application/json" \'
echo '  -H "Authorization: Bearer YOUR_TOKEN_HERE" \'
echo '  -d '"'"'{"shopId": 1, "productId": 1, "quantity": 2, "pickupTime": "2024-01-15T14:30:00", "unitPrice": 150000.00, "totalPrice": 300000.00}'"'"''
