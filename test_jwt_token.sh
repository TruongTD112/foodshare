#!/bin/bash

# Test script để kiểm tra JWT token generation

BASE_URL="http://localhost:8080"

echo "=== TESTING JWT TOKEN GENERATION ==="
echo

# Test 1: Đăng nhập admin
echo "1. Testing admin login (should generate JWT token):"
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

# Test 2: Decode JWT token (nếu có jq)
echo "2. Decoding JWT token (if jq is available):"
if command -v jq &> /dev/null; then
  # Decode header
  echo "Header:"
  echo $TOKEN | cut -d. -f1 | base64 -d 2>/dev/null | jq 2>/dev/null || echo "Could not decode header"
  echo
  
  # Decode payload
  echo "Payload:"
  echo $TOKEN | cut -d. -f2 | base64 -d 2>/dev/null | jq 2>/dev/null || echo "Could not decode payload"
  echo
else
  echo "jq not available, skipping token decode"
fi

# Test 3: Test admin API với token
echo "3. Testing admin API with generated token:"
curl -X GET "$BASE_URL/api/admin/shops" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s
echo
echo

# Test 4: Test tạo admin mới
echo "4. Testing create admin with generated token:"
curl -X POST "$BASE_URL/api/admin/create-admin" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Test Admin 2",
    "email": "testadmin2@foodshare.com",
    "password": "Test123456",
    "role": "ADMIN"
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s
echo
echo

echo "=== TEST COMPLETED ==="
