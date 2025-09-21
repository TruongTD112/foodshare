#!/bin/bash

# Test script để kiểm tra authentication cho API admin

BASE_URL="http://localhost:8080"

echo "=== TESTING ADMIN API AUTHENTICATION ==="
echo

# Test 1: Truy cập admin API không có token
echo "1. Testing admin API without token (should return 401/403):"
curl -X GET "$BASE_URL/api/admin/shops" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s
echo
echo

# Test 2: Truy cập admin API với token không hợp lệ
echo "2. Testing admin API with invalid token (should return 401/403):"
curl -X GET "$BASE_URL/api/admin/shops" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer invalid_token" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s
echo
echo

# Test 3: Đăng nhập để lấy token
echo "3. Login to get admin token:"
LOGIN_RESPONSE=$(curl -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@foodshare.com",
    "password": "Admin123456"
  }' \
  -s)

echo "Login response: $LOGIN_RESPONSE"
echo

# Extract token from response
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "ERROR: Could not extract token from login response"
  exit 1
fi

echo "Extracted token: $TOKEN"
echo

# Test 4: Truy cập admin API với token hợp lệ
echo "4. Testing admin API with valid admin token (should return 200):"
curl -X GET "$BASE_URL/api/admin/shops" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s
echo
echo

# Test 5: Test tạo admin mới (cần admin role)
echo "5. Testing create admin with valid token (should return 200):"
curl -X POST "$BASE_URL/api/admin/create-admin" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Test Admin",
    "email": "testadmin@foodshare.com",
    "password": "Test123456",
    "role": "ADMIN"
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s
echo
echo

# Test 6: Test với seller token (nếu có)
echo "6. Testing admin API with seller token (should return 403):"
# Note: Cần có seller account để test này
echo "Skipping seller test - need seller account"
echo

echo "=== TEST COMPLETED ==="
