package com.shopservice.dto;

import org.springframework.http.codec.multipart.FilePart;

public record ProductInputDto(
    String title,
    String description,
    Double price,
    FilePart image
) {
    public static ProductInputDto fromFormData(
        String title,
        String description,
        String priceStr,
        FilePart image
    ) {
        return new ProductInputDto(
            title,
            description,
            Double.parseDouble(priceStr),
            image
        );
    }
}
