#!/bin/bash

# =====================================================
# SCRIPT TEST SHOP MANAGEMENT APIs
# =====================================================

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SCRIPT TEST SHOP MANAGEMENT APIs ===${NC}"
echo ""

# =====================================================
# 1. TẠO CỬA HÀNG MỚI
# =====================================================
echo -e "${YELLOW}1. Tạo cửa hàng mới${NC}"
echo ""

CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/admin/shops" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pizza Palace New",
    "address": "456 Đường XYZ, Quận 2, TP.HCM",
    "phone": "0987654321",
    "imageUrl": "https://example.com/pizza_palace_new.jpg",
    "latitude": 10.7769,
    "longitude": 106.7009,
    "description": "Cửa hàng pizza mới với không gian rộng rãi",
    "status": "active"
  }')

echo "Create Shop Response:"
echo "$CREATE_RESPONSE" | jq '.'

# Extract shop ID
SHOP_ID=$(echo "$CREATE_RESPONSE" | jq -r '.data.id // empty')

if [ -n "$SHOP_ID" ] && [ "$SHOP_ID" != "null" ] && [ "$SHOP_ID" != "0" ]; then
    echo -e "${GREEN}✅ Cửa hàng tạo thành công - ID: $SHOP_ID${NC}"
else
    echo -e "${RED}❌ Không thể tạo cửa hàng${NC}"
    exit 1
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. LẤY THÔNG TIN CỬA HÀNG VỪA TẠO
# =====================================================
echo -e "${YELLOW}2. Lấy thông tin cửa hàng vừa tạo${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/shops/$SHOP_ID" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. CẬP NHẬT THÔNG TIN CỬA HÀNG
# =====================================================
echo -e "${YELLOW}3. Cập nhật thông tin cửa hàng${NC}"
echo ""

UPDATE_RESPONSE=$(curl -s -X PUT "${BASE_URL}/api/admin/shops/$SHOP_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pizza Palace Updated",
    "address": "789 Đường ABC, Quận 3, TP.HCM",
    "phone": "0123456789",
    "imageUrl": "https://example.com/pizza_palace_updated.jpg",
    "latitude": 10.7829,
    "longitude": 106.6959,
    "description": "Cửa hàng pizza đã được cập nhật với menu mới",
    "status": "active"
  }')

echo "Update Shop Response:"
echo "$UPDATE_RESPONSE" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. LẤY DANH SÁCH TẤT CẢ CỬA HÀNG
# =====================================================
echo -e "${YELLOW}4. Lấy danh sách tất cả cửa hàng${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/shops" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 5. LẤY DANH SÁCH CỬA HÀNG THEO TRẠNG THÁI
# =====================================================
echo -e "${YELLOW}5. Lấy danh sách cửa hàng theo trạng thái (active)${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/shops/status/active" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 6. TẠO CỬA HÀNG VỚI TRẠNG THÁI INACTIVE
# =====================================================
echo -e "${YELLOW}6. Tạo cửa hàng với trạng thái inactive${NC}"
echo ""

CREATE_INACTIVE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/admin/shops" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Burger Joint Inactive",
    "address": "321 Đường DEF, Quận 4, TP.HCM",
    "phone": "0369852147",
    "imageUrl": "https://example.com/burger_joint.jpg",
    "latitude": 10.7659,
    "longitude": 106.7059,
    "description": "Cửa hàng burger tạm ngưng hoạt động",
    "status": "inactive"
  }')

echo "Create Inactive Shop Response:"
echo "$CREATE_INACTIVE_RESPONSE" | jq '.'

# Extract inactive shop ID
INACTIVE_SHOP_ID=$(echo "$CREATE_INACTIVE_RESPONSE" | jq -r '.data.id // empty')

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 7. LẤY DANH SÁCH CỬA HÀNG INACTIVE
# =====================================================
echo -e "${YELLOW}7. Lấy danh sách cửa hàng inactive${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/shops/status/inactive" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 8. TEST LỖI - TẠO CỬA HÀNG THIẾU THÔNG TIN
# =====================================================
echo -e "${YELLOW}8. Test lỗi - Tạo cửa hàng thiếu thông tin${NC}"
echo ""

curl -s -X POST "${BASE_URL}/api/admin/shops" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "latitude": 10.7769
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 9. TEST LỖI - TẠO CỬA HÀNG VỚI TỌA ĐỘ KHÔNG HỢP LỆ
# =====================================================
echo -e "${YELLOW}9. Test lỗi - Tạo cửa hàng với tọa độ không hợp lệ${NC}"
echo ""

curl -s -X POST "${BASE_URL}/api/admin/shops" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Invalid Coordinates Shop",
    "address": "123 Test Street",
    "latitude": 200.0,
    "longitude": 300.0,
    "status": "active"
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 10. TEST LỖI - CẬP NHẬT CỬA HÀNG KHÔNG TỒN TẠI
# =====================================================
echo -e "${YELLOW}10. Test lỗi - Cập nhật cửa hàng không tồn tại${NC}"
echo ""

curl -s -X PUT "${BASE_URL}/api/admin/shops/999" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Non-existent Shop"
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 11. XÓA CỬA HÀNG INACTIVE
# =====================================================
if [ -n "$INACTIVE_SHOP_ID" ] && [ "$INACTIVE_SHOP_ID" != "null" ] && [ "$INACTIVE_SHOP_ID" != "0" ]; then
    echo -e "${YELLOW}11. Xóa cửa hàng inactive (ID: $INACTIVE_SHOP_ID)${NC}"
    echo ""

    curl -s -X DELETE "${BASE_URL}/api/admin/shops/$INACTIVE_SHOP_ID" \
      -w "\nHTTP Status: %{http_code}\n" | jq '.'
else
    echo -e "${YELLOW}11. Không có cửa hàng inactive để xóa${NC}"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 12. XÓA CỬA HÀNG CHÍNH
# =====================================================
echo -e "${YELLOW}12. Xóa cửa hàng chính (ID: $SHOP_ID)${NC}"
echo ""

curl -s -X DELETE "${BASE_URL}/api/admin/shops/$SHOP_ID" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 13. KIỂM TRA CỬA HÀNG ĐÃ BỊ XÓA
# =====================================================
echo -e "${YELLOW}13. Kiểm tra cửa hàng đã bị xóa${NC}"
echo ""

curl -s -X GET "${BASE_URL}/api/admin/shops/$SHOP_ID" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

echo -e "${GREEN}=== KẾT QUẢ TEST SHOP MANAGEMENT APIs ===${NC}"
echo ""
echo -e "${GREEN}✅ Các APIs đã test:${NC}"
echo "1. ✅ POST /api/admin/shops - Tạo cửa hàng"
echo "2. ✅ GET /api/admin/shops/{id} - Lấy thông tin cửa hàng"
echo "3. ✅ PUT /api/admin/shops/{id} - Cập nhật cửa hàng"
echo "4. ✅ GET /api/admin/shops - Lấy danh sách tất cả cửa hàng"
echo "5. ✅ GET /api/admin/shops/status/{status} - Lấy cửa hàng theo trạng thái"
echo "6. ✅ DELETE /api/admin/shops/{id} - Xóa cửa hàng"
echo "7. ✅ Error Testing - Test các trường hợp lỗi"
echo ""
echo -e "${BLUE}Response format:${NC}"
echo '{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Pizza Palace",
    "address": "123 Đường ABC, Quận 1, TP.HCM",
    "phone": "0123456789",
    "imageUrl": "https://example.com/pizza_palace.jpg",
    "latitude": 10.7769,
    "longitude": 106.7009,
    "description": "Cửa hàng pizza ngon",
    "rating": 4.5,
    "status": "active"
  }
}'
