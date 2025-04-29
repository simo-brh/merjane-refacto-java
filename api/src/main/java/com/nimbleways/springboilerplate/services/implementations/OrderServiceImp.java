package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.exception.OrderNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.OrderService;
import com.nimbleways.springboilerplate.services.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Order Service dedicate for business operations (CRUD)
 */
@Service
@Slf4j
public class OrderServiceImp implements OrderService {

    private final OrderRepository orderRepository;

    private final ProductService productService;

    public OrderServiceImp(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    @Override
    public Order getOrderById(Long id) {

        Optional<Order> optionalOrder = orderRepository.findById(id);

        if(optionalOrder.isEmpty()){

            log.error("Order with ID  {} not found", id);
            throw  new OrderNotFoundException("Order with ID " + id + " not found");

        }

        return optionalOrder.get();

    }

    @Override
    @Transactional
    public void processOrderById(Long orderId) {

        log.info("Process order with id {}", orderId);

        Order order = this.getOrderById(orderId);

        Set<Product> products = order.getItems() != null
                ? order.getItems() : new HashSet<>();

        products.forEach(productService::processProduct);

    }


}
