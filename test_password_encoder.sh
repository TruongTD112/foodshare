#!/bin/bash

# Test script để kiểm tra password encoder

BASE_URL="http://localhost:8080"

echo "=== TESTING PASSWORD ENCODER ==="
echo

# Test 1: Đăng ký seller với password mới
echo "1. Testing seller registration with password encoding:"
REGISTER_RESPONSE=$(curl -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Seller",
    "email": "testseller@foodshare.com",
    "password": "TestPassword123"
  }' \
  -s)

echo "Register response: $REGISTER_RESPONSE"
echo

# Test 2: Đăng nhập với password đã encode
echo "2. Testing login with encoded password:"
LOGIN_RESPONSE=$(curl -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testseller@foodshare.com",
    "password": "TestPassword123"
  }' \
  -s)

echo "Login response: $LOGIN_RESPONSE"
echo

# Test 3: Đăng nhập với password sai
echo "3. Testing login with wrong password:"
WRONG_LOGIN_RESPONSE=$(curl -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testseller@foodshare.com",
    "password": "WrongPassword123"
  }' \
  -s)

echo "Wrong login response: $WRONG_LOGIN_RESPONSE"
echo

# Test 4: Đăng nhập admin
echo "4. Testing admin login:"
ADMIN_LOGIN_RESPONSE=$(curl -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@foodshare.com",
    "password": "Admin123456"
  }' \
  -s)

echo "Admin login response: $ADMIN_LOGIN_RESPONSE"
echo

# Extract admin token
ADMIN_TOKEN=$(echo $ADMIN_LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -n "$ADMIN_TOKEN" ]; then
  echo "Admin token extracted: $ADMIN_TOKEN"
  
  # Test 5: Tạo admin mới với password encoding
  echo "5. Testing create admin with password encoding:"
  CREATE_ADMIN_RESPONSE=$(curl -X POST "$BASE_URL/api/admin/create-admin" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{
      "name": "New Admin",
      "email": "newadmin@foodshare.com",
      "password": "NewAdminPassword123",
      "role": "ADMIN"
    }' \
    -s)
  
  echo "Create admin response: $CREATE_ADMIN_RESPONSE"
  echo
fi

echo "=== TEST COMPLETED ==="
