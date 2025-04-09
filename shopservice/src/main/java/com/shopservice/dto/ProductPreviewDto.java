package com.shopservice.dto;

import java.io.Serial;
import java.io.Serializable;

public record ProductPreviewDto(
    Integer id,
    String title,
    Double price,
    String imagePath
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
