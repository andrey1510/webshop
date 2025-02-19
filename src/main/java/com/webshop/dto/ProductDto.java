package com.webshop.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class ProductDto {

    private Integer id;
    private String title;
    private Double price;
    private String imageUrl;

}
