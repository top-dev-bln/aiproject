package com.demo.controller;

import com.demo.dto.ProductRequest;
import com.demo.dto.ProductSearchRequest;
import com.demo.dto.ProductUpdateRequest;
import com.demo.model.Product;
import com.demo.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/product")
@Tag(name = "product-controller", description = "CRUD and search endpoints for products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping()
    @Operation(summary = "List products by user", description = "Return all products created by a specific user")
    public List<Product> getProductsByUserId(@RequestParam long userId) {
        return productService.getProductsByUserId(userId);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's products", description = "Return all products created by the authenticated user")
    @PreAuthorize("isAuthenticated()")
    public List<Product> getMyProducts() {
        return productService.getMyProducts();
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Filter by id/name/description/creator/prices with pagination")
    public ResponseEntity<Page<Product>> searchProducts(ProductSearchRequest searchRequest) {
        Page<Product> products = productService.searchProducts(searchRequest);
        return ResponseEntity.ok(products);
    }

    @PostMapping()
    @Operation(summary = "Create a product", description = "Create a new product (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        Product createdProduct = productService.createProduct(productRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a product", description = "Partially update an existing product (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductUpdateRequest updateRequest) {
        Product updatedProduct = productService.updateProduct(id, updateRequest);
        return ResponseEntity.ok(updatedProduct);
    }

}
