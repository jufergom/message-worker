package com.technicaltest.messageworker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.technicaltest.messageworker.entity.Order;
import com.technicaltest.messageworker.entity.Product;
import com.technicaltest.messageworker.event.OrderEvent;
import com.technicaltest.messageworker.exception.EntityNotFoundException;
import com.technicaltest.messageworker.exception.InvalidOrderException;
import com.technicaltest.messageworker.repository.OrderRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderServiceImpl implements OrderService {

    private OrderRepository orderRepository;

    private ProductService productService;

    private CustomerService customerService;

    public OrderServiceImpl(OrderRepository orderRepository, ProductService productService,
            CustomerService customerService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.customerService = customerService;
    }

    @Override
    public Mono<Order> process(OrderEvent orderEvent) {
        return customerService.getCustomerById(orderEvent.getCustomerId())
                .onErrorMap(EntityNotFoundException.class,
                        ex -> new InvalidOrderException(ex.getMessage()))
                .flatMap(customer -> {
                    if (!customer.isActive()) {
                        return Mono.error(new InvalidOrderException(
                                "Customer is inactive: " + customer.getId()));
                    }

                    return Flux.fromIterable(orderEvent.getProducts())
                            .flatMap(productId -> productService.getProductById(productId)
                                    .onErrorMap(EntityNotFoundException.class,
                                            ex -> new InvalidOrderException(ex.getMessage())))
                            .collectList()
                            .flatMap(products -> {
                                Order order = new Order();
                                order.setOrderId("order-" + orderEvent.getOrderId());
                                order.setCustomerId("customer-" + customer.getId());

                                List<Product> orderProducts = products.stream()
                                        .map(product -> {
                                            Product orderProduct = new Product();
                                            orderProduct.setProductId("product-" + product.getId());
                                            orderProduct.setName(product.getName());
                                            orderProduct.setPrice(product.getPrice());
                                            return orderProduct;
                                        })
                                        .toList();
                                order.setProducts(orderProducts);

                                return orderRepository.save(order);
                            });
                });
    }
}
