package com.webshop.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private boolean isCompleted;

    @OneToMany(mappedBy = "productOrder", cascade = CascadeType.ALL)
    private Set<OrderItem> items;

}
