#!/bin/bash

# =====================================================
# SCRIPT TEST ORDER STATUS UPDATE API
# =====================================================

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SCRIPT TEST ORDER STATUS UPDATE API ===${NC}"
echo ""

# =====================================================
# 1. TẠO ĐƠN HÀNG MỚI
# =====================================================
echo -e "${YELLOW}1. Tạo đơn hàng mới${NC}"
echo ""

CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "pickupTime": "2024-01-15T14:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  }')

echo "Create Order Response:"
echo "$CREATE_RESPONSE" | jq '.'

# Extract order ID
ORDER_ID=$(echo "$CREATE_RESPONSE" | jq -r '.data.id // empty')

if [ -n "$ORDER_ID" ] && [ "$ORDER_ID" != "null" ] && [ "$ORDER_ID" != "0" ]; then
    echo -e "${GREEN}✅ Đơn hàng tạo thành công - ID: $ORDER_ID${NC}"
else
    echo -e "${RED}❌ Không thể tạo đơn hàng${NC}"
    exit 1
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. CẬP NHẬT TRẠNG THÁI: PENDING → CONFIRMED
# =====================================================
echo -e "${YELLOW}2. Cập nhật trạng thái: PENDING → CONFIRMED${NC}"
echo ""

curl -s -X PUT "${BASE_URL}/orders/$ORDER_ID/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "status": "confirmed"
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. CẬP NHẬT TRẠNG THÁI: CONFIRMED → PREPARING
# =====================================================
echo -e "${YELLOW}3. Cập nhật trạng thái: CONFIRMED → PREPARING${NC}"
echo ""

curl -s -X PUT "${BASE_URL}/orders/$ORDER_ID/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "status": "preparing"
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. CẬP NHẬT TRẠNG THÁI: PREPARING → READY
# =====================================================
echo -e "${YELLOW}4. Cập nhật trạng thái: PREPARING → READY${NC}"
echo ""

curl -s -X PUT "${BASE_URL}/orders/$ORDER_ID/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "status": "ready"
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 5. CẬP NHẬT TRẠNG THÁI: READY → COMPLETED
# =====================================================
echo -e "${YELLOW}5. Cập nhật trạng thái: READY → COMPLETED${NC}"
echo ""

curl -s -X PUT "${BASE_URL}/orders/$ORDER_ID/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "status": "completed"
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 6. TEST LỖI - CẬP NHẬT TRẠNG THÁI ĐÃ COMPLETED
# =====================================================
echo -e "${YELLOW}6. Test lỗi - Cập nhật trạng thái đã COMPLETED${NC}"
echo ""

curl -s -X PUT "${BASE_URL}/orders/$ORDER_ID/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "status": "pending"
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 7. TẠO ĐƠN HÀNG MỚI ĐỂ TEST CANCELLED
# =====================================================
echo -e "${YELLOW}7. Tạo đơn hàng mới để test CANCELLED${NC}"
echo ""

CREATE_RESPONSE_2=$(curl -s -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 2,
    "quantity": 1,
    "pickupTime": "2024-01-15T15:30:00",
    "unitPrice": 200000.00,
    "totalPrice": 200000.00
  }')

echo "Create Order 2 Response:"
echo "$CREATE_RESPONSE_2" | jq '.'

# Extract order ID 2
ORDER_ID_2=$(echo "$CREATE_RESPONSE_2" | jq -r '.data.id // empty')

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 8. CẬP NHẬT TRẠNG THÁI: PENDING → CANCELLED
# =====================================================
if [ -n "$ORDER_ID_2" ] && [ "$ORDER_ID_2" != "null" ] && [ "$ORDER_ID_2" != "0" ]; then
    echo -e "${YELLOW}8. Cập nhật trạng thái: PENDING → CANCELLED (Order ID: $ORDER_ID_2)${NC}"
    echo ""

    curl -s -X PUT "${BASE_URL}/orders/$ORDER_ID_2/status" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer user_1" \
      -d '{
        "status": "cancelled"
      }' \
      -w "\nHTTP Status: %{http_code}\n" | jq '.'
else
    echo -e "${YELLOW}8. Không có đơn hàng thứ 2 để test CANCELLED${NC}"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 9. TEST LỖI - CẬP NHẬT TRẠNG THÁI KHÔNG HỢP LỆ
# =====================================================
echo -e "${YELLOW}9. Test lỗi - Cập nhật trạng thái không hợp lệ${NC}"
echo ""

curl -s -X PUT "${BASE_URL}/orders/$ORDER_ID/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "status": "invalid_status"
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 10. TEST LỖI - CẬP NHẬT ĐƠN HÀNG KHÔNG TỒN TẠI
# =====================================================
echo -e "${YELLOW}10. Test lỗi - Cập nhật đơn hàng không tồn tại${NC}"
echo ""

curl -s -X PUT "${BASE_URL}/orders/999/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "status": "confirmed"
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 11. TEST LỖI - THIẾU TRẠNG THÁI
# =====================================================
echo -e "${YELLOW}11. Test lỗi - Thiếu trạng thái${NC}"
echo ""

curl -s -X PUT "${BASE_URL}/orders/$ORDER_ID/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{}' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 12. TEST LỖI - CHUYỂN ĐỔI TRẠNG THÁI KHÔNG HỢP LỆ
# =====================================================
echo -e "${YELLOW}12. Test lỗi - Chuyển đổi trạng thái không hợp lệ (READY → PENDING)${NC}"
echo ""

# Tạo đơn hàng mới và chuyển sang ready
CREATE_RESPONSE_3=$(curl -s -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
  -d '{
    "shopId": 1,
    "productId": 3,
    "quantity": 1,
    "pickupTime": "2024-01-15T16:30:00",
    "unitPrice": 100000.00,
    "totalPrice": 100000.00
  }')

ORDER_ID_3=$(echo "$CREATE_RESPONSE_3" | jq -r '.data.id // empty')

if [ -n "$ORDER_ID_3" ] && [ "$ORDER_ID_3" != "null" ] && [ "$ORDER_ID_3" != "0" ]; then
    # Chuyển sang ready
    curl -s -X PUT "${BASE_URL}/orders/$ORDER_ID_3/status" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer user_1" \
      -d '{"status": "ready"}' > /dev/null

    # Test chuyển ngược về pending (không hợp lệ)
    curl -s -X PUT "${BASE_URL}/orders/$ORDER_ID_3/status" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer user_1" \
      -d '{
        "status": "pending"
      }' \
      -w "\nHTTP Status: %{http_code}\n" | jq '.'
else
    echo -e "${YELLOW}Không có đơn hàng thứ 3 để test${NC}"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 13. XEM DANH SÁCH ĐƠN HÀNG
# =====================================================
echo -e "${YELLOW}13. Xem danh sách đơn hàng${NC}"
echo ""

curl -s -X GET "${BASE_URL}/orders" \
  -H "Authorization: Bearer user_1" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

echo -e "${GREEN}=== KẾT QUẢ TEST ORDER STATUS UPDATE API ===${NC}"
echo ""
echo -e "${GREEN}✅ Các test cases đã thực hiện:${NC}"
echo "1. ✅ Tạo đơn hàng mới"
echo "2. ✅ PENDING → CONFIRMED"
echo "3. ✅ CONFIRMED → PREPARING"
echo "4. ✅ PREPARING → READY"
echo "5. ✅ READY → COMPLETED"
echo "6. ✅ PENDING → CANCELLED"
echo "7. ✅ Test lỗi - Trạng thái không hợp lệ"
echo "8. ✅ Test lỗi - Đơn hàng không tồn tại"
echo "9. ✅ Test lỗi - Thiếu trạng thái"
echo "10. ✅ Test lỗi - Chuyển đổi không hợp lệ"
echo ""
echo -e "${BLUE}API Endpoint:${NC}"
echo "PUT /orders/{id}/status"
echo ""
echo -e "${BLUE}Request Body:${NC}"
echo '{
  "status": "confirmed|preparing|ready|completed|cancelled"
}'
echo ""
echo -e "${BLUE}Response Format:${NC}"
echo '{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,
    "userId": 1,
    "shopId": 1,
    "productId": 1,
    "quantity": 2,
    "status": "confirmed",
    "pickupTime": "2024-01-15T14:30:00",
    "expiresAt": "2024-01-15T15:30:00",
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  }
}'
echo ""
echo -e "${BLUE}Status Transition Rules:${NC}"
echo "• PENDING → any status"
echo "• CONFIRMED → preparing, ready, completed, cancelled"
echo "• PREPARING → ready, completed, cancelled"
echo "• READY → completed, cancelled"
echo "• COMPLETED → (cannot change)"
echo "• CANCELLED → (cannot change)"
