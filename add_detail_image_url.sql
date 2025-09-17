-- =====================================================
-- THÊM CỘT DETAIL_IMAGE_URL VÀO BẢNG PRODUCT
-- =====================================================

-- Thêm cột detail_image_url vào bảng Product
ALTER TABLE Product ADD COLUMN detail_image_url TEXT NULL;

-- Cập nhật dữ liệu mẫu với ảnh chi tiết
UPDATE Product SET detail_image_url = 'https://example.com/images/pizza-margherita-detail1.jpg,https://example.com/images/pizza-margherita-detail2.jpg,https://example.com/images/pizza-margherita-detail3.jpg' WHERE id = 1;
UPDATE Product SET detail_image_url = 'https://example.com/images/pizza-pepperoni-detail1.jpg,https://example.com/images/pizza-pepperoni-detail2.jpg' WHERE id = 2;
UPDATE Product SET detail_image_url = 'https://example.com/images/pizza-haisan-detail1.jpg,https://example.com/images/pizza-haisan-detail2.jpg,https://example.com/images/pizza-haisan-detail3.jpg,https://example.com/images/pizza-haisan-detail4.jpg' WHERE id = 3;
UPDATE Product SET detail_image_url = 'https://example.com/images/burger-deluxe-detail1.jpg,https://example.com/images/burger-deluxe-detail2.jpg' WHERE id = 4;
UPDATE Product SET detail_image_url = 'https://example.com/images/chicken-burger-detail1.jpg,https://example.com/images/chicken-burger-detail2.jpg,https://example.com/images/chicken-burger-detail3.jpg' WHERE id = 5;
UPDATE Product SET detail_image_url = 'https://example.com/images/fish-burger-detail1.jpg,https://example.com/images/fish-burger-detail2.jpg' WHERE id = 6;
UPDATE Product SET detail_image_url = 'https://example.com/images/cafe-den-detail1.jpg,https://example.com/images/cafe-den-detail2.jpg' WHERE id = 7;
UPDATE Product SET detail_image_url = 'https://example.com/images/cafe-sua-detail1.jpg,https://example.com/images/cafe-sua-detail2.jpg,https://example.com/images/cafe-sua-detail3.jpg' WHERE id = 8;
UPDATE Product SET detail_image_url = 'https://example.com/images/banh-tiramisu-detail1.jpg,https://example.com/images/banh-tiramisu-detail2.jpg,https://example.com/images/banh-tiramisu-detail3.jpg,https://example.com/images/banh-tiramisu-detail4.jpg,https://example.com/images/banh-tiramisu-detail5.jpg' WHERE id = 9;

-- Kiểm tra dữ liệu đã cập nhật
SELECT id, name, image_url, detail_image_url FROM Product;

-- Thông báo hoàn thành
SELECT 'Detail image URL column added successfully!' as status;
