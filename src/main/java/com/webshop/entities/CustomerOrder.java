package com.webshop.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"items"})
@Getter
@Setter
@Builder
public class CustomerOrder {

    @Id
    private Integer id;

    private OrderStatus status;

    private LocalDateTime timestamp;

    private Double completedOrderPrice;

    @Transient
    private List<OrderItem> items;
}
