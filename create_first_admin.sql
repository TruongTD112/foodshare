-- =====================================================
-- SCRIPT TẠO ADMIN ĐẦU TIÊN
-- =====================================================

-- Thêm cột role vào bảng BackOffice_User (nếu chưa có)
ALTER TABLE BackOffice_User 
ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'SELLER';

-- Tạo admin đầu tiên
INSERT INTO BackOffice_User (name, email, password_hash, role) 
VALUES (
    'Super Admin', 
    'admin@foodshare.com', 
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- Password: Admin123456
    'ADMIN'
);

-- Tạo index cho cột role
CREATE INDEX IF NOT EXISTS idx_backoffice_user_role ON BackOffice_User(role);

-- Kiểm tra admin đã được tạo
SELECT id, name, email, role, created_at 
FROM BackOffice_User 
WHERE role = 'ADMIN';

-- =====================================================
-- HƯỚNG DẪN SỬ DỤNG
-- =====================================================
-- 1. Chạy script này để tạo admin đầu tiên
-- 2. Đăng nhập bằng: admin@foodshare.com / Admin123456
-- 3. Sử dụng API /api/auth/backoffice/create-user để tạo user khác
