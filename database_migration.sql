-- =====================================================
-- DATABASE MIGRATION SCRIPT FOR FOODSHARE APP
-- =====================================================

-- 1. Thêm cột original_price vào bảng Product
ALTER TABLE Product ADD COLUMN original_price DECIMAL(10,2) NULL;

-- 2. Tạo bảng ProductSalesStats để thống kê số lượng bán
CREATE TABLE ProductSalesStats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL UNIQUE,
    total_quantity_sold INT NOT NULL DEFAULT 0,
    total_orders INT NOT NULL DEFAULT 0,
    last_sold_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE CASCADE,
    INDEX idx_product_id (product_id),
    INDEX idx_total_quantity_sold (total_quantity_sold DESC),
    INDEX idx_last_sold_at (last_sold_at DESC)
);

-- 3. Khởi tạo dữ liệu thống kê từ Order hiện có (nếu có)
-- Chỉ chạy nếu đã có dữ liệu Order
INSERT INTO ProductSalesStats (product_id, total_quantity_sold, total_orders, last_sold_at)
SELECT 
    product_id,
    SUM(quantity) as total_quantity_sold,
    COUNT(*) as total_orders,
    MAX(updated_at) as last_sold_at
FROM `Order` 
WHERE status = 'completed'
GROUP BY product_id
ON DUPLICATE KEY UPDATE
    total_quantity_sold = VALUES(total_quantity_sold),
    total_orders = VALUES(total_orders),
    last_sold_at = VALUES(last_sold_at);

-- 4. Tạo trigger để tự động cập nhật stats khi Order completed
-- (Tùy chọn - có thể dùng application logic thay vì trigger)

DELIMITER $$

CREATE TRIGGER tr_order_completed_stats
AFTER UPDATE ON `Order`
FOR EACH ROW
BEGIN
    -- Chỉ cập nhật khi status chuyển từ pending sang completed
    IF OLD.status = 'pending' AND NEW.status = 'completed' THEN
        INSERT INTO ProductSalesStats (product_id, total_quantity_sold, total_orders, last_sold_at)
        VALUES (NEW.product_id, NEW.quantity, 1, NOW())
        ON DUPLICATE KEY UPDATE
            total_quantity_sold = total_quantity_sold + NEW.quantity,
            total_orders = total_orders + 1,
            last_sold_at = NOW();
    END IF;
END$$

DELIMITER ;

-- 5. Tạo view để dễ dàng query sản phẩm bán chạy
CREATE VIEW v_popular_products AS
SELECT 
    p.id,
    p.shop_id,
    p.category_id,
    p.name,
    p.description,
    p.price,
    p.original_price,
    p.image_url,
    p.quantity_available,
    p.quantity_pending,
    p.status,
    p.created_at,
    p.updated_at,
    COALESCE(s.total_quantity_sold, 0) as total_quantity_sold,
    COALESCE(s.total_orders, 0) as total_orders,
    s.last_sold_at,
    CASE 
        WHEN p.original_price IS NOT NULL AND p.original_price > p.price 
        THEN ROUND(((p.original_price - p.price) / p.original_price) * 100, 2)
        ELSE 0 
    END as discount_percentage
FROM Product p
LEFT JOIN ProductSalesStats s ON p.id = s.product_id
WHERE p.status = '1';

-- 6. Tạo index để tối ưu performance
CREATE INDEX idx_product_status ON Product(status);
CREATE INDEX idx_product_original_price ON Product(original_price);
CREATE INDEX idx_order_status ON `Order`(status);
CREATE INDEX idx_order_product_id ON `Order`(product_id);

-- 7. Sample data để test (tùy chọn)
-- INSERT INTO Product (shop_id, category_id, name, description, price, original_price, quantity_available, status)
-- VALUES 
--     (1, 1, 'Pizza Margherita', 'Pizza cổ điển với phô mai mozzarella', 120000, 150000, 50, '1'),
--     (1, 1, 'Pizza Pepperoni', 'Pizza với pepperoni và phô mai', 140000, 180000, 30, '1'),
--     (2, 1, 'Burger Deluxe', 'Burger với thịt bò và rau tươi', 80000, 100000, 40, '1'),
--     (2, 1, 'Chicken Burger', 'Burger gà rán giòn', 70000, 90000, 25, '1');

-- 8. Query để kiểm tra dữ liệu
-- SELECT * FROM v_popular_products ORDER BY total_quantity_sold DESC LIMIT 10;

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Kiểm tra cấu trúc bảng Product
-- DESCRIBE Product;

-- Kiểm tra cấu trúc bảng ProductSalesStats  
-- DESCRIBE ProductSalesStats;

-- Kiểm tra view popular products
-- SHOW CREATE VIEW v_popular_products;

-- Kiểm tra trigger
-- SHOW TRIGGERS LIKE 'Order';

-- =====================================================
-- CLEANUP QUERIES (chỉ chạy khi cần xóa)
-- =====================================================

-- Xóa trigger (nếu cần)
-- DROP TRIGGER IF EXISTS tr_order_completed_stats;

-- Xóa view (nếu cần)
-- DROP VIEW IF EXISTS v_popular_products;

-- Xóa bảng stats (nếu cần)
-- DROP TABLE IF EXISTS ProductSalesStats;

-- Xóa cột original_price (nếu cần)
-- ALTER TABLE Product DROP COLUMN original_price;
