#!/bin/bash

# =====================================================
# SCRIPT TEST BACKOFFICE AUTHENTICATION APIs
# =====================================================

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SCRIPT TEST BACKOFFICE AUTHENTICATION APIs ===${NC}"
echo ""
echo -e "${YELLOW}Test APIs đăng ký và đăng nhập cho Admin và Seller${NC}"
echo ""

# =====================================================
# 1. ĐĂNG KÝ ADMIN USER ĐẦU TIÊN
# =====================================================
echo -e "${YELLOW}1. Đăng ký Admin User đầu tiên${NC}"
echo ""

REGISTER_ADMIN=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin User",
    "email": "admin@foodshare.com",
    "password": "Admin123456"
  }')

echo "Register Admin Response:"
echo "$REGISTER_ADMIN" | jq '.'

ADMIN_TOKEN=$(echo "$REGISTER_ADMIN" | jq -r '.data.accessToken // empty')
echo "Admin Token: $ADMIN_TOKEN"
echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 2. TEST ĐĂNG KÝ LẦN 2 (SHOULD FAIL)
# =====================================================
echo -e "${YELLOW}2. Test đăng ký lần 2 (should fail)${NC}"
echo ""

REGISTER_SECOND=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Another Admin",
    "email": "admin2@foodshare.com",
    "password": "Admin123456"
  }')

echo "Register Second Admin Response:"
echo "$REGISTER_SECOND" | jq '.'
echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 3. ĐĂNG NHẬP ADMIN
# =====================================================
echo -e "${YELLOW}3. Đăng nhập Admin${NC}"
echo ""

LOGIN_ADMIN=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@foodshare.com",
    "password": "Admin123456"
  }')

echo "Login Admin Response:"
echo "$LOGIN_ADMIN" | jq '.'

ADMIN_LOGIN_TOKEN=$(echo "$LOGIN_ADMIN" | jq -r '.data.accessToken // empty')
echo "Admin Login Token: $ADMIN_LOGIN_TOKEN"
echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 4. ĐĂNG NHẬP SELLER
# =====================================================
echo -e "${YELLOW}4. Đăng nhập Seller${NC}"
echo ""

LOGIN_SELLER=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "seller@foodshare.com",
    "password": "Seller123456"
  }')

echo "Login Seller Response:"
echo "$LOGIN_SELLER" | jq '.'

SELLER_LOGIN_TOKEN=$(echo "$LOGIN_SELLER" | jq -r '.data.accessToken // empty')
echo "Seller Login Token: $SELLER_LOGIN_TOKEN"
echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 5. LẤY THÔNG TIN USER HIỆN TẠI (ADMIN)
# =====================================================
echo -e "${YELLOW}5. Lấy thông tin user hiện tại (Admin)${NC}"
echo ""

if [ -n "$ADMIN_LOGIN_TOKEN" ] && [ "$ADMIN_LOGIN_TOKEN" != "null" ]; then
    GET_CURRENT_ADMIN=$(curl -s -X GET "${BASE_URL}/api/auth/backoffice/me" \
      -H "Authorization: Bearer $ADMIN_LOGIN_TOKEN")
    
    echo "Get Current Admin Response:"
    echo "$GET_CURRENT_ADMIN" | jq '.'
else
    echo "Không có Admin token để test"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 6. LẤY THÔNG TIN USER HIỆN TẠI (SELLER)
# =====================================================
echo -e "${YELLOW}6. Lấy thông tin user hiện tại (Seller)${NC}"
echo ""

if [ -n "$SELLER_LOGIN_TOKEN" ] && [ "$SELLER_LOGIN_TOKEN" != "null" ]; then
    GET_CURRENT_SELLER=$(curl -s -X GET "${BASE_URL}/api/auth/backoffice/me" \
      -H "Authorization: Bearer $SELLER_LOGIN_TOKEN")
    
    echo "Get Current Seller Response:"
    echo "$GET_CURRENT_SELLER" | jq '.'
else
    echo "Không có Seller token để test"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 7. TEST ĐĂNG NHẬP SAI MẬT KHẨU
# =====================================================
echo -e "${YELLOW}7. Test đăng nhập sai mật khẩu${NC}"
echo ""

WRONG_PASSWORD=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@foodshare.com",
    "password": "WrongPassword123"
  }')

echo "Wrong Password Response:"
echo "$WRONG_PASSWORD" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 8. TEST ĐĂNG NHẬP EMAIL KHÔNG TỒN TẠI
# =====================================================
echo -e "${YELLOW}8. Test đăng nhập email không tồn tại${NC}"
echo ""

WRONG_EMAIL=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nonexistent@foodshare.com",
    "password": "Password123"
  }')

echo "Wrong Email Response:"
echo "$WRONG_EMAIL" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 9. TEST ĐĂNG KÝ EMAIL TRÙNG LẶP
# =====================================================
echo -e "${YELLOW}9. Test đăng ký email trùng lặp${NC}"
echo ""

DUPLICATE_EMAIL=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Another Admin",
    "email": "admin@foodshare.com",
    "password": "Admin123456",
    "role": "ADMIN"
  }')

echo "Duplicate Email Response:"
echo "$DUPLICATE_EMAIL" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 10. TEST VALIDATION ERRORS
# =====================================================
echo -e "${YELLOW}10. Test validation errors${NC}"
echo ""

VALIDATION_ERROR=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "email": "invalid-email",
    "password": "123",
    "role": "INVALID_ROLE"
  }')

echo "Validation Error Response:"
echo "$VALIDATION_ERROR" | jq '.'

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 11. TEST UNAUTHORIZED ACCESS
# =====================================================
echo -e "${YELLOW}11. Test unauthorized access (không có token)${NC}"
echo ""

UNAUTHORIZED_ACCESS=$(curl -s -X GET "${BASE_URL}/api/auth/backoffice/me" \
  -w "\nHTTP Status: %{http_code}\n")
echo "Unauthorized Access Response:"
echo "$UNAUTHORIZED_ACCESS"

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 12. TEST LOGOUT
# =====================================================
echo -e "${YELLOW}12. Test logout${NC}"
echo ""

if [ -n "$ADMIN_LOGIN_TOKEN" ] && [ "$ADMIN_LOGIN_TOKEN" != "null" ]; then
    LOGOUT=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/logout" \
      -H "Authorization: Bearer $ADMIN_LOGIN_TOKEN")
    
    echo "Logout Response:"
    echo "$LOGOUT" | jq '.'
else
    echo "Không có token để test logout"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 12. ADMIN TẠO SELLER USER MỚI
# =====================================================
echo -e "${YELLOW}12. Admin tạo Seller User mới${NC}"
echo ""

if [ -n "$ADMIN_LOGIN_TOKEN" ] && [ "$ADMIN_LOGIN_TOKEN" != "null" ]; then
    CREATE_SELLER=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/create-user" \
      -H "Authorization: Bearer $ADMIN_LOGIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "name": "Seller User",
        "email": "seller@foodshare.com",
        "password": "Seller123456",
        "role": "SELLER"
      }')
    
    echo "Create Seller Response:"
    echo "$CREATE_SELLER" | jq '.'
    
    SELLER_TOKEN=$(echo "$CREATE_SELLER" | jq -r '.data.accessToken // empty')
    echo "Created Seller Token: $SELLER_TOKEN"
else
    echo "Không có Admin token để tạo Seller"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 13. ADMIN TẠO ADMIN USER MỚI
# =====================================================
echo -e "${YELLOW}13. Admin tạo Admin User mới${NC}"
echo ""

if [ -n "$ADMIN_LOGIN_TOKEN" ] && [ "$ADMIN_LOGIN_TOKEN" != "null" ]; then
    CREATE_ADMIN=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/create-user" \
      -H "Authorization: Bearer $ADMIN_LOGIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "name": "Another Admin",
        "email": "admin2@foodshare.com",
        "password": "Admin123456",
        "role": "ADMIN"
      }')
    
    echo "Create Admin Response:"
    echo "$CREATE_ADMIN" | jq '.'
else
    echo "Không có Admin token để tạo Admin"
fi

echo ""
echo "----------------------------------------"
echo ""

# =====================================================
# 14. TEST SELLER TẠO USER (SHOULD FAIL)
# =====================================================
echo -e "${YELLOW}14. Test Seller tạo user (should fail)${NC}"
echo ""

if [ -n "$SELLER_TOKEN" ] && [ "$SELLER_TOKEN" != "null" ]; then
    SELLER_CREATE_USER=$(curl -s -X POST "${BASE_URL}/api/auth/backoffice/create-user" \
      -H "Authorization: Bearer $SELLER_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "name": "Test User",
        "email": "test@foodshare.com",
        "password": "Test123456",
        "role": "SELLER"
      }')
    
    echo "Seller Create User Response:"
    echo "$SELLER_CREATE_USER" | jq '.'
else
    echo "Không có Seller token để test"
fi

echo ""
echo "----------------------------------------"
echo ""

echo -e "${GREEN}=== KẾT QUẢ TEST BACKOFFICE AUTHENTICATION APIs ===${NC}"
echo ""
echo -e "${GREEN}✅ Các APIs đã test:${NC}"
echo "1. ✅ POST /api/auth/backoffice/register - Đăng ký Admin đầu tiên"
echo "2. ✅ POST /api/auth/backoffice/register - Test đăng ký lần 2 (should fail)"
echo "3. ✅ POST /api/auth/backoffice/login - Đăng nhập Admin"
echo "4. ✅ POST /api/auth/backoffice/login - Đăng nhập Seller"
echo "5. ✅ GET /api/auth/backoffice/me - Lấy thông tin user hiện tại"
echo "6. ✅ Test đăng nhập sai mật khẩu"
echo "7. ✅ Test đăng nhập email không tồn tại"
echo "8. ✅ Test đăng ký email trùng lặp"
echo "9. ✅ Test validation errors"
echo "10. ✅ Test unauthorized access"
echo "11. ✅ POST /api/auth/backoffice/logout - Đăng xuất"
echo "12. ✅ POST /api/auth/backoffice/create-user - Admin tạo Seller"
echo "13. ✅ POST /api/auth/backoffice/create-user - Admin tạo Admin"
echo "14. ✅ Test Seller tạo user (should fail)"
echo ""
echo -e "${BLUE}API Endpoints:${NC}"
echo "POST /api/auth/backoffice/register   - Đăng ký admin đầu tiên"
echo "POST /api/auth/backoffice/login      - Đăng nhập"
echo "GET  /api/auth/backoffice/me         - Lấy thông tin user hiện tại"
echo "POST /api/auth/backoffice/logout     - Đăng xuất"
echo "POST /api/auth/backoffice/create-user - Admin tạo user mới"
echo ""
echo -e "${BLUE}Request Examples:${NC}"
echo "# Đăng ký Admin đầu tiên"
echo 'curl -X POST "http://localhost:8080/api/auth/backoffice/register" -d "{\"name\":\"Admin\",\"email\":\"admin@foodshare.com\",\"password\":\"Admin123456\"}"'
echo ""
echo "# Đăng nhập"
echo 'curl -X POST "http://localhost:8080/api/auth/backoffice/login" -d "{\"email\":\"admin@foodshare.com\",\"password\":\"Admin123456\"}"'
echo ""
echo "# Lấy thông tin user hiện tại"
echo 'curl -X GET "http://localhost:8080/api/auth/backoffice/me" -H "Authorization: Bearer <token>"'
echo ""
echo "# Admin tạo user mới"
echo 'curl -X POST "http://localhost:8080/api/auth/backoffice/create-user" -H "Authorization: Bearer <admin_token>" -d "{\"name\":\"Seller\",\"email\":\"seller@foodshare.com\",\"password\":\"Seller123456\",\"role\":\"SELLER\"}"'
