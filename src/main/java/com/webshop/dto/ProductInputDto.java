package com.webshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class ProductInputDto {

    private String title;
    private String description;
    private Double price;
    private MultipartFile image;

}
