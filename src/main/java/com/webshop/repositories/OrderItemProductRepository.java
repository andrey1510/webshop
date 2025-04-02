package com.webshop.repositories;

import com.webshop.entities.OrderItem;
import com.webshop.entities.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@RequiredArgsConstructor
public class OrderItemProductRepository {

    private final DatabaseClient databaseClient;

    public Flux<OrderItem> findByCustomerOrderIdWithProduct(Integer orderId) {
        String sql = """
            SELECT 
                i.id, i.customer_order_id, i.product_id, i.quantity,
                p.id as p_id, p.title as p_title, p.price as p_price,
                p.description as p_description, p.image_path as p_image_path
            FROM order_items i
            LEFT JOIN products p ON i.product_id = p.id
            WHERE i.customer_order_id = :orderId
            """;

        return databaseClient.sql(sql)
            .bind("orderId", orderId)
            .map((row, metadata) -> {
                OrderItem item = new OrderItem();
                item.setId(row.get("id", Integer.class));
                item.setCustomerOrderId(row.get("customer_order_id", Integer.class));
                item.setProductId(row.get("product_id", Integer.class));
                item.setQuantity(row.get("quantity", Integer.class));

                if (row.get("product_id", Integer.class) != null) {
                    Product product = new Product();
                    product.setId(row.get("p_id", Integer.class));
                    product.setTitle(row.get("p_title", String.class));

                    Number price = row.get("p_price", Number.class);
                    product.setPrice(price != null ? price.doubleValue() : 0.0);

                    product.setDescription(row.get("p_description", String.class));
                    product.setImagePath(row.get("p_image_path", String.class));
                    item.setProduct(product);
                }

                return item;
            })
            .all();
    }

}