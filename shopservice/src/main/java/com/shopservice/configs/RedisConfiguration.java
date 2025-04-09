package com.shopservice.configs;

import com.shopservice.dto.ProductPreviewDto;
import com.shopservice.entities.CustomerOrder;
import com.shopservice.entities.Product;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfiguration {

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
        ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());
    }

    @Bean
    public Jackson2JsonRedisSerializer<Product> productJsonRedisSerializer() {
        return new Jackson2JsonRedisSerializer<>(Product.class);
    }

    @Bean
    public Jackson2JsonRedisSerializer<CustomerOrder> customerOrderJsonRedisSerializer() {
        return new Jackson2JsonRedisSerializer<>(CustomerOrder.class);
    }

    @Bean
    public ReactiveRedisTemplate<String, Product> productRedisTemplate(
        ReactiveRedisConnectionFactory factory,
        Jackson2JsonRedisSerializer<Product> productSerializer) {

        RedisSerializationContext<String, Product> context =
            RedisSerializationContext.<String, Product>newSerializationContext()
                .key(StringRedisSerializer.UTF_8)
                .value(productSerializer)
                .hashKey(StringRedisSerializer.UTF_8)
                .hashValue(productSerializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate(
        ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisTemplate<>(
            factory,
            RedisSerializationContext.string()
        );
    }

    @Bean
    public ReactiveRedisTemplate<String, ProductPreviewDto> productPreviewDtoTemplate(
        ReactiveRedisConnectionFactory factory) {

        Jackson2JsonRedisSerializer<ProductPreviewDto> serializer =
            new Jackson2JsonRedisSerializer<>(ProductPreviewDto.class);

        RedisSerializationContext<String, ProductPreviewDto> context =
            RedisSerializationContext.<String, ProductPreviewDto>newSerializationContext()
                .key(StringRedisSerializer.UTF_8)
                .value(serializer)
                .hashKey(StringRedisSerializer.UTF_8)
                .hashValue(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, CustomerOrder> customerOrderRedisTemplate(
        ReactiveRedisConnectionFactory factory,
        Jackson2JsonRedisSerializer<CustomerOrder> customerOrderSerializer) {

        RedisSerializationContext<String, CustomerOrder> context =
            RedisSerializationContext.<String, CustomerOrder>newSerializationContext()
                .key(StringRedisSerializer.UTF_8)
                .value(customerOrderSerializer)
                .hashKey(StringRedisSerializer.UTF_8)
                .hashValue(customerOrderSerializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveJsonRedisTemplate(
        ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext<String, Object> serializationContext =
            RedisSerializationContext.<String, Object>newSerializationContext()
                .key(StringRedisSerializer.UTF_8)
                .value(new Jackson2JsonRedisSerializer<>(Object.class))
                .hashKey(StringRedisSerializer.UTF_8)
                .hashValue(new Jackson2JsonRedisSerializer<>(Object.class))
                .build();
        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }
}
