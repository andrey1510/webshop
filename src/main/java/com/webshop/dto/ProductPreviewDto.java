package com.webshop.dto;

public record ProductPreviewDto(
    Integer id,
    String title,
    Double price,
    String imagePath
) {}
