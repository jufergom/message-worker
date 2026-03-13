package com.technicaltest.messageworker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.technicaltest.messageworker.dto.ProductDTO;
import com.technicaltest.messageworker.exception.EntityNotFoundException;

import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService {

    private final WebClient webClient;

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    public ProductServiceImpl(WebClient.Builder webClientBuilder, @Value("${products.api.url}") String productsApiUrl) {
        this.webClient = webClientBuilder.baseUrl(productsApiUrl)
            .build();
    }

    @Override
    @Cacheable(value = "product_cache", key = "id")
    @Retryable(
        retryFor = { RuntimeException.class },
        noRetryFor = { EntityNotFoundException.class },
        maxAttempts = 5,
        backoff = @Backoff(
            delay = 1000,
            multiplier = 2
        )
    )
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

    @Recover
    public Mono<ProductDTO> recover(RuntimeException ex, String id) {
        logger.error("API failed after retries {}", id);
        throw ex;
    }
}
