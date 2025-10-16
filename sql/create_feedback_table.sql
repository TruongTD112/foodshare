-- Tạo bảng Feedback
CREATE TABLE Feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL COMMENT 'Nội dung feedback',
    user_id INT NULL COMMENT 'ID người dùng (có thể null nếu không đăng nhập)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo'
);

-- Tạo index cho user_id để tối ưu truy vấn
CREATE INDEX idx_feedback_user_id ON Feedback(user_id);

-- Tạo index cho created_at để tối ưu truy vấn theo thời gian
CREATE INDEX idx_feedback_created_at ON Feedback(created_at);
