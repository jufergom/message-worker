package com.technicaltest.messageworker.service;

import com.technicaltest.messageworker.entity.Order;
import com.technicaltest.messageworker.event.OrderEvent;

import reactor.core.publisher.Mono;

public interface OrderService {
    Mono<Order> process(OrderEvent orderEvent);
}
