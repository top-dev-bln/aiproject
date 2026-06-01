package com.demo.controller;

import com.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@RestController
@RequestMapping("/api/product-events")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@Tag(name = "product-event-controller", description = "Simulates product events handled by account-service")
public class ProductEventController {

    @Autowired
    private UserService userService;

    /**
     * Test endpoint to simulate product event processing
     * This demonstrates how the account service would handle product events
     */
    @PostMapping("/test/{userId}")
    @Operation(summary = "Test product event handling", description = "Updates user stats based on a simulated product event action")
    public String testProductEvent(@PathVariable Long userId, @RequestParam String action) {
        log.info("Testing product event processing for user {} with action {}", userId, action);

        userService.updateUserProductStats(userId, action);

        return String.format("Product event processed for user %d with action: %s", userId, action);
    }
}
