INSERT INTO categories (category_id, parent_id, name, icon_url, is_active) VALUES
                                                                               (1, NULL, 'Điện thoại', NULL, true),
                                                                               (2, NULL, 'Laptop / PC', NULL, true),
                                                                               (3, NULL, 'Máy ảnh', NULL, true),
                                                                               (4, NULL, 'Âm thanh', NULL, true),
                                                                               (5, NULL, 'Đồng hồ thông minh', NULL, true),
                                                                               (6, NULL, 'Màn hình / TV', NULL, true),
                                                                               (7, NULL, 'Gaming / Console', NULL, true),
                                                                               (8, NULL, 'Linh kiện', NULL, true);

INSERT INTO categories (category_id, parent_id, name, icon_url, is_active) VALUES

-- Điện thoại
(101, 1, 'Điện thoại bàn', NULL, true),
(102, 1, 'Điện thoại cảm ứng', NULL, true),
(103, 1, 'Điện thoại phổ thông', NULL, true),

-- Laptop / PC
(201, 2, 'Laptop Gaming', NULL, true),
(202, 2, 'Laptop Văn phòng', NULL, true),
(203, 2, 'PC Gaming', NULL, true),
(204, 2, 'PC Văn phòng', NULL, true),

-- Máy ảnh
(301, 3, 'Máy ảnh DSLR', NULL, true),
(302, 3, 'Máy ảnh Mirrorless', NULL, true),
(303, 3, 'Máy ảnh du lịch', NULL, true),

-- Âm thanh
(401, 4, 'Tai nghe không dây', NULL, true),
(402, 4, 'Tai nghe có dây', NULL, true),
(403, 4, 'Tai nghe chụp tai', NULL, true),
(404, 4, 'Loa để bàn', NULL, true),
(405, 4, 'Loa Bluetooth', NULL, true),

-- Đồng hồ thông minh
(501, 5, 'Apple Watch', NULL, true),
(502, 5, 'Đồng hồ Android', NULL, true),
(503, 5, 'Vòng đeo tay thông minh', NULL, true),

-- Màn hình / TV
(601, 6, 'Màn hình máy tính', NULL, true),
(602, 6, 'Smart TV', NULL, true),
(603, 6, 'Android TV', NULL, true),

-- Gaming / Console
(701, 7, 'PlayStation', NULL, true),
(702, 7, 'Xbox', NULL, true),
(703, 7, 'Nintendo Switch', NULL, true),
(704, 7, 'Phụ kiện gaming', NULL, true),

-- Linh kiện
(801, 8, 'CPU', NULL, true),
(802, 8, 'RAM', NULL, true),
(803, 8, 'Ổ cứng (SSD/HDD)', NULL, true),
(804, 8, 'Card đồ họa (GPU)', NULL, true);
