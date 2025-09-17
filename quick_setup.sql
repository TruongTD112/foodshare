-- =====================================================
-- QUICK SETUP SCRIPT - CHẠY NHANH
-- =====================================================

-- 1. Thêm cột original_price vào Product
ALTER TABLE Product ADD COLUMN original_price DECIMAL(10,2) NULL;

-- 2. Tạo bảng ProductSalesStats
CREATE TABLE ProductSalesStats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL UNIQUE,
    total_quantity_sold INT NOT NULL DEFAULT 0,
    total_orders INT NOT NULL DEFAULT 0,
    last_sold_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE CASCADE
);

-- 3. Tạo index để tối ưu
CREATE INDEX idx_total_quantity_sold ON ProductSalesStats(total_quantity_sold DESC);
CREATE INDEX idx_product_status ON Product(status);

-- 4. Khởi tạo dữ liệu từ Order hiện có (nếu có)
INSERT INTO ProductSalesStats (product_id, total_quantity_sold, total_orders, last_sold_at)
SELECT 
    product_id,
    SUM(quantity) as total_quantity_sold,
    COUNT(*) as total_orders,
    MAX(updated_at) as last_sold_at
FROM `Order` 
WHERE status = 'completed'
GROUP BY product_id;

-- 5. Kiểm tra kết quả
SELECT 'Setup completed successfully!' as status;
SELECT COUNT(*) as product_count FROM Product;
SELECT COUNT(*) as stats_count FROM ProductSalesStats;
