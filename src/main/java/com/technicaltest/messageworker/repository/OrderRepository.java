package com.technicaltest.messageworker.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.technicaltest.messageworker.entity.Order;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String>{

}
