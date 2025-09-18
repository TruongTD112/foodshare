#!/bin/bash

# =====================================================
# CURL ĐẶT ĐƠN HÀNG - TEST NHANH
# =====================================================

BASE_URL="http://localhost:8080"

echo "=== ĐẶT ĐƠN HÀNG PIZZA MARGHERITA ==="
echo ""

# Đặt đơn hàng Pizza Margherita
curl -X POST "${BASE_URL}/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user_1" \
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

echo ""
echo "=== XEM DANH SÁCH ĐƠN HÀNG ==="
echo ""

# Xem danh sách đơn hàng
curl -X GET "${BASE_URL}/orders" \
  -H "Authorization: Bearer user_1" \
  -w "\n\nHTTP Status: %{http_code}\n" \
  -s | jq '.'
