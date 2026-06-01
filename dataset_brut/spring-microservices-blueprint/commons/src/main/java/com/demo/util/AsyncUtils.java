package com.demo.util;

import com.demo.context.CommonContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Utility class for asynchronous operations with context preservation.
 * 
 * This class provides convenient methods for executing async operations
 * while preserving correlation IDs and user context across thread boundaries.
 */

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Slf4j
@Component
public class AsyncUtils {

    private static TaskExecutor taskExecutor;

    @Autowired
    public void setTaskExecutor(TaskExecutor taskExecutor) {
        AsyncUtils.taskExecutor = taskExecutor;
    }

    /**
     * Executes a task asynchronously with context preservation.
     * 
     * @param task the task to execute
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> runAsync(Runnable task) {
        if (taskExecutor == null) {
            log.warn("TaskExecutor not initialized, falling back to default executor");
            return CompletableFuture.runAsync(task);
        }

        log.debug("Executing async task with context preservation - correlationId: {}", 
                 CommonContextHolder.getCorrelationId());

        return CompletableFuture.runAsync(task, taskExecutor);
    }

    /**
     * Executes a supplier asynchronously with context preservation.
     * 
     * @param supplier the supplier to execute
     * @param <T> the return type
     * @return CompletableFuture that completes with the supplier's result
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        if (taskExecutor == null) {
            log.warn("TaskExecutor not initialized, falling back to default executor");
            return CompletableFuture.supplyAsync(supplier);
        }

        log.debug("Executing async supplier with context preservation - correlationId: {}", 
                 CommonContextHolder.getCorrelationId());

        return CompletableFuture.supplyAsync(supplier, taskExecutor);
    }

    /**
     * Executes a task asynchronously with a specific executor.
     * 
     * @param task the task to execute
     * @param executor the executor to use
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> runAsync(Runnable task, TaskExecutor executor) {
        log.debug("Executing async task with custom executor - correlationId: {}", 
                 CommonContextHolder.getCorrelationId());

        return CompletableFuture.runAsync(task, executor);
    }

    /**
     * Executes a supplier asynchronously with a specific executor.
     * 
     * @param supplier the supplier to execute
     * @param executor the executor to use
     * @param <T> the return type
     * @return CompletableFuture that completes with the supplier's result
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, TaskExecutor executor) {
        log.debug("Executing async supplier with custom executor - correlationId: {}", 
                 CommonContextHolder.getCorrelationId());

        return CompletableFuture.supplyAsync(supplier, executor);
    }

    /**
     * Creates a completed CompletableFuture with the given value.
     * 
     * @param value the value to complete with
     * @param <T> the type of the value
     * @return a completed CompletableFuture
     */
    public static <T> CompletableFuture<T> completedFuture(T value) {
        return CompletableFuture.completedFuture(value);
    }

    /**
     * Creates a failed CompletableFuture with the given exception.
     * 
     * @param throwable the exception to fail with
     * @param <T> the type of the future
     * @return a failed CompletableFuture
     */
    public static <T> CompletableFuture<T> failedFuture(Throwable throwable) {
        return CompletableFuture.failedFuture(throwable);
    }
}
