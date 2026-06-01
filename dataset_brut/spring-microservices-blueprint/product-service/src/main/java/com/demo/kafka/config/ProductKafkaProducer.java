package com.demo.kafka.config;

import com.demo.dto.ProductEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Component
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ProductKafkaProducer {

    @Autowired
    private KafkaTemplate<String, ProductEvent> kafkaTemplate;

    @Value("${spring.kafka.topic.product.name:product-events}")
    private String productTopic;

    @Value("${spring.kafka.replication.factor:1}")
    private int replicationFactor;

    @Value("${spring.kafka.partition.number:1}")
    private int partitionNumber;

    public void sendProductEvent(ProductEvent productEvent) {
        log.info("Sending product event: {}", productEvent);
        kafkaTemplate.send(productTopic, productEvent.getEventType().name(), productEvent);
    }

    @Bean
    @Order(-1)
    public NewTopic createProductTopic() {
        return new NewTopic(productTopic, partitionNumber, (short) replicationFactor);
    }
}
