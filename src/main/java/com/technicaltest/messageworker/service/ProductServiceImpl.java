package com.technicaltest.messageworker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.technicaltest.messageworker.dto.ProductDTO;
import com.technicaltest.messageworker.exception.EntityNotFoundException;

import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService {

    private final WebClient webClient;

    public ProductServiceImpl(WebClient.Builder webClientBuilder, @Value("${products.api.url}") String productsApiUrl) {
        this.webClient = webClientBuilder.baseUrl(productsApiUrl)
            .build();
    }

    @Override
    public Mono<ProductDTO> getProductById(String id) {
        return this.webClient
            .get()
            .uri("/api/products/{id}", id)
            .retrieve()
            .onStatus(
                status -> status.is4xxClientError(),
                response -> Mono.error(new EntityNotFoundException("Product with id " + id + " not found"))
            )
            .bodyToMono(ProductDTO.class);
    }
}
