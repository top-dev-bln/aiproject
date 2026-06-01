package com.demo.dto;

import com.demo.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEvent {

    private EventType eventType; // CREATED, UPDATED, DELETED
    private Long productId;
    private String productName;
    private String description;
    private BigDecimal price;
    private Long creatorId;
    private LocalDateTime timestamp;
    private String username;

    public static ProductEvent created(Long productId, String productName, String description, 
                                     BigDecimal price, Long creatorId, String username) {
        ProductEvent event = new ProductEvent();
        event.setEventType(EventType.CREATED);
        event.setProductId(productId);
        event.setProductName(productName);
        event.setDescription(description);
        event.setPrice(price);
        event.setCreatorId(creatorId);
        event.setUsername(username);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    public static ProductEvent updated(Long productId, String productName, String description, 
                                     BigDecimal price, Long creatorId, String username) {
        ProductEvent event = new ProductEvent();
        event.setEventType(EventType.UPDATED);
        event.setProductId(productId);
        event.setProductName(productName);
        event.setDescription(description);
        event.setPrice(price);
        event.setCreatorId(creatorId);
        event.setUsername(username);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
}
