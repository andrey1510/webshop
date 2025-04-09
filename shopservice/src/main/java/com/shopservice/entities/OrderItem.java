package com.shopservice.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serial;
import java.io.Serializable;

@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"customerOrder", "product"})
@Getter
@Setter
@Builder
@Table("order_items")
public class OrderItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private Integer customerOrderId;

    private Integer productId;

    private Integer quantity;

    @Transient
    private CustomerOrder customerOrder;

    @Transient
    private Product product;
}
