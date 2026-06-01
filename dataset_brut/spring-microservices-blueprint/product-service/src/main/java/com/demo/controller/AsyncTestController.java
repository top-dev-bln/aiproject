package com.demo.controller;

import com.demo.service.AsyncExampleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.concurrent.CompletableFuture;

/**
 * Test controller to demonstrate async functionality with context preservation.
 */

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Slf4j
@RestController
@RequestMapping("/api/async-test")
@RequiredArgsConstructor
@Tag(name = "async-test-controller", description = "Demonstrates async methods, context propagation, and error handling")
public class AsyncTestController {

    private final AsyncExampleService asyncExampleService;

    /**
     * Test endpoint for @Async annotation with context preservation.
     */
    @GetMapping("/annotation/{data}")
    @Operation(summary = "Test @Async annotation", description = "Runs a background task with context propagation using @Async")
    public CompletableFuture<ResponseEntity<String>> testAsyncAnnotation(@PathVariable String data) {
        log.info("Testing @Async annotation with data: {}", data);

        return asyncExampleService.processWithAsyncAnnotation(data)
                .thenApply(result -> {
                    log.info("Async annotation test completed: {}", result);
                    return ResponseEntity.ok(result);
                });
    }

    /**
     * Test endpoint for AsyncUtils with context preservation.
     */
    @GetMapping("/utils/{data}")
    @Operation(summary = "Test AsyncUtils helper", description = "Runs a background task using AsyncUtils with MDC context")
    public CompletableFuture<ResponseEntity<String>> testAsyncUtils(@PathVariable String data) {
        log.info("Testing AsyncUtils with data: {}", data);

        return asyncExampleService.processWithAsyncUtils(data)
                .thenApply(result -> {
                    log.info("AsyncUtils test completed: {}", result);
                    return ResponseEntity.ok(result);
                });
    }

    /**
     * Test endpoint for chained async operations.
     */
    @GetMapping("/chain/{data}")
    @Operation(summary = "Test async chaining", description = "Demonstrates composing multiple async stages")
    public CompletableFuture<ResponseEntity<String>> testAsyncChaining(@PathVariable String data) {
        log.info("Testing async chaining with data: {}", data);

        return asyncExampleService.processWithChaining(data)
                .thenApply(result -> {
                    log.info("Async chaining test completed: {}", result);
                    return ResponseEntity.ok(result);
                });
    }

    /**
     * Test endpoint for error handling in async operations.
     */
    @GetMapping("/error/{data}")
    @Operation(summary = "Test async error handling", description = "Triggers and handles an error in an async flow")
    public CompletableFuture<ResponseEntity<String>> testAsyncErrorHandling(@PathVariable String data) {
        log.info("Testing async error handling with data: {}", data);

        return asyncExampleService.processWithErrorHandling(data)
                .thenApply(result -> {
                    log.info("Async error handling test completed: {}", result);
                    return ResponseEntity.ok(result);
                });
    }

    /**
     * Test endpoint to verify async configuration is loaded.
     */
    @GetMapping("/config-status")
    @Operation(summary = "Check async configuration", description = "Verifies async configuration is loaded")
    public ResponseEntity<String> getConfigStatus() {
        log.info("Checking async configuration status");

        // This will only work if AsyncConfig is properly loaded
        return ResponseEntity.ok("Async configuration is loaded and working! " +
                "Try the other endpoints to test async operations with context preservation.");
    }
}
