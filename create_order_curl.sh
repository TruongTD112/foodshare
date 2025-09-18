#!/bin/bash

# =====================================================
# CURL SCRIPT ĐẶT ĐƠN HÀNG
# =====================================================

# Base URL
BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== CURL SCRIPT ĐẶT ĐƠN HÀNG ===${NC}"
echo ""

# =====================================================
# 1. ĐẶT ĐƠN HÀNG CƠ BẢN
# =====================================================
echo -e "${YELLOW}1. Đặt đơn hàng cơ bản${NC}"
echo "Sản phẩm: Pizza Margherita (ID: 1)"
echo "Cửa hàng: Pizza Corner (ID: 1)"
echo "Số lượng: 2"
echo ""

curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "pickupInMinutes": 30
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. ĐẶT ĐƠN HÀNG VỚI PICKUP TIME KHÁC
# =====================================================
echo -e "${YELLOW}2. Đặt đơn hàng với pickup time khác${NC}"
echo "Sản phẩm: Pizza Pepperoni (ID: 2)"
echo "Cửa hàng: Pizza Corner (ID: 1)"
echo "Số lượng: 1"
echo "Pickup: 60 phút"
echo ""

curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 2,
    "quantity": 1,
    "pickupInMinutes": 60
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. ĐẶT ĐƠN HÀNG KHÔNG CÓ PICKUP TIME (DEFAULT)
# =====================================================
echo -e "${YELLOW}3. Đặt đơn hàng không có pickup time (default)${NC}"
echo "Sản phẩm: Burger Deluxe (ID: 4)"
echo "Cửa hàng: Burger King (ID: 2)"
echo "Số lượng: 3"
echo ""

curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 2,
    "productId": 4,
    "quantity": 3
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. ĐẶT ĐƠN HÀNG SỐ LƯỢNG LỚN
# =====================================================
echo -e "${YELLOW}4. Đặt đơn hàng số lượng lớn${NC}"
echo "Sản phẩm: Cà phê đen (ID: 7)"
echo "Cửa hàng: Cafe Central (ID: 3)"
echo "Số lượng: 10"
echo ""

curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 3,
    "productId": 7,
    "quantity": 10,
    "pickupInMinutes": 15
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 5. TEST LỖI - KHÔNG CÓ TOKEN
# =====================================================
echo -e "${YELLOW}5. Test lỗi - Không có token${NC}"
echo ""

curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 1
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 6. TEST LỖI - SẢN PHẨM KHÔNG TỒN TẠI
# =====================================================
echo -e "${YELLOW}6. Test lỗi - Sản phẩm không tồn tại${NC}"
echo ""

curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 999,
    "quantity": 1
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 7. TEST LỖI - SỐ LƯỢNG KHÔNG HỢP LỆ
# =====================================================
echo -e "${YELLOW}7. Test lỗi - Số lượng không hợp lệ${NC}"
echo ""

curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 0
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 8. XEM DANH SÁCH ĐƠN HÀNG
# =====================================================
echo -e "${YELLOW}8. Xem danh sách đơn hàng${NC}"
echo ""

curl -X GET "${BASE_URL}/orders" \
  -H "Authorization: Bearer user_1" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 9. XEM ĐƠN HÀNG THEO STATUS
# =====================================================
echo -e "${YELLOW}9. Xem đơn hàng theo status (pending)${NC}"
echo ""

curl -X GET "${BASE_URL}/orders?status=pending" \
  -H "Authorization: Bearer user_1" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 10. HỦY ĐƠN HÀNG (CẦN ORDER ID TỪ BƯỚC TRƯỚC)
# =====================================================
echo -e "${YELLOW}10. Hủy đơn hàng (thay ORDER_ID bằng ID thực tế)${NC}"
echo ""

# Lưu ý: Thay ORDER_ID bằng ID thực tế từ response trước đó
ORDER_ID=1

curl -X DELETE "${BASE_URL}/orders/${ORDER_ID}" \
  -H "Authorization: Bearer user_1" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

echo -e "${GREEN}=== HOÀN THÀNH TEST ĐẶT ĐƠN HÀNG ===${NC}"
echo ""
echo -e "${BLUE}Các lệnh cURL chính:${NC}"
echo "1. Đặt đơn hàng: POST /orders"
echo "2. Xem đơn hàng: GET /orders"
echo "3. Hủy đơn hàng: DELETE /orders/{id}"
echo ""
echo -e "${BLUE}Headers cần thiết:${NC}"
echo "- Content-Type: application/json"
echo "- Authorization: Bearer user_1"
echo ""
echo -e "${BLUE}Body mẫu:${NC}"
echo '{
  "shopId": 1,
  "productId": 1,
  "quantity": 2,
  "pickupInMinutes": 30
}'
