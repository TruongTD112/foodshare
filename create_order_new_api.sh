#!/bin/bash

# =====================================================
# CURL SCRIPT ĐẶT ĐƠN HÀNG - API MỚI
# =====================================================

# Base URL
BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== CURL SCRIPT ĐẶT ĐƠN HÀNG - API MỚI ===${NC}"
echo ""

# =====================================================
# 1. ĐẶT ĐƠN HÀNG CƠ BẢN
# =====================================================
echo -e "${YELLOW}1. Đặt đơn hàng cơ bản${NC}"
echo "Sản phẩm: Pizza Margherita (ID: 1)"
echo "Cửa hàng: Pizza Corner (ID: 1)"
echo "Số lượng: 2"
echo "Giá đơn vị: 150,000 VND"
echo "Tổng giá: 300,000 VND"
echo "Thời gian nhận: 2024-01-15 14:30:00"
echo ""

curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
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

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. ĐẶT ĐƠN HÀNG VỚI THỜI GIAN KHÁC
# =====================================================
echo -e "${YELLOW}2. Đặt đơn hàng với thời gian khác${NC}"
echo "Sản phẩm: Pizza Pepperoni (ID: 2)"
echo "Cửa hàng: Pizza Corner (ID: 1)"
echo "Số lượng: 1"
echo "Giá đơn vị: 180,000 VND"
echo "Tổng giá: 180,000 VND"
echo "Thời gian nhận: 2024-01-15 16:00:00"
echo ""

curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 2,
    "quantity": 1,
    "pickupTime": "2024-01-15T16:00:00",
    "unitPrice": 180000.00,
    "totalPrice": 180000.00
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. ĐẶT ĐƠN HÀNG SỐ LƯỢNG LỚN
# =====================================================
echo -e "${YELLOW}3. Đặt đơn hàng số lượng lớn${NC}"
echo "Sản phẩm: Burger Deluxe (ID: 4)"
echo "Cửa hàng: Burger King (ID: 2)"
echo "Số lượng: 5"
echo "Giá đơn vị: 120,000 VND"
echo "Tổng giá: 600,000 VND"
echo "Thời gian nhận: 2024-01-15 18:30:00"
echo ""

curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 2,
    "productId": 4,
    "quantity": 5,
    "pickupTime": "2024-01-15T18:30:00",
    "unitPrice": 120000.00,
    "totalPrice": 600000.00
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. TEST LỖI - THIẾU PICKUP TIME
# =====================================================
echo -e "${YELLOW}4. Test lỗi - Thiếu pickup time${NC}"
echo ""

curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 1,
    "unitPrice": 150000.00,
    "totalPrice": 150000.00
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 5. TEST LỖI - PICKUP TIME TRONG QUÁ KHỨ
# =====================================================
echo -e "${YELLOW}5. Test lỗi - Pickup time trong quá khứ${NC}"
echo ""

curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 1,
    "pickupTime": "2023-01-01T10:00:00",
    "unitPrice": 150000.00,
    "totalPrice": 150000.00
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 6. TEST LỖI - GIÁ KHÔNG HỢP LỆ
# =====================================================
echo -e "${YELLOW}6. Test lỗi - Giá không hợp lệ${NC}"
echo ""

curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 1,
    "pickupTime": "2024-01-15T14:30:00",
    "unitPrice": 0,
    "totalPrice": 0
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 7. TEST LỖI - TÍNH TOÁN TỔNG GIÁ SAI
# =====================================================
echo -e "${YELLOW}7. Test lỗi - Tính toán tổng giá sai${NC}"
echo ""

curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "pickupTime": "2024-01-15T14:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 200000.00
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
# 10. HỦY ĐƠN HÀNG
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

echo -e "${GREEN}=== HOÀN THÀNH TEST API MỚI ===${NC}"
echo ""
echo -e "${BLUE}API mới yêu cầu:${NC}"
echo "1. pickupTime: Ngày và giờ đặt hàng (bắt buộc)"
echo "2. unitPrice: Giá trên từng sản phẩm (bắt buộc)"
echo "3. totalPrice: Tổng giá đơn hàng (bắt buộc)"
echo ""
echo -e "${BLUE}Validation:${NC}"
echo "- pickupTime phải trong tương lai"
echo "- unitPrice và totalPrice phải > 0"
echo "- totalPrice = unitPrice × quantity"
echo ""
echo -e "${BLUE}Response mới:${NC}"
echo "- Bao gồm unitPrice và totalPrice"
echo "- Hiển thị pickupTime chính xác"
