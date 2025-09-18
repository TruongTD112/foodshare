-- =====================================================
-- MIGRATION SCRIPT: Thêm cột giá vào bảng Order
-- =====================================================

-- Thêm cột unit_price và total_price vào bảng Order
ALTER TABLE `Order` 
ADD COLUMN `unit_price` DECIMAL(10,2) AFTER `expires_at`,
ADD COLUMN `total_price` DECIMAL(10,2) AFTER `unit_price`;

-- Cập nhật dữ liệu mẫu cho các đơn hàng hiện có (nếu có)
-- Lưu ý: Cần cập nhật giá dựa trên giá sản phẩm hiện tại
UPDATE `Order` o
JOIN `Product` p ON o.product_id = p.id
SET 
    o.unit_price = p.price,
    o.total_price = p.price * o.quantity
WHERE o.unit_price IS NULL OR o.total_price IS NULL;

-- Thêm comment cho các cột mới
ALTER TABLE `Order` 
MODIFY COLUMN `unit_price` DECIMAL(10,2) COMMENT 'Giá trên từng sản phẩm tại thời điểm đặt',
MODIFY COLUMN `total_price` DECIMAL(10,2) COMMENT 'Tổng giá của đơn hàng';

-- Kiểm tra kết quả
SELECT 
    id,
    product_id,
    quantity,
    unit_price,
    total_price,
    pickup_time,
    status
FROM `Order` 
ORDER BY id DESC 
LIMIT 5;
