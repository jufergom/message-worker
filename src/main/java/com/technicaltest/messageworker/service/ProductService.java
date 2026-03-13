package com.technicaltest.messageworker.service;

import com.technicaltest.messageworker.dto.ProductDTO;

import reactor.core.publisher.Mono;

public interface ProductService {
    Mono<ProductDTO> getProductById(String id);
}
