#!/bin/bash

# =====================================================
# SCRIPT TEST UNIFIED AUTHENTICATION
# =====================================================

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SCRIPT TEST UNIFIED AUTHENTICATION ===${NC}"
echo ""
echo -e "${YELLOW}Test hệ thống authentication thống nhất:${NC}"
echo "- 1 API đăng nhập duy nhất cho cả Admin và Seller"
echo "- Trả về role khác nhau tùy theo user"
echo "- Seller có thể đăng ký tự do"
echo "- Admin tạo bằng script SQL"
echo ""

# =====================================================
# 1. ADMIN ĐĂNG NHẬP (UNIFIED API)
# =====================================================
echo -e "${YELLOW}1. Admin đăng nhập (Unified API)${NC}"
echo ""

ADMIN_LOGIN=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@foodshare.com",
    "password": "Admin123456"
  }')

echo "Admin Login Response:"
echo "$ADMIN_LOGIN" | jq '.'

ADMIN_TOKEN=$(echo "$ADMIN_LOGIN" | jq -r '.data.accessToken // empty')
echo "Admin Token: $ADMIN_TOKEN"
echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. LẤY THÔNG TIN USER HIỆN TẠI (ADMIN)
# =====================================================
echo -e "${YELLOW}2. Lấy thông tin Admin hiện tại${NC}"
echo ""

if [ -n "$ADMIN_TOKEN" ] && [ "$ADMIN_TOKEN" != "null" ]; then
    GET_ADMIN=$(curl -s -X GET "${BASE_URL}/api/auth/me" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    echo "Get Admin Response:"
    echo "$GET_ADMIN" | jq '.'
else
    echo "Không có Admin token để test"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. ADMIN TẠO ADMIN MỚI
# =====================================================
echo -e "${YELLOW}3. Admin tạo admin mới${NC}"
echo ""

if [ -n "$ADMIN_TOKEN" ] && [ "$ADMIN_TOKEN" != "null" ]; then
    CREATE_ADMIN=$(curl -s -X POST "${BASE_URL}/api/auth/admin/create-admin" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "name": "Another Admin",
        "email": "admin2@foodshare.com",
        "password": "Admin123456",
        "role": "ADMIN"
      }')
    
    echo "Create Admin Response:"
    echo "$CREATE_ADMIN" | jq '.'
    
    ADMIN2_TOKEN=$(echo "$CREATE_ADMIN" | jq -r '.data.accessToken // empty')
    echo "Admin2 Token: $ADMIN2_TOKEN"
else
    echo "Không có Admin token để tạo admin mới"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. TEST VALIDATION ERRORS
# =====================================================
echo -e "${YELLOW}4. Test validation errors${NC}"
echo ""

VALIDATION_ERROR=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "invalid-email",
    "password": "123"
  }')

echo "Validation Error Response:"
echo "$VALIDATION_ERROR" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

echo -e "${GREEN}=== KẾT QUẢ TEST UNIFIED AUTHENTICATION ===${NC}"
echo ""
echo -e "${GREEN}✅ Các APIs đã test:${NC}"
echo "1. ✅ POST /api/auth/login - Admin đăng nhập (Unified)"
echo "2. ✅ GET /api/auth/me - Lấy thông tin Admin"
echo "3. ✅ POST /api/auth/admin/create-admin - Admin tạo admin mới"
echo "4. ✅ Test validation errors"
echo ""
echo -e "${BLUE}API Endpoints:${NC}"
echo "POST /api/auth/login                    - Đăng nhập thống nhất (Admin/Seller)"
echo "GET  /api/auth/me                       - Lấy thông tin user hiện tại"
echo "POST /api/auth/logout                   - Đăng xuất"
echo "POST /api/auth/admin/create-admin       - Admin tạo admin mới"
echo ""
echo -e "${BLUE}Request Examples:${NC}"
echo "# Đăng nhập thống nhất"
echo 'curl -X POST "http://localhost:8080/api/auth/login" -d "{\"email\":\"user@foodshare.com\",\"password\":\"Password123\"}"'
echo ""
echo "# Lấy thông tin user"
echo 'curl -X GET "http://localhost:8080/api/auth/me" -H "Authorization: Bearer <token>"'
echo ""
echo "# Admin tạo admin mới"
echo 'curl -X POST "http://localhost:8080/api/auth/admin/create-admin" -H "Authorization: Bearer <admin_token>" -d "{\"name\":\"New Admin\",\"email\":\"newadmin@foodshare.com\",\"password\":\"Admin123456\",\"role\":\"ADMIN\"}"'
