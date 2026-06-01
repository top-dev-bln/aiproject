# Thread Context Configuration

This package provides thread context management for microservices, ensuring that correlation IDs and user context are preserved across async operations.

## Components

### 1. CommonContext Interface
Defines the contract for thread-local context information:
- `correlationId`: Request tracing identifier
- `userId`: Current user ID
- `username`: Current username

### 2. DefaultCommonContext
Simple implementation of CommonContext interface.

### 3. CommonContextHolder
Thread-local storage for context information with support for:
- Regular thread-local storage
- Inheritable thread-local storage (for child threads)
- Integration with Spring Security context

### 4. CommonContextTaskDecorator
Task decorator that preserves context across async operations:
- Captures current context before async execution
- Restores context in the new thread
- Cleans up context after execution

### 5. AsyncConfig
Configuration class that enables async operations with context preservation:
- `@EnableAsync` annotation enables async processing
- Custom `TaskExecutor` with configurable thread pool
- Automatic context preservation via `CommonContextTaskDecorator`

### 6. AsyncUtils
Utility class for easy async operations with context preservation:
- `runAsync()` - Execute Runnable asynchronously
- `supplyAsync()` - Execute Supplier asynchronously
- Automatic context preservation

## Usage

### Using @Async Annotation
```java
@Service
public class MyService {
    
    @Async
    public CompletableFuture<String> processAsync(String data) {
        // Context is automatically preserved by CommonContextTaskDecorator
        String correlationId = CommonContextHolder.getCorrelationId();
        String userId = CommonContextHolder.getUserId();
        
        // Your async logic here
        return CompletableFuture.completedFuture("Processed: " + data);
    }
}
```

### Using AsyncUtils
```java
@Service
public class MyService {
    
    public CompletableFuture<String> processWithAsyncUtils(String data) {
        return AsyncUtils.supplyAsync(() -> {
            // Context is automatically preserved
            String correlationId = CommonContextHolder.getCorrelationId();
            String userId = CommonContextHolder.getUserId();
            
            // Your async logic here
            return "Processed: " + data;
        });
    }
}
```

### Manual Context Management
```java
// Set context manually
CommonContext context = CommonContextHolder.createFromSecurityContext();
CommonContextHolder.setContext(context, true); // true = inheritable

// Get context information
String correlationId = CommonContextHolder.getCorrelationId();
String userId = CommonContextHolder.getUserId();
String username = CommonContextHolder.getUsername();

// Clear context
CommonContextHolder.resetContext();
```

### Configuration Properties
Add these to your `application.yml`:
```yaml
app:
  async:
    core-pool-size: 5
    max-pool-size: 20
    queue-capacity: 100
    thread-name-prefix: microservice-async-
    keep-alive-seconds: 60
    await-termination-seconds: 30
```

## Benefits

1. **Request Tracing**: Correlation IDs are preserved across all async operations
2. **User Context**: User information is available in background threads
3. **Logging**: Structured logging with consistent correlation IDs
4. **Debugging**: Easier to trace requests through async operations
5. **Security**: User context is preserved for authorization checks
6. **Performance**: Configurable thread pools for optimal performance
7. **Reliability**: Graceful shutdown and error handling

## Current Status

✅ **Fully Configured and Ready to Use**

The async infrastructure is now properly configured and actively used in the application:
- `@Async` annotations work with context preservation
- `AsyncUtils` provides convenient async operations
- Thread pools are configurable via application properties
- Context is automatically preserved across all async operations

## Example Service

See `AsyncExampleService` for complete examples of how to use async operations with context preservation.
