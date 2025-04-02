package com.webshop.repositories;

import com.webshop.dto.ProductPreviewDto;
import com.webshop.entities.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@RequiredArgsConstructor
public class ProductPreviewDtoRepository {

    private final R2dbcEntityTemplate template;

    public Flux<ProductPreviewDto> findProductPreviewDtosByTitleContaining(String title, Pageable pageable) {
        return template.select(Product.class)
            .matching(Query.query(Criteria.where("title").like("%" + title + "%"))
                .with(pageable))
            .all()
            .map(this::toProductPreviewDto);
    }

    public Flux<ProductPreviewDto> findProductPreviewDtosByPriceGreaterThan(Double price, Pageable pageable) {
        return template.select(Product.class)
            .matching(Query.query(Criteria.where("price").greaterThan(price))
                .with(pageable))
            .all()
            .map(this::toProductPreviewDto);
    }

    public Flux<ProductPreviewDto> findProductPreviewDtosByPriceLessThan(Double price, Pageable pageable) {
        return template.select(Product.class)
            .matching(Query.query(Criteria.where("price").lessThan(price))
                .with(pageable))
            .all()
            .map(this::toProductPreviewDto);
    }

    public Flux<ProductPreviewDto> findProductPreviewDtosByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable) {
        return template.select(Product.class)
            .matching(Query.query(Criteria.where("price").between(minPrice, maxPrice))
                .with(pageable))
            .all()
            .map(this::toProductPreviewDto);
    }

    public Flux<ProductPreviewDto> findAllProductPreviewDtos(Pageable pageable) {
        return template.select(Product.class)
            .matching(Query.empty().with(pageable))
            .all()
            .map(this::toProductPreviewDto);
    }

    private ProductPreviewDto toProductPreviewDto(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductPreviewDto(
            product.getId(),
            product.getTitle(),
            product.getPrice(),
            product.getImagePath()
        );
    }

}
