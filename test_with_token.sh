#!/bin/bash

# =====================================================
# SCRIPT TEST NHANH VỚI TOKEN
# =====================================================

BASE_URL="http://localhost:8080"

echo "=== TẠO TOKEN VÀ TEST API ==="
echo ""

# Tạo token cho user 1
echo "1. Tạo token cho User 1..."
TOKEN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/social" \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "providerId": "google_123",
    "email": "user1@example.com",
    "name": "User 1",
    "profilePictureUrl": "https://example.com/avatar1.jpg"
  }')

echo "Response:"
echo "$TOKEN_RESPONSE" | jq '.'

# Extract token
TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.data.token // empty')

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    echo ""
    echo "✅ Token tạo thành công!"
    echo "Token: $TOKEN"
    echo ""
    
    echo "2. Test đặt đơn hàng với token..."
    curl -X POST "${BASE_URL}/orders" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d '{
        "shopId": 1,
        "productId": 1,
        "quantity": 2,
        "pickupTime": "2024-01-15T14:30:00",
        "unitPrice": 150000.00,
        "totalPrice": 300000.00
      }' \
      -w "\n\nHTTP Status: %{http_code}\n" \
      -s | jq '.'
else
    echo "❌ Không thể tạo token"
fi
