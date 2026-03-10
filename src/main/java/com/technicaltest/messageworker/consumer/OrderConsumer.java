package com.technicaltest.messageworker.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.technicaltest.messageworker.event.OrderEvent;

import tools.jackson.databind.ObjectMapper;

@Service
public class OrderConsumer {

    private ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    public OrderConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-topic", groupId = "order-processor-consumer-group")
    public void processOrder(String event) {
        logger.info("Received event {}", event);
        OrderEvent orderEvent = objectMapper.readValue(event, OrderEvent.class);
    }
}
