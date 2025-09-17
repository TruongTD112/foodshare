# Hướng Dẫn Setup Database

## 🚀 **Cách 1: Chạy nhanh (Khuyến nghị)**

```bash
# Chạy file setup nhanh
mysql -u your_username -p your_database_name < quick_setup.sql
```

## 🔧 **Cách 2: Chạy từng lệnh**

### **Bước 1: Thêm cột original_price**
```sql
ALTER TABLE Product ADD COLUMN original_price DECIMAL(10,2) NULL;
```

### **Bước 2: Tạo bảng ProductSalesStats**
```sql
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
```

### **Bước 3: Tạo index tối ưu**
```sql
CREATE INDEX idx_total_quantity_sold ON ProductSalesStats(total_quantity_sold DESC);
CREATE INDEX idx_product_status ON Product(status);
```

### **Bước 4: Khởi tạo dữ liệu (nếu có Order)**
```sql
INSERT INTO ProductSalesStats (product_id, total_quantity_sold, total_orders, last_sold_at)
SELECT 
    product_id,
    SUM(quantity) as total_quantity_sold,
    COUNT(*) as total_orders,
    MAX(updated_at) as last_sold_at
FROM `Order` 
WHERE status = 'completed'
GROUP BY product_id;
```

## ✅ **Kiểm tra setup thành công**

```sql
-- Kiểm tra cấu trúc bảng Product
DESCRIBE Product;

-- Kiểm tra bảng ProductSalesStats
DESCRIBE ProductSalesStats;

-- Kiểm tra dữ liệu
SELECT COUNT(*) as product_count FROM Product;
SELECT COUNT(*) as stats_count FROM ProductSalesStats;
```

## 🧪 **Test dữ liệu mẫu**

```sql
-- Thêm sản phẩm test
INSERT INTO Product (shop_id, category_id, name, description, price, original_price, quantity_available, status)
VALUES 
    (1, 1, 'Pizza Margherita', 'Pizza cổ điển', 120000, 150000, 50, '1'),
    (1, 1, 'Pizza Pepperoni', 'Pizza với pepperoni', 140000, 180000, 30, '1'),
    (2, 1, 'Burger Deluxe', 'Burger thịt bò', 80000, 100000, 40, '1');

-- Thêm stats test
INSERT INTO ProductSalesStats (product_id, total_quantity_sold, total_orders, last_sold_at)
VALUES 
    (1, 25, 10, NOW()),
    (2, 15, 8, NOW()),
    (3, 30, 12, NOW());
```

## 🔄 **Rollback (nếu cần)**

```sql
-- Xóa bảng ProductSalesStats
DROP TABLE IF EXISTS ProductSalesStats;

-- Xóa cột original_price
ALTER TABLE Product DROP COLUMN original_price;
```

## 📊 **Các file SQL có sẵn:**

1. **`quick_setup.sql`** - Setup nhanh (khuyến nghị)
2. **`database_migration.sql`** - Setup đầy đủ với trigger và view
3. **`SQL_SETUP_GUIDE.md`** - Hướng dẫn này

## ⚠️ **Lưu ý quan trọng:**

- Backup database trước khi chạy migration
- Kiểm tra tên database và user trong lệnh mysql
- Chạy từng bước để tránh lỗi
- Test trên môi trường dev trước khi chạy production
