DELETE FROM order_item;
DELETE FROM customer_order;
DELETE FROM product;

INSERT INTO product (id, title, description, price, image_path) VALUES
        (6, 'Ноутбук', 'описание ноутбука', 1000.0, 'laptop.jpg'),
        (7, 'Смартфон', 'описание смартфона', 500.0, 'phone.jpg'),
        (8, 'Планшет', 'описание планшета', 300.0, 'tablet.jpg'),
        (9, 'Персональный компьютер', 'описание компьютера', 200.0, 'pc.jpg');

INSERT INTO customer_order (id, status, completed_order_price, timestamp) VALUES
        (6, 'CART', 0.0, '2023-10-01 10:00:00'),
        (7, 'COMPLETED', 1500.0, '2023-10-02 11:00:00'),
        (8, 'COMPLETED', 700.0, '2023-10-03 12:00:00');

INSERT INTO order_item (id, customer_order_id, product_id, quantity) VALUES
        (6,6, 6, 1),
        (7,6, 7, 2),
        (8,7, 6, 1),
        (9,7, 7, 2),
        (10,8, 8, 1),
        (11, 8, 9, 2);