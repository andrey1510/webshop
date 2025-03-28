package com.webshop.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table("products")
public class Product {

    @Id
    private Integer id;

    private String title;

    private String description;

    private Double price;

    private String imagePath;
}
