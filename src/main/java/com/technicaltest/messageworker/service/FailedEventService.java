package com.technicaltest.messageworker.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class FailedEventService {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String FAILED_EVENTS_KEY = "failed_orders";

    public FailedEventService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveFailedEvent(String event, int attempts) {
        Map<String, Object> data = new HashMap<>();
        data.put("event", event);
        data.put("attempts", attempts);
        data.put("timestamp", Instant.now().toString());

        redisTemplate.opsForList().rightPush(FAILED_EVENTS_KEY, data);
    }
}
