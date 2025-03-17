package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@UnitTest
public class MyUnitTests {

    @Mock
    private NotificationService notificationService;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks 
    private ProductService productService;

    @Test
    public void notifyDelayTest() {
        // GIVEN
        Product product =new Product(null, 15, 0, "NORMAL", "RJ45 Cable", null, null, null);

        Mockito.when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.notifyDelay(product);

        // THEN
        assertEquals(0, product.getAvailable());
        assertEquals(15, product.getLeadTime());
        Mockito.verify(notificationService, Mockito.times(1)).sendDelayNotification(product.getLeadTime(), product.getName());
    }

    @Test
    public void processNormalProductTest() {
        // GIVEN
        Product product =new Product(null, 15, 10, "NORMAL", "RJ45 Cable", null, null, null);

        Mockito.when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.processNormalProduct(product);

        // THEN
        assertEquals(9, product.getAvailable());
        assertEquals(15, product.getLeadTime());

    }

    @Test
    public void processSeasonalProductTest(){

        // GIVEN
        Product product =new Product(null, 15, 10, "SEASONAL", "RJ45 Cable", null, null, null);
        product.setSeasonStartDate(LocalDate.of(2018, 1,1));
        product.setSeasonEndDate(LocalDate.of(2020, 1,1));

        Mockito.when(productRepository.save(product)).thenReturn(product);

        //WHEN
        productService.processSeasonalProduct(product);

        //THEN
        assertEquals(0, product.getAvailable());
        Mockito.verify(notificationService, Mockito.times(1)).sendOutOfStockNotification(product.getName());

    }

    @Test
    public void processExpirableProduct(){
        // GIVEN
        Product product =new Product(null, 15, 10, "EXPIRABLE", "RJ45 Cable", null, null, null);
        product.setExpiryDate(LocalDate.of(2020, 1, 1));

        Mockito.when(productRepository.save(product)).thenReturn(product);

        //WHEN
        productService.processExpirableProduct(product);

        //THEN
        assertEquals(0, product.getAvailable());
        Mockito.verify(notificationService, Mockito.times(1)).sendExpirationNotification(product.getName(), product.getExpiryDate());


    }
}