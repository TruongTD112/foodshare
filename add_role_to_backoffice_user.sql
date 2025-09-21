-- =====================================================
-- SCRIPT THÊM ROLE VÀO BẢNG BACKOFFICE_USER
-- =====================================================

-- Thêm cột role vào bảng BackOffice_User
ALTER TABLE BackOffice_User 
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'SELLER';

-- Cập nhật role cho các user hiện có (có thể điều chỉnh theo nhu cầu)
-- Mặc định tất cả user hiện có sẽ là SELLER
-- Bạn có thể thay đổi một số user thành ADMIN nếu cần

-- Ví dụ: Cập nhật user đầu tiên thành ADMIN
-- UPDATE BackOffice_User SET role = 'ADMIN' WHERE id = 1;

-- Tạo index cho cột role để tối ưu query
CREATE INDEX idx_backoffice_user_role ON BackOffice_User(role);

-- =====================================================
-- SCRIPT THÊM ROLE VÀO BẢNG CUSTOMER_USER (nếu cần)
-- =====================================================

-- Thêm cột role vào bảng Customer_User
ALTER TABLE Customer_User 
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER';

-- Cập nhật role cho các customer hiện có
UPDATE Customer_User SET role = 'CUSTOMER' WHERE role IS NULL;

-- Tạo index cho cột role
CREATE INDEX idx_customer_user_role ON Customer_User(role);

-- =====================================================
-- SCRIPT TẠO DỮ LIỆU MẪU
-- =====================================================

-- Tạo admin user mẫu
INSERT INTO BackOffice_User (name, email, password_hash, role) 
VALUES ('Admin User', 'admin@foodshare.com', '$2a$10$example_hash', 'ADMIN');

-- Tạo seller user mẫu
INSERT INTO BackOffice_User (name, email, password_hash, role) 
VALUES ('Seller User', 'seller@foodshare.com', '$2a$10$example_hash', 'SELLER');

-- Liên kết seller với shop (ví dụ shop_id = 1)
INSERT INTO Shop_Member (shop_id, backoffice_user_id, role) 
VALUES (1, (SELECT id FROM BackOffice_User WHERE email = 'seller@foodshare.com'), 'OWNER');
