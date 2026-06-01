package com.demo.kafka;

import com.demo.dto.ProductEvent;
import com.demo.enums.EventType;
import com.demo.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Component
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ProductEventConsumer {

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topic.product.name:product-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "consumerKafkaListenerContainerFactory"
    )
    public void handleProductEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String value = record.value();
        try {
            ProductEvent productEvent = objectMapper.readValue(value, ProductEvent.class);

            log.info("Received product event: {} for product: {} by user: {}", 
                    productEvent.getEventType(), 
                    productEvent.getProductName(), 
                    productEvent.getUsername());

            // Process the product event
            processProductEvent(productEvent);

            acknowledgment.acknowledge();

        } catch (JsonProcessingException e) {
            log.error("Error deserializing product event: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing product event: {}", e.getMessage(), e);
        }
    }

    private void processProductEvent(ProductEvent productEvent) {
        try {
            EventType type = productEvent.getEventType();
            if (type == null) {
                log.warn("Unknown product event type: null");
                return;
            }
            switch (type) {
                case CREATED:
                    handleProductCreated(productEvent);
                    break;
                case UPDATED:
                    handleProductUpdated(productEvent);
                    break;
                case DELETED:
                    handleProductDeleted(productEvent);
                    break;
                default:
                    log.warn("Unknown product event type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error processing product event type {}: {}", 
                     productEvent.getEventType(), e.getMessage(), e);
        }
    }

    private void handleProductCreated(ProductEvent productEvent) {
        log.info("User {} created a new product: {} (ID: {})", 
                productEvent.getUsername(), 
                productEvent.getProductName(), 
                productEvent.getProductId());

        // Update user statistics or send notifications
        userService.updateUserProductStats(productEvent.getCreatorId(), EventType.CREATED.name());
    }

    private void handleProductUpdated(ProductEvent productEvent) {
        log.info("User {} updated product: {} (ID: {})", 
                productEvent.getUsername(), 
                productEvent.getProductName(), 
                productEvent.getProductId());

        // Update user statistics
        userService.updateUserProductStats(productEvent.getCreatorId(), EventType.UPDATED.name());
    }

    private void handleProductDeleted(ProductEvent productEvent) {
        log.info("User {} deleted product: {} (ID: {})", 
                productEvent.getUsername(), 
                productEvent.getProductName(), 
                productEvent.getProductId());

        // Update user statistics
        userService.updateUserProductStats(productEvent.getCreatorId(), EventType.DELETED.name());
    }
}
