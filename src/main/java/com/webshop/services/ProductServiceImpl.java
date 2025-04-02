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
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
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
            .switchIfEmpty(Mono.error(new ProductNotFoundException("Товар не найден")))
            .doOnNext(product -> {
                if (product.getImagePath() != null) {
                    log.info("Found product with ID: {}, imagePath: {}", id, product.getImagePath());
                } else {
                    log.warn("Product with ID: {} has null imagePath", id);
                }
            });
    }

    @Override
    public Mono<Page<ProductPreviewDto>> getPageableProductPreviewDtos(
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

        Mono<Long> countMono;
        Flux<ProductPreviewDto> productsFlux;

        if (title != null && !title.isEmpty()) {
            countMono = productRepository.countByTitleContaining(title);
            productsFlux = productPreviewDtoRepository.findProductPreviewDtosByTitleContaining(title, pageable);
        } else if (minPrice != null && maxPrice != null) {
            countMono = productRepository.countByPriceBetween(minPrice, maxPrice);
            productsFlux = productPreviewDtoRepository.findProductPreviewDtosByPriceBetween(minPrice, maxPrice, pageable);
        } else if (minPrice != null) {
            countMono = productRepository.countByPriceGreaterThan(minPrice);
            productsFlux = productPreviewDtoRepository.findProductPreviewDtosByPriceGreaterThan(minPrice, pageable);
        } else if (maxPrice != null) {
            countMono = productRepository.countByPriceLessThan(maxPrice);
            productsFlux = productPreviewDtoRepository.findProductPreviewDtosByPriceLessThan(maxPrice, pageable);
        } else {
            countMono = productRepository.count();
            productsFlux = productPreviewDtoRepository.findAllProductPreviewDtos(pageable);
        }

        return productsFlux.collectList()
            .zipWith(countMono)
            .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Override
    public Mono<Product> createProduct(ProductInputDto productInputDto) {
        return Mono.justOrEmpty(productInputDto.image())
            .flatMap(image -> {
                if (!ImageUtils.isValidImageExtension(image.filename()))
                    return Mono.error(
                        new WrongImageTypeException("Недопустимый формат изображения, разрешены: jpeg, jpg, png."));
                if (!ImageUtils.isValidImageSize(image.headers().getContentLength()))
                    return Mono.error(new MaxImageSizeExceededException("Размер файла не должен превышать 3 МБ."));
                String uniqueFileName = ImageUtils.generateUniqueImageName(image.filename());
                return saveImage(image, uniqueFileName)
                    .thenReturn(uniqueFileName);
            })
            .onErrorResume(e -> {
                if (e instanceof WrongImageTypeException || e instanceof MaxImageSizeExceededException) {
                    return Mono.just("noimage.png");
                }
                return Mono.error(e);
            })
            .defaultIfEmpty("noimage.png")
            .flatMap(imagePath -> {
                Product product = Product.builder()
                    .title(productInputDto.title())
                    .description(productInputDto.description())
                    .price(productInputDto.price())
                    .imagePath(imagePath)
                    .build();
                return productRepository.save(product);
            });
    }

    @PostConstruct
    public void init() {
        Path uploadDir = Paths.get(uploadDirectory).toAbsolutePath();
        try {
            Files.createDirectories(uploadDir);
            log.info("Upload directory created at: {}", uploadDir);

            Path defaultImagePath = uploadDir.resolve("noimage.png");
            if (!Files.exists(defaultImagePath)) {
                try (InputStream is = getClass().getResourceAsStream("/images/noimage.png")) {
                    if (is != null) Files.copy(is, defaultImagePath);
                }
            }
        } catch (IOException e) {
            log.error("Failed to initialize upload directory", e);
        }
    }

    private Mono<Void> saveImage(FilePart file, String filename) {
        return Mono.fromCallable(() -> {
                String uploadDir = System.getenv("UPLOAD_DIR") != null
                    ? System.getenv("UPLOAD_DIR")
                    : uploadDirectory;
                return Paths.get(uploadDir, filename).toAbsolutePath();
            })
            .flatMap(filePath -> {
                return Mono.fromCallable(() -> {
                        if (!Files.exists(filePath.getParent())) {
                            Files.createDirectories(filePath.getParent());
                        }
                        return filePath;
                    })
                    .subscribeOn(Schedulers.boundedElastic());
            })
            .flatMap(file::transferTo);
    }
}