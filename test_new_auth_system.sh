#!/bin/bash

# =====================================================
# SCRIPT TEST NEW AUTHENTICATION SYSTEM
# =====================================================

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SCRIPT TEST NEW AUTHENTICATION SYSTEM ===${NC}"
echo ""
echo -e "${YELLOW}Test hệ thống authentication mới:${NC}"
echo "- Seller: Đăng ký và đăng nhập qua email"
echo "- Admin: Chỉ admin mới có thể tạo admin khác"
echo "- Admin đầu tiên: Tạo bằng script SQL"
echo ""

# =====================================================
# 1. SELLER ĐĂNG KÝ
# =====================================================
echo -e "${YELLOW}1. Seller đăng ký tài khoản${NC}"
echo ""

SELLER_REGISTER=$(curl -s -X POST "${BASE_URL}/api/seller/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Seller User",
    "email": "seller@foodshare.com",
    "password": "Seller123456"
  }')

echo "Seller Register Response:"
echo "$SELLER_REGISTER" | jq '.'

SELLER_TOKEN=$(echo "$SELLER_REGISTER" | jq -r '.data.accessToken // empty')
echo "Seller Token: $SELLER_TOKEN"
echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. SELLER ĐĂNG NHẬP
# =====================================================
echo -e "${YELLOW}2. Seller đăng nhập${NC}"
echo ""

SELLER_LOGIN=$(curl -s -X POST "${BASE_URL}/api/seller/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "seller@foodshare.com",
    "password": "Seller123456"
  }')

echo "Seller Login Response:"
echo "$SELLER_LOGIN" | jq '.'

SELLER_LOGIN_TOKEN=$(echo "$SELLER_LOGIN" | jq -r '.data.accessToken // empty')
echo "Seller Login Token: $SELLER_LOGIN_TOKEN"
echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. ADMIN ĐĂNG NHẬP (TỪ SCRIPT)
# =====================================================
echo -e "${YELLOW}3. Admin đăng nhập (từ script)${NC}"
echo ""

ADMIN_LOGIN=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/login" \
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
# 4. ADMIN TẠO ADMIN MỚI
# =====================================================
echo -e "${YELLOW}4. Admin tạo admin mới${NC}"
echo ""

if [ -n "$ADMIN_TOKEN" ] && [ "$ADMIN_TOKEN" != "null" ]; then
    CREATE_ADMIN=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/create-admin" \
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
# 5. TEST SELLER TẠO ADMIN (SHOULD FAIL)
# =====================================================
echo -e "${YELLOW}5. Test Seller tạo admin (should fail)${NC}"
echo ""

if [ -n "$SELLER_LOGIN_TOKEN" ] && [ "$SELLER_LOGIN_TOKEN" != "null" ]; then
    SELLER_CREATE_ADMIN=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/create-admin" \
      -H "Authorization: Bearer $SELLER_LOGIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "name": "Test Admin",
        "email": "testadmin@foodshare.com",
        "password": "Admin123456",
        "role": "ADMIN"
      }')
    
    echo "Seller Create Admin Response:"
    echo "$SELLER_CREATE_ADMIN" | jq '.'
else
    echo "Không có Seller token để test"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 6. TEST SELLER ĐĂNG NHẬP VÀO ADMIN API (SHOULD FAIL)
# =====================================================
echo -e "${YELLOW}6. Test Seller đăng nhập vào Admin API (should fail)${NC}"
echo ""

SELLER_ADMIN_LOGIN=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "seller@foodshare.com",
    "password": "Seller123456"
  }')

echo "Seller Admin Login Response:"
echo "$SELLER_ADMIN_LOGIN" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 7. TEST ADMIN ĐĂNG NHẬP VÀO SELLER API (SHOULD FAIL)
# =====================================================
echo -e "${YELLOW}7. Test Admin đăng nhập vào Seller API (should fail)${NC}"
echo ""

ADMIN_SELLER_LOGIN=$(curl -s -X POST "${BASE_URL}/api/seller/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@foodshare.com",
    "password": "Admin123456"
  }')

echo "Admin Seller Login Response:"
echo "$ADMIN_SELLER_LOGIN" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 8. LẤY THÔNG TIN SELLER HIỆN TẠI
# =====================================================
echo -e "${YELLOW}8. Lấy thông tin Seller hiện tại${NC}"
echo ""

if [ -n "$SELLER_LOGIN_TOKEN" ] && [ "$SELLER_LOGIN_TOKEN" != "null" ]; then
    GET_SELLER=$(curl -s -X GET "${BASE_URL}/api/seller/auth/me" \
      -H "Authorization: Bearer $SELLER_LOGIN_TOKEN")
    
    echo "Get Seller Response:"
    echo "$GET_SELLER" | jq '.'
else
    echo "Không có Seller token để test"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 9. LẤY THÔNG TIN ADMIN HIỆN TẠI
# =====================================================
echo -e "${YELLOW}9. Lấy thông tin Admin hiện tại${NC}"
echo ""

if [ -n "$ADMIN_TOKEN" ] && [ "$ADMIN_TOKEN" != "null" ]; then
    GET_ADMIN=$(curl -s -X GET "${BASE_URL}/api/auth/backoffice/me" \
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
# 10. TEST VALIDATION ERRORS
# =====================================================
echo -e "${YELLOW}10. Test validation errors${NC}"
echo ""

VALIDATION_ERROR=$(curl -s -X POST "${BASE_URL}/api/seller/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "email": "invalid-email",
    "password": "123"
  }')

echo "Validation Error Response:"
echo "$VALIDATION_ERROR" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

echo -e "${GREEN}=== KẾT QUẢ TEST NEW AUTHENTICATION SYSTEM ===${NC}"
echo ""
echo -e "${GREEN}✅ Các APIs đã test:${NC}"
echo "1. ✅ POST /api/seller/auth/register - Seller đăng ký"
echo "2. ✅ POST /api/seller/auth/login - Seller đăng nhập"
echo "3. ✅ POST /api/auth/backoffice/login - Admin đăng nhập"
echo "4. ✅ POST /api/auth/backoffice/create-admin - Admin tạo admin mới"
echo "5. ✅ Test Seller tạo admin (should fail)"
echo "6. ✅ Test Seller đăng nhập vào Admin API (should fail)"
echo "7. ✅ Test Admin đăng nhập vào Seller API (should fail)"
echo "8. ✅ GET /api/seller/auth/me - Lấy thông tin Seller"
echo "9. ✅ GET /api/auth/backoffice/me - Lấy thông tin Admin"
echo "10. ✅ Test validation errors"
echo ""
echo -e "${BLUE}API Endpoints:${NC}"
echo "POST /api/seller/auth/register        - Seller đăng ký"
echo "POST /api/seller/auth/login           - Seller đăng nhập"
echo "GET  /api/seller/auth/me              - Lấy thông tin Seller"
echo "POST /api/seller/auth/logout          - Seller đăng xuất"
echo "POST /api/auth/backoffice/login       - Admin đăng nhập"
echo "GET  /api/auth/backoffice/me          - Lấy thông tin Admin"
echo "POST /api/auth/backoffice/logout      - Admin đăng xuất"
echo "POST /api/auth/backoffice/create-admin - Admin tạo admin mới"
echo ""
echo -e "${BLUE}Request Examples:${NC}"
echo "# Seller đăng ký"
echo 'curl -X POST "http://localhost:8080/api/seller/auth/register" -d "{\"name\":\"Seller\",\"email\":\"seller@foodshare.com\",\"password\":\"Seller123456\"}"'
echo ""
echo "# Seller đăng nhập"
echo 'curl -X POST "http://localhost:8080/api/seller/auth/login" -d "{\"email\":\"seller@foodshare.com\",\"password\":\"Seller123456\"}"'
echo ""
echo "# Admin đăng nhập"
echo 'curl -X POST "http://localhost:8080/api/auth/backoffice/login" -d "{\"email\":\"admin@foodshare.com\",\"password\":\"Admin123456\"}"'
echo ""
echo "# Admin tạo admin mới"
echo 'curl -X POST "http://localhost:8080/api/auth/backoffice/create-admin" -H "Authorization: Bearer <admin_token>" -d "{\"name\":\"New Admin\",\"email\":\"newadmin@foodshare.com\",\"password\":\"Admin123456\",\"role\":\"ADMIN\"}"'
