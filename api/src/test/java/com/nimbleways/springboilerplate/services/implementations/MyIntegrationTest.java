package com.nimbleways.springboilerplate.services.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Collectors;

import static com.nimbleways.springboilerplate.entities.ProductType.*;
import static com.nimbleways.springboilerplate.utils.TestUtils.readJsonFile;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MyIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @Test
    @org.junit.jupiter.api.Order(1)
    void contextLoads() throws JsonProcessingException {

        String jsonContent  = readJsonFile("/data/order.json", this.getClass());

        Order orderFromJson = objectMapper.readValue(jsonContent, Order.class);

        // sort list by id to save one by on usng id 1, 2, 3...
        List<Product> productsFormJson = orderFromJson.getItems().stream()
                .sorted(Comparator.comparingLong(Product::getId))
                .collect(Collectors.toList());

        List<Product> savedProducts = productRepository.saveAll(productsFormJson);

        Assertions.assertEquals(4, savedProducts.size());
        
        Order orderToSave = new Order();
        orderToSave.setId(orderFromJson.getId());
        orderToSave.setItems(new HashSet<>(savedProducts));

        Order savedOrder = orderRepository.save(orderFromJson);

        Assertions.assertNotNull(savedOrder.getId());
        Assertions.assertEquals(4, savedOrder.getItems().size());
    }


    @Test
    @org.junit.jupiter.api.Order(2)
    void processOrderById() {


        orderService.processOrderById(1L);

        // normal product
        Optional<Product> product = productRepository.findById(1L);
        Assertions.assertTrue(product.isPresent());
        Assertions.assertEquals(19, product.get().getAvailable());
        Assertions.assertEquals(NORMAL, product.get().getType());

        // Seasonal product -> must set availability to 0
        Optional<Product> product1 = productRepository.findById(2L);

        Assertions.assertTrue(product1.isPresent());
        Assertions.assertEquals(0, product1.get().getAvailable());
        Assertions.assertEquals(SEASONAL, product1.get().getType());

        // EXPIRABLE product but can process as normal ( 15/05 > now)
        Optional<Product> product2 = productRepository.findById(3L);

        Assertions.assertTrue(product2.isPresent());
        Assertions.assertEquals(14, product2.get().getAvailable());
        Assertions.assertEquals(EXPIRABLE, product2.get().getType());


        // EXPIRABLE product ( expiry data : 27/04 < now ) should set availability to 0
        Optional<Product> product4 = productRepository.findById(4L);

        Assertions.assertTrue(product4.isPresent());
        Assertions.assertEquals(0, product4.get().getAvailable());
        Assertions.assertEquals(EXPIRABLE, product4.get().getType());

    }
}
