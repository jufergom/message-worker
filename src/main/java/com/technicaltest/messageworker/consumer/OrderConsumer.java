package com.technicaltest.messageworker.consumer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.technicaltest.messageworker.event.OrderEvent;
import com.technicaltest.messageworker.exception.InvalidOrderException;
import com.technicaltest.messageworker.service.FailedEventService;
import com.technicaltest.messageworker.service.OrderLockService;
import com.technicaltest.messageworker.service.OrderService;

import reactor.util.retry.Retry;
import tools.jackson.databind.ObjectMapper;

@Service
public class OrderConsumer {

    private OrderService orderService;

    private ObjectMapper objectMapper;

    private FailedEventService failedEventService;

    private OrderLockService orderLockService;

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    public OrderConsumer(OrderService orderService, ObjectMapper objectMapper, FailedEventService failedEventService, OrderLockService orderLockService) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
        this.failedEventService = failedEventService;
        this.orderLockService = orderLockService;
    }

    @KafkaListener(topics = "order-topic", groupId = "order-processor-consumer-group")
    public void processOrder(String event) throws Exception {
        logger.info("Received event {}", event);
        OrderEvent orderEvent = objectMapper.readValue(event, OrderEvent.class);

        RLock lock = orderLockService.getLock(orderEvent.getOrderId());

        boolean acquired = lock.tryLock(5, 30, TimeUnit.SECONDS);

        if (!acquired) {
            logger.warn("Order locked for order {}", orderEvent.getOrderId());
            return;
        }

        try {
            AtomicInteger attempts = new AtomicInteger(0);
            orderService.process(orderEvent)
                .doOnSubscribe(sub -> attempts.incrementAndGet())
                .retryWhen(
                    Retry.backoff(3, Duration.ofSeconds(5))
                        .doBeforeRetry(rs -> {
                            attempts.incrementAndGet();
                            logger.info("Retry #{}", attempts.get());
                        })
                )
                .doOnSuccess(order -> logger.info("Order saved: {}", order.getOrderId()))
                .doOnError(error -> {
                    if (error instanceof InvalidOrderException) {
                        logger.error("Invalid order: {}", error.getMessage());
                    } else {
                        logger.error("Error processing order", error);
                        failedEventService.saveFailedEvent(event, attempts.get());
                    }
                })
                .subscribe();

        } finally {
            lock.unlock();
        }
    }
}
