package com.demo.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdateRequest {

    @Size(max = 100, message = "Product name must not exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Product description must not exceed 1000 characters")
    private String description;

    @Positive(message = "Price must be positive")
    private BigDecimal price;
}
