package com.demo.service;

import com.demo.context.CommonContextHolder;
import com.demo.util.AsyncUtils;
import com.demo.exception.HttpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.Arrays;

import java.util.concurrent.CompletableFuture;

/**
 * Example service demonstrating async operations with context preservation.
 * 
 * This service shows how to use @Async annotations and AsyncUtils
 * while maintaining correlation IDs and user context across threads.
 */

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Slf4j
@Service
public class AsyncExampleService {

    /**
     * Example of using @Async annotation with context preservation.
     * The context will be automatically preserved by CommonContextTaskDecorator.
     */
    public CompletableFuture<String> processWithAsyncAnnotation(String data) {
        log.info("Starting async processing with @Async - correlationId: {}", 
                CommonContextHolder.getCorrelationId());

        // Simulate some processing time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Async processing interrupted");
        }

        String correlationId = CommonContextHolder.getCorrelationId();
        String userId = CommonContextHolder.getUserId();
        String username = CommonContextHolder.getUsername();

        log.info("Async processing completed - correlationId: {}, userId: {}, username: {}", 
                correlationId, userId, username);

        return CompletableFuture.completedFuture(
            String.format("Processed: %s by user: %s (correlationId: %s)", 
                         data, username, correlationId)
        );
    }

    /**
     * Example of using AsyncUtils for manual async operations.
     */
    public CompletableFuture<String> processWithAsyncUtils(String data) {
        log.info("Starting async processing with AsyncUtils - correlationId: {}", 
                CommonContextHolder.getCorrelationId());

        return AsyncUtils.supplyAsync(() -> {
            String correlationId = CommonContextHolder.getCorrelationId();
            String userId = CommonContextHolder.getUserId();
            String username = CommonContextHolder.getUsername();

            log.info("Processing in async thread - correlationId: {}, userId: {}, username: {}", 
                    correlationId, userId, username);

            // Simulate some processing time
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Async processing interrupted");
            }

            return String.format("AsyncUtils processed: %s by user: %s (correlationId: %s)", 
                               data, username, correlationId);
        });
    }

    /**
     * Example of chaining multiple async operations.
     */
    public CompletableFuture<String> processWithChaining(String data) {
        log.info("Starting chained async processing - correlationId: {}", 
                CommonContextHolder.getCorrelationId());

        return AsyncUtils.supplyAsync(() -> {
            log.info("Step 1: Initial processing - correlationId: {}", 
                    CommonContextHolder.getCorrelationId());
            return "Step1: " + data;
        })
        .thenCompose(step1Result -> AsyncUtils.supplyAsync(() -> {
            log.info("Step 2: Secondary processing - correlationId: {}", 
                    CommonContextHolder.getCorrelationId());
            return step1Result + " -> Step2: Enhanced";
        }))
        .thenCompose(step2Result -> AsyncUtils.supplyAsync(() -> {
            log.info("Step 3: Final processing - correlationId: {}", 
                    CommonContextHolder.getCorrelationId());
            return step2Result + " -> Step3: Complete";
        }));
    }

    /**
     * Example of error handling in async operations.
     */
    public CompletableFuture<String> processWithErrorHandling(String data) {
        log.info("Starting async processing with error handling - correlationId: {}", 
                CommonContextHolder.getCorrelationId());

        return AsyncUtils.supplyAsync(() -> {
            String correlationId = CommonContextHolder.getCorrelationId();
            log.info("Processing with potential error - correlationId: {}", correlationId);

            if ("error".equals(data)) {
                throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    Arrays.asList("Simulated error in async processing"));
            }

            return "Successfully processed: " + data;
        })
        .handle((result, throwable) -> {
            if (throwable != null) {
                log.error("Async processing failed - correlationId: {}, error: {}", 
                         CommonContextHolder.getCorrelationId(), throwable.getMessage());
                return "Error occurred: " + throwable.getMessage();
            }
            return result;
        });
    }
}
