package com.demo.kafka;

import com.demo.dto.User;
import com.demo.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class KafkaUserConsumer {

    @Autowired
    private UserService userService;

    @Value("${spring.kafka.topic.name}")
    private String topic;

    @Value("${spring.kafka.replication.factor:1}")
    private int replicationFactor;

    @Value("${spring.kafka.partition.number:1}")
    private int partitionNumber;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topic.name}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "consumerKafkaListenerContainerFactory",
            autoStartup = "${application.kafka.payment-request-log.auto-startup}"
    )
    public void kafkaListener(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String value = record.value();
        try {
            User user = objectMapper.readValue(value, User.class);
            userService.addUser(user);
            acknowledgment.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Error deserializing user: {}", e.getMessage());
        }
    }

}
