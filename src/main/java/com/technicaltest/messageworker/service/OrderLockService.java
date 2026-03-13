package com.technicaltest.messageworker.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
public class OrderLockService {
    private final RedissonClient redissonClient;

    public OrderLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public RLock getLock(String orderId) {
        return redissonClient.getLock("order-lock:" + orderId);
    }
}
