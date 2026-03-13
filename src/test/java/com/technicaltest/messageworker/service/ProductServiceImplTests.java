package com.technicaltest.messageworker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.technicaltest.messageworker.dto.ProductDTO;
import com.technicaltest.messageworker.exception.EntityNotFoundException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTests {
    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ProductServiceImpl productService;

    private final String baseUrl = "http://fake-api.com";

    @BeforeEach
    void setUp() {
        Mockito.when(webClientBuilder.baseUrl(baseUrl)).thenReturn(webClientBuilder);
        Mockito.when(webClientBuilder.build()).thenReturn(webClient);
        productService = new ProductServiceImpl(webClientBuilder, baseUrl);
    }

    @Test
    void getProductByIdReturnsProduct() {
        String productId = "123";
        ProductDTO fakeProduct = new ProductDTO();
        fakeProduct.setId(productId);
        fakeProduct.setName("Test Product");
        fakeProduct.setDescription("Test description");
        fakeProduct.setCategory("Test category");
        fakeProduct.setPrice(9.99);

        Mockito.doReturn(requestHeadersUriSpec).when(webClient).get();
        Mockito.doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri("/api/products/{id}", productId);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(ProductDTO.class)).thenReturn(Mono.just(fakeProduct));

        StepVerifier.create(productService.getProductById(productId))
            .expectNextMatches(p -> p.getId().equals(productId) && p.getName().equals("Test Product"))
            .verifyComplete();
    }

    @Test
    void getProductById4xxErrorThrowsEntityNotFoundException() {
        String productId = "999";

        Mockito.doReturn(requestHeadersUriSpec).when(webClient).get();
        Mockito.doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri("/api/products/{id}", productId);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenAnswer(invocation -> {
            return responseSpec;
        });
        Mockito.when(responseSpec.bodyToMono(ProductDTO.class))
               .thenReturn(Mono.error(new EntityNotFoundException("Product with id " + productId + " not found")));

        StepVerifier.create(productService.getProductById(productId))
            .expectErrorMatches(ex -> ex instanceof EntityNotFoundException &&
                                      ex.getMessage().contains(productId))
            .verify();
    }
}
