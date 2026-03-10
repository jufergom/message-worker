package com.technicaltest.messageworker.service;

import com.technicaltest.messageworker.event.OrderEvent;

public interface OrderService {
    void process(OrderEvent orderEvent);
}
