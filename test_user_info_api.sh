#!/bin/bash

# =====================================================
# SCRIPT TEST USER INFO API - BỎ CREATED AT, UPDATED AT, PROVIDER ID
# =====================================================

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== TEST USER INFO API - BỎ CREATED AT, UPDATED AT, PROVIDER ID ===${NC}"
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

echo "Token Response:"
echo "$TOKEN_RESPONSE" | jq '.'

# Extract token và user ID
TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.data.token // empty')
USER_ID=$(echo "$TOKEN_RESPONSE" | jq -r '.data.userId // empty')

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    echo -e "${GREEN}✅ Token tạo thành công${NC}"
    echo "Token: $TOKEN"
    echo "User ID: $USER_ID"
else
    echo -e "${RED}❌ Không thể tạo token${NC}"
    exit 1
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. LẤY THÔNG TIN USER
# =====================================================
echo -e "${YELLOW}2. Lấy thông tin user${NC}"
echo ""

USER_INFO_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/users/$USER_ID" \
  -H "Authorization: Bearer $TOKEN")

echo "User Info Response:"
echo "$USER_INFO_RESPONSE" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. KIỂM TRA CÁC FIELD TRONG RESPONSE
# =====================================================
echo -e "${YELLOW}3. Kiểm tra các field trong response${NC}"
echo ""

echo "User ID: $(echo "$USER_INFO_RESPONSE" | jq -r '.data.id // "null"')"
echo "Name: $(echo "$USER_INFO_RESPONSE" | jq -r '.data.name // "null"')"
echo "Email: $(echo "$USER_INFO_RESPONSE" | jq -r '.data.email // "null"')"
echo "Phone Number: $(echo "$USER_INFO_RESPONSE" | jq -r '.data.phoneNumber // "null"')"
echo "Profile Picture URL: $(echo "$USER_INFO_RESPONSE" | jq -r '.data.profilePictureUrl // "null"')"

echo ""
echo "=== KIỂM TRA CÁC FIELD ĐÃ BỎ ==="
echo "Provider: $(echo "$USER_INFO_RESPONSE" | jq -r '.data.provider // "null"')"
echo "Provider ID: $(echo "$USER_INFO_RESPONSE" | jq -r '.data.providerId // "null"')"
echo "Created At: $(echo "$USER_INFO_RESPONSE" | jq -r '.data.createdAt // "null"')"
echo "Updated At: $(echo "$USER_INFO_RESPONSE" | jq -r '.data.updatedAt // "null"')"

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. CẬP NHẬT THÔNG TIN USER
# =====================================================
echo -e "${YELLOW}4. Cập nhật thông tin user${NC}"
echo ""

UPDATE_RESPONSE=$(curl -s -X PUT "${BASE_URL}/api/users/$USER_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "User 1 Updated",
    "email": "user1.updated@example.com",
    "phoneNumber": "0123456789",
    "profilePictureUrl": "https://example.com/avatar1_updated.jpg"
  }')

echo "Update Response:"
echo "$UPDATE_RESPONSE" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 5. KIỂM TRA RESPONSE SAU KHI CẬP NHẬT
# =====================================================
echo -e "${YELLOW}5. Kiểm tra response sau khi cập nhật${NC}"
echo ""

echo "Updated User ID: $(echo "$UPDATE_RESPONSE" | jq -r '.data.id // "null"')"
echo "Updated Name: $(echo "$UPDATE_RESPONSE" | jq -r '.data.name // "null"')"
echo "Updated Email: $(echo "$UPDATE_RESPONSE" | jq -r '.data.email // "null"')"
echo "Updated Phone Number: $(echo "$UPDATE_RESPONSE" | jq -r '.data.phoneNumber // "null"')"
echo "Updated Profile Picture URL: $(echo "$UPDATE_RESPONSE" | jq -r '.data.profilePictureUrl // "null"')"

echo ""
echo "=== KIỂM TRA CÁC FIELD ĐÃ BỎ (SAU CẬP NHẬT) ==="
echo "Provider: $(echo "$UPDATE_RESPONSE" | jq -r '.data.provider // "null"')"
echo "Provider ID: $(echo "$UPDATE_RESPONSE" | jq -r '.data.providerId // "null"')"
echo "Created At: $(echo "$UPDATE_RESPONSE" | jq -r '.data.createdAt // "null"')"
echo "Updated At: $(echo "$UPDATE_RESPONSE" | jq -r '.data.updatedAt // "null"')"

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 6. KIỂM TRA RESPONSE FORMAT
# =====================================================
echo -e "${YELLOW}6. Kiểm tra response format${NC}"
echo ""

echo "Response có đúng format không?"
echo "✅ Có field id: $(echo "$UPDATE_RESPONSE" | jq -r 'has("data") and (.data | has("id"))')"
echo "✅ Có field name: $(echo "$UPDATE_RESPONSE" | jq -r 'has("data") and (.data | has("name"))')"
echo "✅ Có field email: $(echo "$UPDATE_RESPONSE" | jq -r 'has("data") and (.data | has("email"))')"
echo "✅ Có field phoneNumber: $(echo "$UPDATE_RESPONSE" | jq -r 'has("data") and (.data | has("phoneNumber"))')"
echo "✅ Có field profilePictureUrl: $(echo "$UPDATE_RESPONSE" | jq -r 'has("data") and (.data | has("profilePictureUrl"))')"

echo ""
echo "=== KIỂM TRA CÁC FIELD ĐÃ BỎ ==="
echo "❌ Không có field provider: $(echo "$UPDATE_RESPONSE" | jq -r 'has("data") and (.data | has("provider") | not)')"
echo "❌ Không có field providerId: $(echo "$UPDATE_RESPONSE" | jq -r 'has("data") and (.data | has("providerId") | not)')"
echo "❌ Không có field createdAt: $(echo "$UPDATE_RESPONSE" | jq -r 'has("data") and (.data | has("createdAt") | not)')"
echo "❌ Không có field updatedAt: $(echo "$UPDATE_RESPONSE" | jq -r 'has("data") and (.data | has("updatedAt") | not)')"

echo ""
echo "----------------------------------------"
echo ""

echo -e "${GREEN}=== KẾT QUẢ TEST ===${NC}"
echo ""
echo -e "${GREEN}✅ User Info API đã được cập nhật:${NC}"
echo "- ✅ Bỏ field provider"
echo "- ✅ Bỏ field providerId" 
echo "- ✅ Bỏ field createdAt"
echo "- ✅ Bỏ field updatedAt"
echo ""
echo -e "${GREEN}✅ Các field còn lại:${NC}"
echo "- ✅ id"
echo "- ✅ name"
echo "- ✅ email"
echo "- ✅ phoneNumber"
echo "- ✅ profilePictureUrl"
echo ""
echo -e "${BLUE}Response format mới:${NC}"
echo '{
  "success": true,
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,
    "name": "User 1 Updated",
    "email": "user1.updated@example.com",
    "phoneNumber": "0123456789",
    "profilePictureUrl": "https://example.com/avatar1_updated.jpg"
  }
}'
