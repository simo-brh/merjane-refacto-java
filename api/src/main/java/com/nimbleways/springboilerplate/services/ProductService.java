package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Product;

public interface ProductService {

    void saveProduct(Product product);

    void resetAvailability(Product product);

    void decreaseAvailability(Product product);

    void notifyDelay(Product product);

    void processProduct(Product product);

    void processNormalProduct(Product product);

    void processSeasonalProduct(Product product);

    void processExpirableProduct(Product product);
}
