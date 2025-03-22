package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.exception.OrderNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.OrderService;
import com.nimbleways.springboilerplate.services.ProductService;
import org.springframework.stereotype.Service;

/**
 * Order Service dedicate for business operations (CRUD)
 */
@Service
public class OrderServiceImp implements OrderService {

    private final OrderRepository orderRepository;

    private final ProductService productService;

    public OrderServiceImp(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    @Override
    public Order getOrderById(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + id + " not found"));

    }

    @Override
    public void processOrderById(Long orderId) {

        Order order = this.getOrderById(orderId);

        if(order.getItems() != null){

            for ( Product product : order.getItems()){
                productService.processProduct(product);
            }

        }

    }


}
