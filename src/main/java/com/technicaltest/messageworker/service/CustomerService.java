package com.technicaltest.messageworker.service;

import com.technicaltest.messageworker.dto.CustomerDTO;

import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<CustomerDTO> getCustomerById(String id);
}
