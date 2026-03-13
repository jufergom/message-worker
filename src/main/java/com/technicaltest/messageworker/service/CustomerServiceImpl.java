package com.technicaltest.messageworker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.technicaltest.messageworker.dto.CustomerDTO;
import com.technicaltest.messageworker.exception.EntityNotFoundException;

import reactor.core.publisher.Mono;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final WebClient webClient;

    public CustomerServiceImpl(WebClient.Builder webClientBuilder, @Value("${products.api.url}") String productsApiUrl) {
        this.webClient = webClientBuilder.baseUrl(productsApiUrl)
            .build();
    }

    @Override
    public Mono<CustomerDTO> getCustomerById(String id) {
        return this.webClient
            .get()
            .uri("/api/customers/{id}", id)
            .retrieve()
            .onStatus(
                status -> status.is4xxClientError(),
                response -> Mono.error(new EntityNotFoundException("Customer with id " + id + " not found"))
            )
            .bodyToMono(CustomerDTO.class);
    }

}
