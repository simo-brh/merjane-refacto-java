package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import ch.qos.logback.classic.spi.LoggingEvent;

import com.nimbleways.springboilerplate.utils.StaticLogbackAppender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@UnitTest
public class MyUnitTests {

    @Mock
    private NotificationService notificationService;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks 
    private ProductServiceImpl productService;

    @Test
    public void notifyDelayTest() {
        // GIVEN
        Product product =new Product(159L, 15, 0, ProductType.of("NORMAL"), "RJ45 Cable", null, null, null);

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
        Product product =new Product(159L, 15, 10, ProductType.of("NORMAL"), "RJ45 Cable", null, null, null);

        Mockito.when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.processNormalProduct(product);

        // check logs
        List<LoggingEvent> logs = StaticLogbackAppender.getEvents();
        Assertions.assertTrue(logs.stream().anyMatch(e -> e.getFormattedMessage().contains("Process Normal Product with Id 159")));

        StaticLogbackAppender.clearEvents();

        // THEN
        assertEquals(9, product.getAvailable());
        assertEquals(15, product.getLeadTime());

    }

    @Test
    public void processSeasonalProductTest(){

        // GIVEN
        Product product =new Product(160L, 15, 10, ProductType.of("SEASONAL"), "RJ45 Cable", null, null, null);
        product.setSeasonStartDate(LocalDate.now().minusDays(10));
        product.setSeasonEndDate(LocalDate.now().minusDays(2));

        Mockito.when(productRepository.save(product)).thenReturn(product);

        //WHEN
        productService.processSeasonalProduct(product);

        // check logs
        List<LoggingEvent> logs = StaticLogbackAppender.getEvents();
        Assertions.assertTrue(logs.stream().anyMatch(e -> e.getFormattedMessage().contains("Process Seasonal Product with Id 160")));
        StaticLogbackAppender.clearEvents();

        //THEN
        assertEquals(0, product.getAvailable());
        Mockito.verify(notificationService, Mockito.times(1)).sendOutOfStockNotification(product.getName());

    }

    @Test
    public void processExpirableProduct(){
        // GIVEN
        Product product =new Product(161L, 15, 10, ProductType.of("EXPIRABLE"), "RJ45 Cable", null, null, null);
        product.setExpiryDate(LocalDate.now().minusDays(2));

        Mockito.when(productRepository.save(product)).thenReturn(product);

        //WHEN
        productService.processExpirableProduct(product);

        // check logs
        List<LoggingEvent> logs = StaticLogbackAppender.getEvents();
        Assertions.assertTrue(logs.stream().anyMatch(e -> e.getFormattedMessage().contains("Process Expirable Product with Id 161")));
        StaticLogbackAppender.clearEvents();

        //THEN
        assertEquals(0, product.getAvailable());
        Mockito.verify(notificationService, Mockito.times(1)).sendExpirationNotification(product.getName(), product.getExpiryDate());


    }
}