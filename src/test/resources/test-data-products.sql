DELETE FROM order_item;
DELETE FROM customer_order;
DELETE FROM product;

INSERT INTO product (id, title, description, price, image_path) VALUES
    (6, 'Ноутбук', 'описание ноутбука', 1000.0, 'laptop.jpg'),
    (7, 'Смартфон', 'описание смартфона', 500.0, 'phone.jpg'),
    (8, 'Планшет', 'описание планшета', 300.0, 'tablet.jpg');
