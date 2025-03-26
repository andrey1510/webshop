package com.webshop.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.resources;

@Configuration
public class WebConfiguration implements WebFluxConfigurer {

    @Value("${images.upload-directory}")
    private String uploadDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String baseDir = System.getProperty("catalina.base") != null ?
            System.getProperty("catalina.base") : "";
        String uploadPath = "file:" + baseDir + uploadDirectory;

        registry.addResourceHandler(uploadDirectory + "**").addResourceLocations(uploadPath);
    }


    @Bean
    public RouterFunction<ServerResponse> staticResourceRouter() {
        return resources("/**", new ClassPathResource("static/"));
    }
}
