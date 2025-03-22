package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.contollers.OrderController;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.OrderService;
import com.nimbleways.springboilerplate.services.ProductService;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

// Specify the controller class you want to test
// This indicates to spring boot to only load UsersController into the context
// Which allows a better performance and needs to do less mocks
@SpringBootTest
@AutoConfigureMockMvc
public class MyControllerIntegrationTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private NotificationService notificationService;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private ProductRepository productRepository;

        @Test
        public void processOrderShouldReturn() throws Exception {

            // save product
                Set<Product> allProducts = createProducts();
                productRepository.saveAll(allProducts);

            //save order
                Order order = createOrder(allProducts);
                productRepository.saveAll(allProducts);
                orderRepository.save(order);

            //process Order
                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                                .contentType("application/json"))
                                .andExpect(status().isOk());


            // check saved order
                Order resultOrder = orderRepository.findById(order.getId()).get();
                assertEquals(resultOrder.getId(), order.getId());

            // check processed order items

            Product product = productRepository.findFirstByName("USB Cable").orElse(null);
            assertNotNull(product);
            assertEquals(Integer.valueOf(29), product.getAvailable());

            Product product1 = productRepository.findFirstByName("USB Dongle").orElse(null);
            assertNotNull(product1);
            Mockito.verify(notificationService, Mockito.times(1)).sendDelayNotification(product1.getLeadTime(), product1.getName());

            Product product2 = productRepository.findFirstByName("Butter").orElse(null);
            assertNotNull(product2);
            assertEquals(Integer.valueOf(29), product2.getAvailable());

            Product product3 = productRepository.findFirstByName("Milk").orElse(null);
            assertNotNull(product3);
            Mockito.verify(notificationService, Mockito.times(1)).sendExpirationNotification(product3.getName(), product3.getExpiryDate());

            Product product4 = productRepository.findFirstByName("Watermelon").orElse(null);
            assertNotNull(product4);
            assertEquals(Integer.valueOf(29), product4.getAvailable());

            Product product5 = productRepository.findFirstByName("Grapes").orElse(null);
            assertNotNull(product5);
            Mockito.verify(notificationService, Mockito.times(1)).sendOutOfStockNotification(product5.getName());

        }
        private static Order createOrder(Set<Product> products) {
                Order order = new Order();
                order.setItems(products);
                return order;
        }

        private static Set<Product> createProducts() {
                Set<Product> products = new HashSet<>();
                products.add(new Product(null, 15, 30, ProductType.of("NORMAL"), "USB Cable", null, null, null));
                products.add(new Product(null, 10, 0, ProductType.of("NORMAL"), "USB Dongle", null, null, null));
                products.add(new Product(null, 15, 30, ProductType.of("EXPIRABLE"), "Butter", LocalDate.now().plusDays(26), null,
                                null));
                products.add(new Product(null, 90, 6, ProductType.of("EXPIRABLE"), "Milk", LocalDate.now().minusDays(2), null, null));
                products.add(new Product(null, 15, 30, ProductType.of("SEASONAL"), "Watermelon", null, LocalDate.now().minusDays(2),
                                LocalDate.now().plusDays(58)));
                products.add(new Product(null, 15, 30, ProductType.of("SEASONAL"), "Grapes", null, LocalDate.now().plusDays(180),
                                LocalDate.now().plusDays(240)));
                return products;
        }
}
