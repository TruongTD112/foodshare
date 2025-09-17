-- =====================================================
-- THÊM CỘT PHONE VÀO BẢNG SHOP
-- =====================================================

-- Thêm cột phone vào bảng Shop
ALTER TABLE Shop ADD COLUMN phone VARCHAR(20) NULL;

-- Cập nhật dữ liệu mẫu với số điện thoại
UPDATE Shop SET phone = '0123456789' WHERE id = 1;
UPDATE Shop SET phone = '0987654321' WHERE id = 2;
UPDATE Shop SET phone = '0369258147' WHERE id = 3;

-- Kiểm tra dữ liệu đã cập nhật
SELECT id, name, address, phone, latitude, longitude FROM Shop;

-- Thông báo hoàn thành
SELECT 'Shop phone column added successfully!' as status;
