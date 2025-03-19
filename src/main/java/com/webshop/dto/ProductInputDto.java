package com.webshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;


@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProductInputDto {

    private String title;
    private String description;
    private Double price;
    private FilePart image;

}
