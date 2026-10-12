-- 在本地数据库配置指定的 study 数据库中执行。
-- 请先创建并选中数据库；本脚本不迁移已有表结构。
-- 可重复执行，但会更新部分示例数据；已有学习数据时请先检查。

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(80) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS books (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(32) NOT NULL UNIQUE,
    title VARCHAR(120) NOT NULL,
    author VARCHAR(80) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_books_available (available)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_no VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    class_name VARCHAR(80) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS loans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    borrowed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    returned_at DATETIME NULL,
    INDEX idx_loans_book_active (book_id, returned_at),
    INDEX idx_loans_student_active (student_id, returned_at),
    CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE RESTRICT,
    CONSTRAINT fk_loans_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users (username, password_hash, display_name)
VALUES ('admin', 'pbkdf2$120000$amF2YXdlYi1kZW1vLTAwMQ==$xeyQFBELHIxgug8O3ZURQnOCRqbpsyyvTHaqMWeQr6M=', '图书管理员')
ON DUPLICATE KEY UPDATE display_name = VALUES(display_name);

INSERT INTO books (isbn, title, author, available) VALUES
('978-7-111-54275-9', '深入理解 Java 虚拟机', '周志明', TRUE),
('978-7-121-42883-8', 'Java 核心技术 卷 I', 'Cay S. Horstmann', TRUE),
('978-7-115-55449-6', 'HTTP 权威指南', 'David Gourley', TRUE)
ON DUPLICATE KEY UPDATE title = VALUES(title), author = VALUES(author);

INSERT INTO students (student_no, name, class_name) VALUES
('20260001', '林晓', '软件工程 1 班'),
('20260002', '陈宇', '软件工程 1 班'),
('20260003', '周然', '计算机科学 2 班')
ON DUPLICATE KEY UPDATE name = VALUES(name), class_name = VALUES(class_name);
