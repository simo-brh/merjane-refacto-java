package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Order;

public interface OrderService {

    Order getOrderById(Long id);
    void processOrderById(Long orderId);

}
