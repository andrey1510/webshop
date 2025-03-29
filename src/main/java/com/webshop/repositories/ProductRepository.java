package com.webshop.repositories;

import com.webshop.entities.Product;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;


@Repository
public interface ProductRepository extends R2dbcRepository<Product, Integer> {

    Flux<Product> findAllById(@NonNull Publisher<Integer> ids);
}
