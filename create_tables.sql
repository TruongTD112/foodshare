-- =====================================================
-- TẠO BẢNG CHO FOODSHARE APP
-- =====================================================

-- 1. Tạo bảng Product (nếu chưa có)
CREATE TABLE IF NOT EXISTS Product (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shop_id INT NOT NULL,
    category_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2) NULL,
    image_url VARCHAR(255),
    quantity_available INT,
    quantity_pending INT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Tạo bảng Shop (nếu chưa có)
CREATE TABLE IF NOT EXISTS Shop (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    description TEXT,
    rating DECIMAL(3,2),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. Tạo bảng Order (nếu chưa có)
CREATE TABLE IF NOT EXISTS `Order` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    shop_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    pickup_time DATETIME,
    expires_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 4. Tạo bảng ProductSalesStats (BẢNG MỚI)
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

-- 5. Tạo bảng Category (nếu chưa có)
CREATE TABLE IF NOT EXISTS Category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT '1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 6. Tạo bảng CustomerUser (nếu chưa có)
CREATE TABLE IF NOT EXISTS CustomerUser (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    status VARCHAR(50) NOT NULL DEFAULT '1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 7. Tạo các INDEX để tối ưu performance
CREATE INDEX idx_product_shop_id ON Product(shop_id);
CREATE INDEX idx_product_status ON Product(status);
CREATE INDEX idx_product_original_price ON Product(original_price);
CREATE INDEX idx_product_price ON Product(price);

CREATE INDEX idx_shop_status ON Shop(status);
CREATE INDEX idx_shop_location ON Shop(latitude, longitude);

CREATE INDEX idx_order_user_id ON `Order`(user_id);
CREATE INDEX idx_order_shop_id ON `Order`(shop_id);
CREATE INDEX idx_order_product_id ON `Order`(product_id);
CREATE INDEX idx_order_status ON `Order`(status);
CREATE INDEX idx_order_created_at ON `Order`(created_at);

CREATE INDEX idx_stats_product_id ON ProductSalesStats(product_id);
CREATE INDEX idx_stats_total_quantity ON ProductSalesStats(total_quantity_sold DESC);
CREATE INDEX idx_stats_last_sold ON ProductSalesStats(last_sold_at DESC);

-- 8. Thêm dữ liệu mẫu để test
INSERT INTO Category (name, description, status) VALUES 
('Đồ ăn nhanh', 'Pizza, Burger, Gà rán', '1'),
('Đồ uống', 'Nước ngọt, Cà phê, Trà', '1'),
('Tráng miệng', 'Bánh ngọt, Kem, Chè', '1');

INSERT INTO Shop (name, address, latitude, longitude, description, rating, status) VALUES 
('Pizza Corner', '123 Đường ABC, Quận 1, TP.HCM', 10.762622, 106.660172, 'Pizza ngon nhất thành phố', 4.5, '1'),
('Burger King', '456 Đường XYZ, Quận 2, TP.HCM', 10.763000, 106.661000, 'Burger thơm ngon', 4.2, '1'),
('Cafe Central', '789 Đường DEF, Quận 3, TP.HCM', 10.764000, 106.662000, 'Cà phê chất lượng cao', 4.8, '1');

INSERT INTO Product (shop_id, category_id, name, description, price, original_price, quantity_available, status) VALUES 
(1, 1, 'Pizza Margherita', 'Pizza cổ điển với phô mai mozzarella và cà chua', 120000, 150000, 50, '1'),
(1, 1, 'Pizza Pepperoni', 'Pizza với pepperoni và phô mai thơm ngon', 140000, 180000, 30, '1'),
(1, 1, 'Pizza Hải Sản', 'Pizza với tôm, mực và cua tươi', 160000, 200000, 25, '1'),
(2, 1, 'Burger Deluxe', 'Burger với thịt bò và rau tươi', 80000, 100000, 40, '1'),
(2, 1, 'Chicken Burger', 'Burger gà rán giòn', 70000, 90000, 35, '1'),
(2, 1, 'Fish Burger', 'Burger cá tươi', 75000, 95000, 20, '1'),
(3, 2, 'Cà phê đen', 'Cà phê đen đậm đà', 25000, 30000, 100, '1'),
(3, 2, 'Cà phê sữa', 'Cà phê sữa ngọt ngào', 30000, 35000, 80, '1'),
(3, 3, 'Bánh Tiramisu', 'Bánh tiramisu Ý thơm ngon', 45000, 55000, 15, '1');

INSERT INTO CustomerUser (name, email, phone, address, status) VALUES 
('Nguyễn Văn A', 'nguyenvana@email.com', '0123456789', '123 Đường ABC, Quận 1', '1'),
('Trần Thị B', 'tranthib@email.com', '0987654321', '456 Đường XYZ, Quận 2', '1'),
('Lê Văn C', 'levanc@email.com', '0369258147', '789 Đường DEF, Quận 3', '1');

-- 9. Thêm dữ liệu thống kê mẫu
INSERT INTO ProductSalesStats (product_id, total_quantity_sold, total_orders, last_sold_at) VALUES 
(1, 25, 10, NOW()),
(2, 15, 8, NOW()),
(3, 20, 12, NOW()),
(4, 30, 15, NOW()),
(5, 18, 9, NOW()),
(6, 12, 6, NOW()),
(7, 50, 25, NOW()),
(8, 35, 18, NOW()),
(9, 8, 5, NOW());

-- 10. Kiểm tra dữ liệu đã tạo
SELECT 'Tables created successfully!' as status;
SELECT COUNT(*) as product_count FROM Product;
SELECT COUNT(*) as shop_count FROM Shop;
SELECT COUNT(*) as order_count FROM `Order`;
SELECT COUNT(*) as stats_count FROM ProductSalesStats;
SELECT COUNT(*) as category_count FROM Category;
SELECT COUNT(*) as user_count FROM CustomerUser;
