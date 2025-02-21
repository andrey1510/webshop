package com.webshop.services;

import com.webshop.dto.ProductInputDto;
import com.webshop.entities.Product;
import com.webshop.exceptions.MaxImageSizeExceededException;
import com.webshop.exceptions.ProductNotFoundException;
import com.webshop.exceptions.WrongImageTypeException;
import com.webshop.repositories.ProductRepository;
import com.webshop.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    @Value("${images.upload-directory}")
    private String uploadDirectory;

    private final ProductRepository productRepository;

    @Override
    public Product createProduct(ProductInputDto productInputDto){

        String uniqueFileName = null;
        MultipartFile image = productInputDto.getImage();
        if (image != null && !image.isEmpty()) {
            validateImage(image);
            uniqueFileName = ImageUtils.generateUniqueImageName(image.getOriginalFilename());
            saveImage(image, uniqueFileName);
        }

        Product product = Product.builder()
            .title(productInputDto.getTitle())
            .description(productInputDto.getDescription())
            .price(productInputDto.getPrice())
            .imagePath(uniqueFileName)
            .build();

        productRepository.save(product);

        return product;
    }

    @Override
    public Product getProductById(Integer id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Продукт не найден"));
    }

    @Override
    public Page<ProductInputDto> getAllProductsPaginatedAsDto(int page, int size) {
        return productRepository.findAllAsDto(PageRequest.of(page, size));
    }

    @Override
    public Page<ProductInputDto> findProductsByTitlePaginatedAsDto(String searchQuery, int page, int size) {
        return productRepository.findProductsByTitleAsDto(searchQuery, PageRequest.of(page, size));
    }


    private void validateImage(MultipartFile file) {
        if (!ImageUtils.isValidImageExtension(file.getOriginalFilename())) {
            throw new WrongImageTypeException("Недопустимый формат изображения, разрешены: jpeg, jpg, png.");
        }
        if (!ImageUtils.isValidImageSize(file.getSize())) {
            throw new MaxImageSizeExceededException("Размер файла не должен превышать 3 МБ.");
        }
    }

    private void saveImage(MultipartFile file, String relativePath) {
        try {
            Path fullPath = Paths.get(System.getProperty("catalina.base") + uploadDirectory, relativePath);
            Files.createDirectories(fullPath.getParent());
            file.transferTo(fullPath);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении файла: " + relativePath, e);
        }
    }

}
