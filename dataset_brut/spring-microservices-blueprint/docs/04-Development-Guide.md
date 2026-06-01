# 04 - Development Guide

🛠️ **Complete guide for developing and extending the microservices system**

## 🚀 Development Setup

### Prerequisites
- **Java 17** or higher
- **Maven 3.9+**
- **Docker** and **Docker Compose**
- **IDE** (IntelliJ IDEA, VS Code, Eclipse)

### Quick Development Setup
```bash
# Clone and setup
git clone <repository-url>
cd spring-boot-micro

# Build everything
mvn clean install -DskipTests

# Start development environment
docker compose up -d --build
```

## 📁 Project Structure

```
spring-boot-micro/
├── 📁 commons/                    # Shared libraries
│   ├── 📁 src/main/java/com/demo/
│   │   ├── 📁 config/             # Shared configurations
│   │   │   ├── 📁 openfeign/      # Feign client config
│   │   │   ├── 📁 thread/         # Async thread config
│   │   │   └── 📁 web/            # Web configurations
│   │   ├── 📁 dto/                # Data Transfer Objects
│   │   ├── 📁 exception/          # Common exceptions
│   │   ├── 📁 kafka/              # Kafka configurations
│   │   ├── 📁 logging/            # Logging utilities
│   │   └── 📁 security/           # Security utilities
│   └── 📄 pom.xml
├── 📁 account-service/            # User management service
│   ├── 📁 src/main/java/com/demo/
│   │   ├── 📁 config/             # Service-specific config
│   │   ├── 📁 controller/         # REST controllers
│   │   ├── 📁 dto/                # Request/Response DTOs
│   │   ├── 📁 entity/             # JPA entities
│   │   ├── 📁 kafka/              # Kafka producers/consumers
│   │   ├── 📁 repository/         # Data repositories
│   │   ├── 📁 security/           # JWT & Security
│   │   └── 📁 service/            # Business logic
│   ├── 📁 src/main/resources/
│   │   ├── 📄 application.yml     # Configuration
│   │   └── 📄 data.sql            # Sample data
│   ├── 📄 Dockerfile
│   └── 📄 pom.xml
├── 📁 product-service/            # Product management service
│   ├── 📁 src/main/java/com/demo/
│   │   ├── 📁 client/             # Feign clients
│   │   ├── 📁 controller/         # REST controllers
│   │   ├── 📁 dto/                # Request/Response DTOs
│   │   ├── 📁 entity/             # JPA entities
│   │   ├── 📁 kafka/              # Kafka producers/consumers
│   │   ├── 📁 repository/         # Data repositories
│   │   └── 📁 service/            # Business logic
│   ├── 📁 src/main/resources/
│   │   ├── 📄 application.yml     # Configuration
│   │   └── 📄 data.sql            # Sample data
│   ├── 📄 Dockerfile
│   └── 📄 pom.xml
├── 📁 fluentd/                    # Log aggregation
├── 📁 docs/                       # Documentation
├── 📄 docker-compose.yml          # Container orchestration
├── 📄 spring microservices.postman_collection.json
└── 📄 pom.xml                     # Parent POM
```

## 🔄 Development Workflow

### Making Changes to Commons
When you modify shared code in the `commons` module:

```bash
# 1. Build commons and install to local repository
mvn clean install -DskipTests

# 2. Rebuild and restart all services
docker compose up -d --build --force-recreate account-service-app product-service-app
```

### Making Changes to Account Service
```bash
# Option 1: Quick restart (if only Java code changed)
docker compose up -d --build --force-recreate account-service-app

# Option 2: Full rebuild (if dependencies changed)
mvn -pl account-service -am clean install -DskipTests
docker compose build account-service-app
docker compose up -d --force-recreate account-service-app
```

### Making Changes to Product Service
```bash
# Option 1: Quick restart
docker compose up -d --build --force-recreate product-service-app

# Option 2: Full rebuild
mvn -pl product-service -am clean install -DskipTests
docker compose build product-service-app
docker compose up -d --force-recreate product-service-app
```

### Clean Restart Everything
```bash
# Stop everything and clean up
docker compose down -v

# Rebuild from scratch
mvn clean install -DskipTests
docker compose build
docker compose up -d
```

## 🐛 Debugging Setup

### Debug Ports (Already Enabled)
- **Account Service:** localhost:5005
- **Product Service:** localhost:5006

### IntelliJ IDEA Setup
1. **Run → Edit Configurations**
2. **Add → Remote JVM Debug**
3. **Configure:**
   - Host: `localhost`
   - Port: `5005` (Account) or `5006` (Product)
   - Module: Select your service module
4. **Start debugging and set breakpoints**

### VS Code Setup
Add to `.vscode/launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug Account Service",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005
    },
    {
      "type": "java", 
      "name": "Debug Product Service",
      "request": "attach",
      "hostName": "localhost",
      "port": 5006
    }
  ]
}
```

## 🧪 Testing During Development

### Unit Tests
```bash
# Run tests for specific service
mvn -pl account-service test
mvn -pl product-service test

# Run all tests
mvn test
```

### Integration Tests
```bash
# Start test environment
docker compose up -d

# Wait for services to be ready
sleep 30

# Run API tests
curl http://localhost:8088/api/test/all
curl http://localhost:8089/api/product/search
```

### API Testing with Swagger
- **Account Service:** http://localhost:8088/swagger-ui/index.html
- **Product Service:** http://localhost:8089/swagger-ui/index.html

## 🔄 Adding New Features

### Adding a New REST Endpoint

#### 1. Create DTO Classes
```java
// Request DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeatureRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotNull(message = "Description is required")
    private String description;
    
    @DecimalMin(value = "0.0", message = "Price must be positive")
    private BigDecimal price;
}

// Response DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Long creatorId;
    private LocalDateTime createdAt;
}
```

#### 2. Create Controller
```java
@RestController
@RequestMapping("/api/feature")
@Tag(name = "Feature Management", description = "APIs for managing features")
public class FeatureController {
    
    @Autowired
    private FeatureService featureService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new feature", description = "Create a new feature (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Feature created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<FeatureResponse> createFeature(
            @Valid @RequestBody CreateFeatureRequest request,
            Authentication authentication) {
        
        FeatureResponse response = featureService.createFeature(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get feature by ID")
    public ResponseEntity<FeatureResponse> getFeature(@PathVariable Long id) {
        FeatureResponse response = featureService.getFeatureById(id);
        return ResponseEntity.ok(response);
    }
}
```

#### 3. Implement Service Logic
```java
@Service
@Transactional
public class FeatureService {
    
    @Autowired
    private FeatureRepository featureRepository;
    
    @Autowired
    private UserService userService;
    
    public FeatureResponse createFeature(CreateFeatureRequest request, String username) {
        // Get current user
        UserProfile user = userService.getUserByUsername(username);
        
        // Create feature entity
        Feature feature = new Feature();
        feature.setName(request.getName());
        feature.setDescription(request.getDescription());
        feature.setPrice(request.getPrice());
        feature.setCreatorId(user.getId());
        feature.setCreatedAt(LocalDateTime.now());
        
        // Save to database
        feature = featureRepository.save(feature);
        
        // Publish event
        publishFeatureCreatedEvent(feature);
        
        // Return response
        return mapToResponse(feature);
    }
    
    private void publishFeatureCreatedEvent(Feature feature) {
        FeatureCreatedEvent event = new FeatureCreatedEvent(
            feature.getId(),
            feature.getName(),
            feature.getCreatorId(),
            LocalDateTime.now()
        );
        
        applicationEventPublisher.publishEvent(event);
    }
}
```

#### 4. Test the Endpoint
```bash
# Test with Swagger UI
# Go to http://localhost:8088/swagger-ui/index.html

# Or test with curl
curl -X POST http://localhost:8088/api/feature \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{"name": "New Feature", "description": "Feature description", "price": 99.99}'
```

### Adding Inter-Service Communication

#### 1. Create Feign Client
```java
@FeignClient(name = "target-service", url = "${app.services.target-service.url}")
public interface TargetServiceClient {
    
    @GetMapping("/api/data/{id}")
    @Operation(summary = "Get data from target service")
    DataResponse getData(@PathVariable("id") Long id);
    
    @PostMapping("/api/data")
    @Operation(summary = "Create data in target service")
    DataResponse createData(@RequestBody CreateDataRequest request);
}
```

#### 2. Configure Service URL
```yaml
# application.yml
app:
  services:
    target-service:
      url: ${TARGET_SERVICE_URL:http://localhost:8090}
```

#### 3. Use in Service
```java
@Service
public class MyService {
    
    @Autowired
    private TargetServiceClient targetServiceClient;
    
    public void processData(Long id) {
        try {
            DataResponse data = targetServiceClient.getData(id);
            // Process data
            log.info("Received data: {}", data);
        } catch (FeignException e) {
            log.error("Failed to get data from target service: {}", e.getMessage());
            throw new ServiceCommunicationException("Target service unavailable");
        }
    }
}
```

### Adding Kafka Events

#### 1. Create Event Class
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureCreatedEvent {
    private Long featureId;
    private String featureName;
    private Long creatorId;
    private LocalDateTime timestamp;
    private String eventType = "FEATURE_CREATED";
}
```

#### 2. Publish Event
```java
@Service
public class FeatureEventPublisher {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @EventListener
    public void handleFeatureCreated(FeatureCreatedEvent event) {
        try {
            kafkaTemplate.send("feature-events", event.getFeatureId().toString(), event);
            log.info("Published feature created event: {}", event.getFeatureId());
        } catch (Exception e) {
            log.error("Failed to publish feature event: {}", e.getMessage());
        }
    }
}
```

#### 3. Consume Event
```java
@Component
public class FeatureEventConsumer {
    
    @KafkaListener(topics = "feature-events")
    public void handleFeatureEvent(FeatureCreatedEvent event) {
        try {
            log.info("Received feature event: {} for feature: {}", 
                event.getEventType(), event.getFeatureName());
            
            // Process event (update statistics, send notifications, etc.)
            processFeatureEvent(event);
            
        } catch (Exception e) {
            log.error("Failed to process feature event: {}", e.getMessage());
        }
    }
    
    private void processFeatureEvent(FeatureCreatedEvent event) {
        // Business logic for handling the event
        // Update user statistics, send notifications, etc.
    }
}
```

## 📊 Development Monitoring

### Application Logs
```bash
# View structured logs in Kibana
# 1. Open http://localhost:5601
# 2. Login with elastic/elastic
# 3. Create index pattern: fluentd-*
# 4. View logs in Discover tab

# Or view logs directly
docker compose logs -f account-service-app product-service-app
```

### Kafka Messages
```bash
# View Kafka topics and messages
# 1. Open http://localhost:8085
# 2. Browse topics: user-events, product-events
# 3. View message details

# Or use command line
docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic user-events --from-beginning
```

### Database Changes
```bash
# Connect to databases
docker exec -it account-db psql -U postgres -d account-service
docker exec -it product-db psql -U postgres -d product-service

# View tables and data
\dt
SELECT * FROM users LIMIT 5;
SELECT * FROM product LIMIT 5;

# Monitor database activity
SELECT * FROM pg_stat_activity;
```

### Health Checks
```bash
# Check service health
curl http://localhost:8088/actuator/health
curl http://localhost:8089/actuator/health

# View detailed health info
curl http://localhost:8088/actuator/health/db
curl http://localhost:8089/actuator/health/kafka

# Check all actuator endpoints
curl http://localhost:8088/actuator
```

## 🚀 Performance Optimization

### Development Performance
- Use Docker layer caching for faster builds
- Run only needed services during development
- Use IDE's hot reload capabilities
- Keep test data minimal

### Build Optimization
```bash
# Parallel builds
mvn -T 4 clean install -DskipTests

# Skip tests during development
mvn clean install -DskipTests

# Build specific modules only
mvn -pl account-service -am clean install -DskipTests
```

### Docker Optimization
```bash
# Use build cache
docker compose build

# Rebuild without cache (when needed)
docker compose build --no-cache

# Remove unused images
docker image prune -f
```

## 🔧 Configuration Management

### Environment-Specific Configuration
```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

---
spring:
  profiles: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/account-service
  kafka:
    bootstrap-servers: localhost:9092

---
spring:
  profiles: docker
  datasource:
    url: jdbc:postgresql://account-service-db:5432/account-service
  kafka:
    bootstrap-servers: kafka:29092
```

### External Configuration
```bash
# Override via environment variables
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:postgresql://localhost:5432/account-service

# Or via Docker Compose
environment:
  - SPRING_PROFILES_ACTIVE=docker
  - DB_URL=jdbc:postgresql://account-service-db:5432/account-service
```

## 📚 Development Best Practices

### Code Organization
- Keep controllers thin, put logic in services
- Use DTOs for API contracts
- Implement proper exception handling
- Add validation annotations
- Write meaningful tests

### Database Design
- Use appropriate indexes
- Follow naming conventions
- Implement proper constraints
- Use migrations for schema changes

### API Design
- Follow RESTful principles
- Use consistent response formats
- Implement proper HTTP status codes
- Add comprehensive validation
- Document all endpoints with Swagger

### Security
- Always validate input
- Use parameterized queries
- Implement proper authorization
- Log security events
- Keep dependencies updated

### Logging
- Use structured logging (JSON format)
- Include correlation IDs
- Log at appropriate levels
- Don't log sensitive information
- Use meaningful log messages

### Error Handling
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
        ErrorResponse error = new ErrorResponse(400, e.getMessages(), "VALIDATION_ERROR");
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        ErrorResponse error = new ErrorResponse(404, List.of(e.getMessage()), "NOT_FOUND");
        return ResponseEntity.notFound().build();
    }
}
```

## 🔄 Development Lifecycle

### Feature Development Process
1. **Create feature branch** from main
2. **Implement feature** with tests
3. **Test locally** with Docker environment
4. **Update documentation** if needed
5. **Create pull request**
6. **Code review** and merge

### Testing Strategy
- **Unit tests** for business logic
- **Integration tests** for API endpoints
- **Contract tests** for service communication
- **End-to-end tests** for critical user journeys

### Deployment Pipeline
1. **Code commit** triggers build
2. **Run tests** (unit, integration)
3. **Build Docker images**
4. **Deploy to staging**
5. **Run E2E tests**
6. **Deploy to production**

---

**Happy Coding! 🚀**

Next: [05-Testing-Guide.md](05-Testing-Guide.md) for comprehensive testing strategies