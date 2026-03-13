package com.technicaltest.messageworker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.technicaltest.messageworker.dto.CustomerDTO;
import com.technicaltest.messageworker.exception.EntityNotFoundException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTests {
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

    private CustomerServiceImpl customerServiceImpl;

    private final String baseUrl = "http://fake-api.com";

    @BeforeEach
    void setUp() {
        Mockito.when(webClientBuilder.baseUrl(baseUrl)).thenReturn(webClientBuilder);
        Mockito.when(webClientBuilder.build()).thenReturn(webClient);
        customerServiceImpl = new CustomerServiceImpl(webClientBuilder, baseUrl);
    }

    @Test
    void getCustomerByIdReturnsCustomer() {
        String customerId = "123";
        CustomerDTO fakeCustomer = new CustomerDTO();
        fakeCustomer.setId(customerId);
        fakeCustomer.setName("Test Customer");
        fakeCustomer.setActive(true);

        Mockito.doReturn(requestHeadersUriSpec).when(webClient).get();
        Mockito.doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri("/api/customers/{id}", customerId);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(CustomerDTO.class)).thenReturn(Mono.just(fakeCustomer));

        StepVerifier.create(customerServiceImpl.getCustomerById(customerId))
            .expectNextMatches(p -> p.getId().equals(customerId) && p.getName().equals("Test Customer"))
            .verifyComplete();
    }

    @Test
    void getProductById4xxErrorThrowsEntityNotFoundException() {
        String customerId = "999";

        Mockito.doReturn(requestHeadersUriSpec).when(webClient).get();
        Mockito.doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri("/api/customers/{id}", customerId);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenAnswer(invocation -> {
            return responseSpec;
        });
        Mockito.when(responseSpec.bodyToMono(CustomerDTO.class))
               .thenReturn(Mono.error(new EntityNotFoundException("Customer with id " + customerId + " not found")));

        StepVerifier.create(customerServiceImpl.getCustomerById(customerId))
            .expectErrorMatches(ex -> ex instanceof EntityNotFoundException &&
                                      ex.getMessage().contains(customerId))
            .verify();
    }
}
