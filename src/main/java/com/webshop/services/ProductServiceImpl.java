package com.webshop.services;

import com.webshop.dto.ProductInputDto;
import com.webshop.dto.ProductPreviewDto;
import com.webshop.entities.Product;
import com.webshop.exceptions.MaxImageSizeExceededException;
import com.webshop.exceptions.ProductNotFoundException;
import com.webshop.exceptions.WrongImageTypeException;
import com.webshop.repositories.ProductPreviewDtoRepository;
import com.webshop.repositories.ProductRepository;
import com.webshop.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    @Value("${images.upload-directory}")
    private String uploadDirectory;

    private final ProductPreviewDtoRepository productPreviewDtoRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Mono<Product> createProduct(ProductInputDto productInputDto){

        String uniqueFileName = null;
        FilePart image = productInputDto.getImage();
        if (image != null) {
            validateImage(image);
            uniqueFileName = ImageUtils.generateUniqueImageName(image.filename());
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
    public Mono<Product> getProductById(Integer id) {
        return productRepository.findById(id)
            .switchIfEmpty(Mono.error(new ProductNotFoundException("Товар не найден")));
    }

    @Override
    public Flux<ProductPreviewDto> getProductPreviewDtos(
        String title, Double minPrice, Double maxPrice, String sort, int page, int size) {

        Sort.Direction direction = Sort.Direction.ASC;
        String property = "title";

        if ("title-desc".equalsIgnoreCase(sort)) {
            direction = Sort.Direction.DESC;
        } else if ("price-asc".equalsIgnoreCase(sort)) {
            property = "price";
        } else if ("price-desc".equalsIgnoreCase(sort)) {
            property = "price";
            direction = Sort.Direction.DESC;
        }

        Pageable pageable = PageRequest.of(page, size, direction, property);

        if (title != null && !title.isEmpty()) {
            return productPreviewDtoRepository.findProductPreviewDtosByTitleContaining(title, pageable);
        } else if (minPrice != null && maxPrice != null) {
            return productPreviewDtoRepository.findProductPreviewDtosByPriceBetween(minPrice, maxPrice, pageable);
        } else if (minPrice != null) {
            return productPreviewDtoRepository.findProductPreviewDtosByPriceGreaterThan(minPrice, pageable);
        } else if (maxPrice != null) {
            return productPreviewDtoRepository.findProductPreviewDtosByPriceLessThan(maxPrice, pageable);
        } else {
            return productPreviewDtoRepository.findAllProductPreviewDtos(pageable);
        }
    }

    private void validateImage(FilePart file) {
        if (!ImageUtils.isValidImageExtension(file.filename())) {
            throw new WrongImageTypeException("Недопустимый формат изображения, разрешены: jpeg, jpg, png.");
        }
        if (!ImageUtils.isValidImageSize(file.headers().getContentLength())) {
            throw new MaxImageSizeExceededException("Размер файла не должен превышать 3 МБ.");
        }
    }

    private void saveImage(FilePart file, String relativePath) {
        try {
            String baseDir = System.getProperty("catalina.base");
            if (baseDir == null) baseDir = "build/tmp";

            if (uploadDirectory == null) {
                throw new IllegalStateException("Свойство images.upload-directory не задано в конфигурации.");
            }

            Path fullPath = Paths.get(baseDir, uploadDirectory, relativePath);
            Files.createDirectories(fullPath.getParent());
            file.transferTo(fullPath);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении файла: " + relativePath, e);
        }
    }

}
