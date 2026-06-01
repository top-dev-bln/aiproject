package com.demo.dto;

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
public class ProductSearchRequest {

    private Long id;
    private String name;
    private String description;
    private Long creatorId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}
