package com.demo.config.thread;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Slf4j
@Configuration
@EnableAsync
public class ThreadAsyncConfig implements AsyncConfigurer {

    @Value("${app.async.core-pool-size:5}")
    private int corePoolSize;

    @Value("${app.async.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${app.async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${app.async.thread-name-prefix:microservice-async-}")
    private String threadNamePrefix;

    @Value("${app.async.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${app.async.await-termination-seconds:30}")
    private int awaitTerminationSeconds;

    /**
     * Creates a custom TaskExecutor with context preservation.
     * 
     * @return configured TaskExecutor
     */
    @Bean("asyncTaskExecutor")
    public TaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Basic thread pool configuration
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setKeepAliveSeconds(keepAliveSeconds);

        // Context preservation
        executor.setTaskDecorator(new CommonContextTaskDecorator());

        // Rejection policy - caller runs when queue is full
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Allow core threads to timeout
        executor.setAllowCoreThreadTimeOut(true);

        // Graceful shutdown configuration
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);

        // Initialize the executor
        executor.initialize();

        log.info("Async TaskExecutor configured - core: {}, max: {}, queue: {}, prefix: {}", 
                corePoolSize, maxPoolSize, queueCapacity, threadNamePrefix);

        return executor;
    }

    /**
     * Configures the default async executor for @Async annotations.
     * 
     * @return the default async executor
     */
    @Override
    public Executor getAsyncExecutor() {
        return asyncTaskExecutor();
    }
}
