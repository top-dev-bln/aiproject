# 06 - Debugging Guide

🐛 **Complete guide for debugging microservices in Docker containers**

## 🚀 Quick Debug Setup

Debug ports are **already enabled** in the Docker containers:
- **Account Service:** localhost:5005
- **Product Service:** localhost:5006

Just start the services and connect your debugger!

```bash
# Start services (debug ports automatically enabled)
mvn clean install -DskipTests
docker compose up -d --build

# Connect debugger to localhost:5005 or localhost:5006
```

## 🔧 IDE Configuration

### IntelliJ IDEA

#### 1. Create Remote Debug Configuration
1. **Go to:** Run → Edit Configurations
2. **Click:** + → Remote JVM Debug
3. **Configure:**
   - **Name:** Account Service Debug
   - **Host:** localhost
   - **Port:** 5005
   - **Use module classpath:** Select account-service module
4. **Save:** Apply → OK

#### 2. For Product Service
- **Name:** Product Service Debug
- **Port:** 5006
- **Module:** product-service

#### 3. Start Debugging
1. **Set breakpoints** in your code
2. **Start debug configuration** (green bug icon)
3. **Make API calls** to trigger breakpoints

### Visual Studio Code

#### 1. Create launch.json
Create `.vscode/launch.json` in project root:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug Account Service",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005,
      "projectName": "account-service"
    },
    {
      "type": "java",
      "name": "Debug Product Service", 
      "request": "attach",
      "hostName": "localhost",
      "port": 5006,
      "projectName": "product-service"
    }
  ]
}
```

#### 2. Start Debugging
1. **Open Debug panel** (Ctrl+Shift+D)
2. **Select configuration** from dropdown
3. **Click play button** or press F5
4. **Set breakpoints** and make API calls

### Eclipse

#### 1. Create Debug Configuration
1. **Go to:** Run → Debug Configurations
2. **Right-click:** Remote Java Application → New
3. **Configure:**
   - **Name:** Account Service Debug
   - **Project:** account-service
   - **Host:** localhost
   - **Port:** 5005
4. **Click:** Debug

## 🔍 Debug Features Available

### JVM Debug Agent Configuration
```dockerfile
# Already configured in Dockerfiles
ENTRYPOINT ["java", \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", "/app/app.jar"]
```

**Parameters explained:**
- `transport=dt_socket` - Use socket transport
- `server=y` - Act as debug server (accept connections)
- `suspend=n` - Don't wait for debugger to start
- `address=*:5005` - Listen on all interfaces, port 5005

### Enhanced Logging
Debug mode includes enhanced logging for:
- **Spring Framework** - DEBUG level
- **Hibernate/JPA** - SQL queries and parameters
- **HTTP Requests** - Full request/response details
- **Kafka Messages** - Message content and headers
- **Security** - Authentication and authorization details

## 🎯 Debugging Scenarios

### Scenario 1: Debug API Endpoint

#### 1. Set Breakpoint
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Set breakpoint here
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword())
        );
        // Execution will pause here when API is called
    }
}
```

#### 2. Make API Call
```bash
curl -X POST http://localhost:8088/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username": "test", "password": "password123"}'
```

#### 3. Debug Session
- **Inspect variables:** loginRequest, authentication
- **Step through code:** F8 (Step Over), F7 (Step Into)
- **Evaluate expressions:** Alt+F8
- **View call stack:** Debug panel

### Scenario 2: Debug Service Communication

#### 1. Set Breakpoint in Product Service
```java
@Service
public class ProductService {
    
    @Autowired
    private AccountServiceClient accountServiceClient;
    
    public List<Product> getProductsByUserId(Long userId) {
        // Set breakpoint here
        UserProfile user = accountServiceClient.getUserById(userId);
        // Debug Feign client call
        return productRepository.findByCreatorId(userId);
    }
}
```

#### 2. Make API Call
```bash
curl "http://localhost:8089/api/product?userId=1"
```

#### 3. Debug Inter-Service Call
- **Step into Feign client** to see HTTP call details
- **Inspect user object** returned from Account Service
- **Check network communication** in logs

### Scenario 3: Debug Kafka Events

#### 1. Set Breakpoint in Event Publisher
```java
@Service
public class ProductEventPublisher {
    
    @EventListener
    public void handleProductCreated(ProductCreatedEvent event) {
        // Set breakpoint here
        kafkaTemplate.send("product-events", event.getProductId().toString(), event);
        // Debug Kafka message publishing
    }
}
```

#### 2. Set Breakpoint in Event Consumer
```java
@Component
public class ProductEventConsumer {
    
    @KafkaListener(topics = "product-events")
    public void handleProductEvent(ProductCreatedEvent event) {
        // Set breakpoint here
        log.info("Received product event: {}", event);
        // Debug Kafka message consumption
    }
}
```

#### 3. Trigger Event
```bash
# Create product (triggers Kafka event)
curl -X POST http://localhost:8089/api/product \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"name": "Debug Product", "price": 99.99}'
```

### Scenario 4: Debug Database Operations

#### 1. Enable SQL Logging
Already configured in `application.yml`:
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### 2. Set Breakpoint in Repository
```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Query("SELECT p FROM Product p WHERE p.creatorId = :creatorId")
    List<Product> findByCreatorId(@Param("creatorId") Long creatorId);
    // Set breakpoint in service method that calls this
}
```

#### 3. Debug Database Query
- **View generated SQL** in logs
- **Inspect query parameters** 
- **Check result mapping**

## 🛠️ Debug Tools & Commands

### View Real-time Logs
```bash
# All services
docker compose logs -f

# Specific services with debug info
docker compose logs -f account-service-app product-service-app

# Filter for specific log levels
docker compose logs -f account-service-app | grep DEBUG
docker compose logs -f product-service-app | grep ERROR
```

### Check Debug Port Status
```bash
# Verify debug ports are listening
netstat -an | findstr :5005
netstat -an | findstr :5006

# Check from inside container
docker exec account-service-app netstat -an | grep 5005
```

### Container Inspection
```bash
# Check container status
docker compose ps

# Inspect container details
docker inspect account-service-app

# Check port mappings
docker port account-service-app
```

### Database Debugging
```bash
# Connect to databases directly
docker exec -it account-db psql -U postgres -d account-service
docker exec -it product-db psql -U postgres -d product-service

# View tables and data
\dt
SELECT * FROM users LIMIT 5;
SELECT * FROM product LIMIT 5;

# Check active connections
SELECT * FROM pg_stat_activity;
```

## 🔧 Advanced Debugging

### Remote Debugging Over Network
If running on remote server:
```bash
# SSH tunnel to remote debug ports
ssh -L 5005:localhost:5005 -L 5006:localhost:5006 user@remote-server

# Then connect debugger to localhost:5005/5006 as usual
```

### Debug with Custom JVM Options
Modify Dockerfile for additional debug options:
```dockerfile
ENTRYPOINT ["java", \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Dspring.profiles.active=debug", \
    "-Dlogging.level.com.demo=DEBUG", \
    "-jar", "/app/app.jar"]
```

### Debug Startup Issues
```bash
# Remove suspend=n to wait for debugger
# Modify Dockerfile temporarily:
"-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"

# Service will wait for debugger connection before starting
```

### Memory Debugging
```bash
# Add heap dump options
"-XX:+HeapDumpOnOutOfMemoryError", \
"-XX:HeapDumpPath=/tmp/heapdump.hprof"

# Monitor memory usage
docker stats account-service-app product-service-app
```

## 🚨 Troubleshooting Debug Issues

### Issue: Can't Connect to Debug Port

**Solutions:**
```bash
# 1. Check if service is running
docker compose ps

# 2. Verify port is exposed
docker port account-service-app

# 3. Check firewall/antivirus
# Temporarily disable and test

# 4. Restart service
docker compose restart account-service-app

# 5. Check logs for debug agent startup
docker compose logs account-service-app | grep "Listening for transport"
```

### Issue: Breakpoints Not Hit

**Solutions:**
1. **Verify source code matches** deployed JAR
2. **Rebuild and redeploy:**
   ```bash
   mvn clean install -DskipTests
   docker compose up -d --build --force-recreate account-service-app
   ```
3. **Check breakpoint location** (not in comments/empty lines)
4. **Ensure request reaches the code path**

### Issue: Debug Session Disconnects

**Solutions:**
1. **Check container health:**
   ```bash
   docker compose ps
   docker compose logs account-service-app
   ```
2. **Increase timeout** in IDE debug configuration
3. **Check network stability**
4. **Restart debug session**

### Issue: Performance Impact

**Solutions:**
1. **Use conditional breakpoints** to reduce hits
2. **Remove breakpoints** when not needed
3. **Debug in development only** (not production)
4. **Monitor resource usage:**
   ```bash
   docker stats
   ```

## 📊 Debug Performance Tips

### Efficient Debugging
- **Set specific breakpoints** rather than stepping through everything
- **Use conditional breakpoints** for specific scenarios
- **Remove breakpoints** after debugging
- **Use logging** for high-frequency code paths

### Debug Configuration
```yaml
# application-debug.yml
logging:
  level:
    com.demo: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

### Memory Considerations
- Debug mode uses more memory
- Monitor container resources
- Increase Docker memory if needed
- Use heap profiling for memory issues

## 🎯 Debug Best Practices

### Code Debugging
1. **Start with logs** before setting breakpoints
2. **Use meaningful variable names** for easier debugging
3. **Add debug logging** in complex business logic
4. **Test edge cases** during debug sessions

### Environment Debugging
1. **Keep debug and production separate**
2. **Use debug profiles** for different configurations
3. **Document debug procedures** for team members
4. **Automate debug environment setup**

### Security Considerations
1. **Don't expose debug ports** in production
2. **Use secure networks** for remote debugging
3. **Disable debug** in production builds
4. **Monitor debug access** in logs

---

**Happy Debugging! 🐛**

Next: [07-Docker-Operations.md](07-Docker-Operations.md) for container management