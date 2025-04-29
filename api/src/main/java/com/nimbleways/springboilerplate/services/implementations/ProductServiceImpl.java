package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.services.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

import java.time.LocalDate;

/**
 * Product Service dedicate for business operations (CRUD)
 */
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final NotificationService notificationService;

    public ProductServiceImpl(ProductRepository productRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }


    @Override
    public void saveProduct(Product product) {

        productRepository.save(product);

        System.out.printf("save product with id : %d.%n", product.getId());

    }

    @Override
    public void resetAvailability(Product product) {

        product.setAvailable(0);
        productRepository.save(product);

        System.out.printf("reset availability for product : %d.%n", product.getId());

    }

    @Override
    public void decreaseAvailability(Product product) {

        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);

        System.out.printf("decrease availability for product : %d.%n", product.getId());
    }

    @Override
    public void processProduct(Product product){

        switch (product.getType()) {

            case NORMAL: {
                processNormalProduct(product);
                break;
            }

            case SEASONAL: {
                processSeasonalProduct(product);
                break;
            }

            case EXPIRABLE: {
                processExpirableProduct(product);
                break;
            }
        }
    }

    @Override
    public void notifyDelay(Product product) {
        notificationService.sendDelayNotification(product.getLeadTime(), product.getName());
    }

    @Override
    public void processNormalProduct(Product product){

        log.info("Process Normal Product with Id {}", product.getId());

        if (product.getAvailable() > 0) {
            this.decreaseAvailability(product);
        } else {
            notifyDelay(product);
        }

    }

    @Override
    public void processSeasonalProduct(Product product) {

        log.info("Process Seasonal Product with Id {}", product.getId());

        if (isWithinSeason(product)) {
            if (product.getAvailable() > 0) {
                this.decreaseAvailability(product);
            } else {
                notifyDelay(product);
            }
        } else {
            notificationService.sendOutOfStockNotification(product.getName());
            this.resetAvailability(product);
        }
    }

    @Override
    public void processExpirableProduct(Product product) {

        log.info("Process Expirable Product with Id {}", product.getId());

        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {
            this.decreaseAvailability(product);
        } else {
            notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
            this.resetAvailability(product);
        }
    }

    private boolean isWithinSeason(Product product) {
        return LocalDate.now().isAfter(product.getSeasonStartDate()) && LocalDate.now().isBefore(product.getSeasonEndDate());
    }

}