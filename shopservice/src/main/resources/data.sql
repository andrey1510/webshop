INSERT INTO users (id, username, password, enabled, roles) VALUES
    (101,'sample-user-1', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrYV6ZPb.8Z2XlWk7LfXr7jDd.4JdXG', true, 'USER'),
    (102,'sample-user-2', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrYV6ZPb.8Z2XlWk7LfXr7jDd.4JdXG', true, 'USER');

INSERT INTO products (id, title, description, price, image_path) VALUES
    (101, 'Sample product 1', 'Sample product 1 description', 100.0, 'sample.jpg'),
    (102, 'Sample product 2', 'Sample product 2 description', 300.0, 'sample.jpg');
