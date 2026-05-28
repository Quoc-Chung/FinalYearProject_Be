
INSERT INTO roles (name, description)
SELECT 'USER', 'Default user role'
    WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'USER');

INSERT INTO roles (name, description)
SELECT 'ADMIN', 'Administrator role'
    WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

INSERT INTO users (email, password, full_name, status, created_at)
SELECT
    'admin@techcycle.com',
    '$2a$10$1QaD7SldE3V8Rw06johWqOzIXf3RQ5jWdPIzRvt9PhYr.WspEWH6C',
    'System Admin',
    'ACTIVE',
    NOW()
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@techcycle.com'
);
INSERT INTO categories (category_id, parent_id, name, icon_url, is_active, created_at)
VALUES
    (1, NULL, 'Điện thoại', NULL, true, NOW()),
    (2, NULL, 'Laptop / PC', NULL, true, NOW()),
    (3, NULL, 'Máy ảnh', NULL, true, NOW()),
    (4, NULL, 'Âm thanh', NULL, true, NOW()),
    (5, NULL, 'Đồng hồ thông minh', NULL, true, NOW()),
    (6, NULL, 'Màn hình / TV', NULL, true, NOW()),
    (7, NULL, 'Gaming / Console', NULL, true, NOW()),
    (8, NULL, 'Linh kiện', NULL, true, NOW()),
    (9, NULL, 'Danh mục khác', NULL, true, NOW());

INSERT INTO brands (category_id, name, logo_url, is_active) VALUES
                                                                (1, 'Apple', NULL, true),
                                                                (1, 'Samsung', NULL, true),
                                                                (1, 'Xiaomi', NULL, true),
                                                                (1, 'OPPO', NULL, true),
                                                                (1, 'Vivo', NULL, true),
                                                                (1, 'Realme', NULL, true),
                                                                (1, 'Nokia', NULL, true);
INSERT INTO brands (category_id, name, logo_url, is_active) VALUES
                                                                (2, 'Apple', NULL, true),
                                                                (2, 'Dell', NULL, true),
                                                                (2, 'HP', NULL, true),
                                                                (2, 'Lenovo', NULL, true),
                                                                (2, 'Asus', NULL, true),
                                                                (2, 'Acer', NULL, true),
                                                                (2, 'MSI', NULL, true),
                                                                (2, 'Microsoft Surface', NULL, true);
INSERT INTO brands (category_id, name, logo_url, is_active) VALUES
                                                                (3, 'Canon', NULL, true),
                                                                (3, 'Nikon', NULL, true),
                                                                (3, 'Sony', NULL, true),
                                                                (3, 'Fujifilm', NULL, true),
                                                                (3, 'Panasonic', NULL, true),
                                                                (3, 'GoPro', NULL, true);
INSERT INTO brands (category_id, name, logo_url, is_active) VALUES
                                                                (4, 'Sony', NULL, true),
                                                                (4, 'JBL', NULL, true),
                                                                (4, 'Bose', NULL, true),
                                                                (4, 'Marshall', NULL, true),
                                                                (4, 'Anker', NULL, true),
                                                                (4, 'Sennheiser', NULL, true);
INSERT INTO brands (category_id, name, logo_url, is_active) VALUES
                                                                (5, 'Apple', NULL, true),
                                                                (5, 'Samsung', NULL, true),
                                                                (5, 'Garmin', NULL, true),
                                                                (5, 'Huawei', NULL, true),
                                                                (5, 'Amazfit', NULL, true),
                                                                (5, 'Fitbit', NULL, true);
INSERT INTO brands (category_id, name, logo_url, is_active) VALUES
                                                                (6, 'Samsung', NULL, true),
                                                                (6, 'LG', NULL, true),
                                                                (6, 'Sony', NULL, true),
                                                                (6, 'TCL', NULL, true),
                                                                (6, 'Asus', NULL, true),
                                                                (6, 'Dell', NULL, true);
INSERT INTO brands (category_id, name, logo_url, is_active) VALUES
                                                                (7, 'Sony PlayStation', NULL, true),
                                                                (7, 'Microsoft Xbox', NULL, true),
                                                                (7, 'Nintendo', NULL, true),
                                                                (7, 'Razer', NULL, true),
                                                                (7, 'Logitech G', NULL, true),
                                                                (7, 'Asus ROG', NULL, true);

INSERT INTO brands (category_id, name, logo_url, is_active) VALUES
                                                                (8, 'Intel', NULL, true),
                                                                (8, 'AMD', NULL, true),
                                                                (8, 'NVIDIA', NULL, true),
                                                                (8, 'Corsair', NULL, true),
                                                                (8, 'Kingston', NULL, true),
                                                                (8, 'Samsung SSD', NULL, true),
                                                                (8, 'Seagate', NULL, true),
                                                                (8, 'Western Digital', NULL, true);
INSERT INTO brands (category_id, name, logo_url, is_active) VALUES
                                                                (9, 'Generic', NULL, true),
                                                                (9, 'Other', NULL, true);


