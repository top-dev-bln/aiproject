package com.demo.service;

import com.demo.dto.ProductEvent;
import com.demo.dto.ProductRequest;
import com.demo.dto.ProductSearchRequest;
import com.demo.dto.ProductUpdateRequest;
import com.demo.exception.ForbiddenException;
import com.demo.exception.NotFoundException;
import com.demo.feign.UserFeignClient;
import com.demo.kafka.config.ProductKafkaProducer;
import com.demo.model.Product;
import com.demo.repo.ProductRepo;
import com.demo.util.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Service
@Slf4j
@Transactional
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ProductKafkaProducer productKafkaProducer;

    public List<Product> getProductsByUserId(long creatorId) {
        // Validate that the user exists before returning products
        userFeignClient.getAccountInfo(creatorId);
        return productRepo.findAllByCreatorId(creatorId);
    }

    public List<Product> getMyProducts() {
        // Get current user from JWT
        Long currentUserId = AuthUtils.getCurrentUserId();
        return productRepo.findAllByCreatorId(currentUserId);
    }

    public Page<Product> searchProducts(ProductSearchRequest searchRequest) {
        // Create pageable object
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(searchRequest.getSortDirection()) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC, 
            searchRequest.getSortBy()
        );

        Pageable pageable = PageRequest.of(
            searchRequest.getPage(), 
            searchRequest.getSize(), 
            sort
        );

        // Call repository search method
        return productRepo.searchProducts(
            searchRequest.getId(),
            searchRequest.getName(),
            searchRequest.getDescription(),
            searchRequest.getCreatorId(),
            searchRequest.getMinPrice(),
            searchRequest.getMaxPrice(),
            pageable
        );
    }

    public Product createProduct(ProductRequest productRequest) {

        // Get current user from JWT
        Long currentUserId = AuthUtils.getCurrentUserId();
        String currentUsername = AuthUtils.getCurrentUsername();

        // Create new product
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setCreatorId(currentUserId);

        Product savedProduct = productRepo.save(product);

        // Send Kafka event
        ProductEvent event = ProductEvent.created(
            savedProduct.getId(),
            savedProduct.getName(),
            savedProduct.getDescription(),
            savedProduct.getPrice(),
            savedProduct.getCreatorId(),
            currentUsername
        );
        productKafkaProducer.sendProductEvent(event);

        return savedProduct;
    }

    public Product updateProduct(Long productId, ProductUpdateRequest updateRequest) {

        Long currentUserId = AuthUtils.getCurrentUserId();
        String currentUsername = AuthUtils.getCurrentUsername();
        boolean isAdmin = AuthUtils.isCurrentUserAdmin();

        Optional<Product> productOpt = productRepo.findById(productId);
        if (productOpt.isEmpty()) {
            throw new NotFoundException("Product not found");
        }

        Product product = productOpt.get();
        // Allow update if user is admin OR if user is the creator
        if (!isAdmin && !product.getCreatorId().equals(currentUserId)) {
            throw new ForbiddenException("You can only update your own products or you need admin role");
        }

        if (updateRequest.getName() != null) {
            product.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            product.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getPrice() != null) {
            product.setPrice(updateRequest.getPrice());
        }

        Product updatedProduct = productRepo.save(product);

        // Send Kafka event
        ProductEvent event = ProductEvent.updated(
            updatedProduct.getId(),
            updatedProduct.getName(),
            updatedProduct.getDescription(),
            updatedProduct.getPrice(),
            updatedProduct.getCreatorId(),
            currentUsername
        );
        productKafkaProducer.sendProductEvent(event);

        return updatedProduct;
    }

}
