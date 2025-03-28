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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
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


    @Override
    public Mono<Product> createProduct(ProductInputDto productInputDto){
        Mono<String> imagePathMono = Mono.justOrEmpty(productInputDto.image())
            .flatMap(this::processImage)
            .defaultIfEmpty(""); //

        return imagePathMono.flatMap(imagePath -> {
            Product product = Product.builder()
                .title(productInputDto.title())
                .description(productInputDto.description())
                .price(productInputDto.price())
                .imagePath(imagePath.isEmpty() ? null : imagePath)
                .build();
            return productRepository.save(product);
        });
    }

    private Mono<String> processImage(FilePart image) {
        validateImage(image);
        String uniqueFileName = ImageUtils.generateUniqueImageName(image.filename());
        return saveImage(image, uniqueFileName).then(Mono.just(uniqueFileName));
    }



    private void validateImage(FilePart file) {
        if (!ImageUtils.isValidImageExtension(file.filename())) {
            throw new WrongImageTypeException("Недопустимый формат изображения, разрешены: jpeg, jpg, png.");
        }
        if (!ImageUtils.isValidImageSize(file.headers().getContentLength())) {
            throw new MaxImageSizeExceededException("Размер файла не должен превышать 3 МБ.");
        }
    }

    private Mono<Void> saveImage(FilePart file, String relativePath) {
        Path fullPath = Paths.get(uploadDirectory, relativePath);
        return Mono.fromCallable(() -> {
                Files.createDirectories(fullPath.getParent());
                return fullPath;
            })
            .flatMap(file::transferTo)
            .onErrorMap(IOException.class, e -> new RuntimeException("Ошибка при сохранении файла: " + relativePath, e));
    }

}
