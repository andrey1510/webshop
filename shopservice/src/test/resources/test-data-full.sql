DELETE FROM order_items;
DELETE FROM customer_orders;
DELETE FROM products;
DELETE FROM users;

INSERT INTO products (id, title, description, price, image_path) VALUES
        (6, 'Ноутбук', 'описание ноутбука', 1000.0, 'laptop.jpg'),
        (7, 'Смартфон', 'описание смартфона', 500.0, 'phone.jpg'),
        (8, 'Планшет', 'описание планшета', 300.0, 'tablet.jpg'),
        (9, 'Персональный компьютер', 'описание компьютера', 200.0, 'pc.jpg');

INSERT INTO customer_orders (id, user_id, status, completed_order_price, timestamp) VALUES
        (6, 1,'CART', 0.0, '2023-10-01 10:00:00'),
        (7, 1,'COMPLETED', 1500.0, '2023-10-02 11:00:00'),
        (8, 1,'COMPLETED', 700.0, '2023-10-03 12:00:00');

INSERT INTO order_items (id, customer_order_id, product_id, quantity) VALUES
        (6,6, 6, 1),
        (7,6, 7, 2),
        (8,7, 6, 1),
        (9,7, 7, 2),
        (10,8, 8, 1),
        (11, 8, 9, 2);