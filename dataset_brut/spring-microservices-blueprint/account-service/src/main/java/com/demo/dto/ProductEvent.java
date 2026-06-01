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

    private EventType eventType;
    private Long productId;
    private String productName;
    private String description;
    private BigDecimal price;
    private Long creatorId;
    private LocalDateTime timestamp;
    private String username;
}
