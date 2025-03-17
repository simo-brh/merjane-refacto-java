package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

import static com.nimbleways.springboilerplate.utils.Constants.*;

@Service
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    NotificationService notificationService;

    public void processOrder(Product product){

        switch (product.getType()) {

            case TYPE_NORMAL: {
                processNormalProduct(product);
                break;
            }

            case TYPE_SEASONAL: {
                processSeasonalProduct(product);
                break;
            }

            case TYPE_EXPIRABLE: {
                processExpirableProduct(product);
                break;
            }
        }
    }

    public void notifyDelay(Product product) {
        notificationService.sendDelayNotification(product.getLeadTime(), product.getName());
    }
    public void processNormalProduct(Product product){

        if (product.getAvailable() > 0) {
            updateAvailability(product);
        } else {
            notifyDelay(product);
        }

    }

    public void processSeasonalProduct(Product product) {
        if (isWithinSeason(product)) {
            if (product.getAvailable() > 0) {
                updateAvailability(product);
            } else {
                notifyDelay(product);
            }
        } else {
            handleSeasonalOutOfStock(product);
        }
    }

    public void processExpirableProduct(Product product) {
        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {
            updateAvailability(product);
        } else {
            handleExpiredProduct(product);
        }
    }

    private void handleExpiredProduct(Product product) {
        notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
        product.setAvailable(0);
        productRepository.save(product);
    }

    private void handleSeasonalOutOfStock(Product product) {
        notificationService.sendOutOfStockNotification(product.getName());
        product.setAvailable(0);
        productRepository.save(product);
    }

    private boolean isWithinSeason(Product product) {
        return LocalDate.now().isAfter(product.getSeasonStartDate()) && LocalDate.now().isBefore(product.getSeasonEndDate());
    }
    private void updateAvailability(Product product){
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }
}