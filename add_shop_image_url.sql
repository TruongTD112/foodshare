-- =====================================================
-- THÊM CỘT IMAGE_URL VÀO BẢNG SHOP
-- =====================================================

-- Thêm cột image_url vào bảng Shop
ALTER TABLE Shop ADD COLUMN image_url VARCHAR(255) NULL;

-- Cập nhật dữ liệu mẫu với ảnh cửa hàng
UPDATE Shop SET image_url = 'https://example.com/images/pizza-corner-shop.jpg' WHERE id = 1;
UPDATE Shop SET image_url = 'https://example.com/images/burger-king-shop.jpg' WHERE id = 2;
UPDATE Shop SET image_url = 'https://example.com/images/cafe-central-shop.jpg' WHERE id = 3;

-- Kiểm tra dữ liệu đã cập nhật
SELECT id, name, address, phone, image_url, latitude, longitude FROM Shop;

-- Thông báo hoàn thành
SELECT 'Shop image_url column added successfully!' as status;
