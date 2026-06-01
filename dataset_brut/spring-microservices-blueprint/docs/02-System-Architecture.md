# 02 - System Architecture Guide

🏗️ **Understanding the microservices architecture and design patterns**

## 🏗️ Multi-Module Maven Architecture

This project demonstrates **Maven Multi-Module** best practices for microservices development:

### Project Structure
```
spring-microservices-blueprint/
├── pom.xml                    # Parent POM with dependency management
├── commons/                   # Shared utilities and DTOs
│   ├── pom.xml               # Commons module POM
│   └── src/main/java/
│       └── com/microservices/commons/
│           ├── dto/           # Shared Data Transfer Objects
│           ├── exception/     # Common exception classes
│           └── util/          # Utility classes
├── account-service/           # User management microservice
│   ├── pom.xml               # Account service POM
│   ├── Dockerfile            # Container build file
│   └── src/main/java/
│       └── com/microservices/account/
└── product-service/           # Product management microservice
    ├── pom.xml               # Product service POM
    ├── Dockerfile            # Container build file
    └── src/main/java/
        └── com/microservices/product/
```

### Maven Module Dependencies
```
Parent POM
├── Commons Module (shared utilities)
├── Account Service Module
│   └── depends on: Commons
└── Product Service Module
    └── depends on: Commons
```

### Benefits of Multi-Module Architecture
- **Shared Dependencies**: Common libraries managed in parent POM
- **Code Reusability**: Shared DTOs and utilities in commons module
- **Consistent Versioning**: All modules use same version from parent
- **Simplified Build**: Single `mvn clean install` builds everything
- **Dependency Management**: Centralized version control for all dependencies
- **IDE Support**: Better project navigation and refactoring

### Build Process
```bash
# Build all modules in correct order
mvn clean install -DskipTests

# Build specific module (with dependencies)
mvn clean install -pl account-service -am -DskipTests

# Build without dependencies
mvn clean install -pl commons -DskipTests
```

## 🎯 Architecture Overview

This project implements a **distributed microservices architecture** with modern patterns and technologies for learning purposes.

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Layer                             │
│   Web Browser, Mobile App, Postman, Swagger UI, curl       │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP/REST
┌─────────────────────▼───────────────────────────────────────┐
│                  Service Layer                              │
│  ┌─────────────────┐         ┌─────────────────┐           │
│  │ Account Service │◄────────┤ Product Service │           │
│  │   Port 8088     │  Feign  │   Port 8089     │           │
│  │                 │  Client │                 │           │
│  │ • Authentication│         │ • Product CRUD  │           │
│  │ • User Mgmt     │         │ • Search/Filter │           │
│  │ • JWT Tokens    │         │ • Authorization │           │
│  │ • Swagger UI    │         │ • Swagger UI    │           │
│  └─────────┬───────┘         └─────────┬───────┘           │
└────────────┼─────────────────────────────┼───────────────────┘
             │                           │
             │ Kafka Events              │ Kafka Events
             ▼                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  Message Layer                              │
│              ┌─────────────────┐                            │
│              │  Kafka Cluster  │                            │
│              │   Port 9092     │                            │
│              │                 │                            │
│              │ Topics:         │                            │
│              │ • user-events   │                            │
│              │ • product-events│                            │
│              └─────────────────┘                            │
└─────────────────────────────────────────────────────────────┘
             │                           │
┌────────────▼───────────┐   ┌───────────▼───────────────────┐
│    Data Layer          │   │    Observability Layer        │
│ ┌─────────┐ ┌────────┐ │   │ ┌─────────────┐ ┌───────────┐ │
│ │Account  │ │Product │ │   │ │Elasticsearch│ │  Kibana   │ │
│ │   DB    │ │   DB   │ │   │ │Port 9200    │ │Port 5601  │ │
│ │(5432)   │ │(5434)  │ │   │ │             │ │           │ │
│ └─────────┘ └────────┘ │   │ │ Log Storage │ │Log Viewer │ │
└─────────────────────────┘   │ └─────────────┘ └───────────┘ │
                              │ ┌─────────────┐ ┌───────────┐ │
                              │ │   Fluentd   │ │  Kafdrop  │ │
                              │ │(24224)      │ │(8085)     │ │
                              │ │Log Shipper  │ │Kafka UI   │ │
                              │ └─────────────┘ └───────────┘ │
                              └─────────────────────────────────┘
```

## 🎯 Core Services

### Account Service (Port 8088)
**Primary Responsibilities:**
- User registration and authentication
- JWT token generation and validation
- User profile management
- Role-based access control (RBAC)

**Key Features:**
- Spring Security with JWT
- Password encryption (BCrypt)
- Role management (USER, ADMIN)
- User profile APIs
- **Swagger UI:** http://localhost:8088/swagger-ui/index.html

**Technology Stack:**
- Spring Boot 3.x
- Spring Security 6.x
- Spring Data JPA
- PostgreSQL
- JWT (jsonwebtoken)
- Swagger/OpenAPI 3

**Database Schema:**
```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(120) NOT NULL,
    enabled BOOLEAN DEFAULT true
);

-- Roles table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL
);

-- User-Role mapping
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id),
    role_id INT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);
```

### Product Service (Port 8089)
**Primary Responsibilities:**
- Product CRUD operations
- Advanced product search with filtering
- Product ownership validation
- Business logic for product management

**Key Features:**
- Advanced search with pagination
- Price range filtering
- Creator-based filtering
- Role-based product creation (ADMIN only)
- **Swagger UI:** http://localhost:8089/swagger-ui/index.html

**Technology Stack:**
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- OpenFeign (service communication)
- Swagger/OpenAPI 3

**Database Schema:**
```sql
-- Products table
CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    creator_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_product_creator_id ON product(creator_id);
CREATE INDEX idx_product_name ON product(name);
CREATE INDEX idx_product_price ON product(price);
```

## 🔄 Communication Patterns

### 1. Synchronous Communication (REST + Feign)

**Implementation:**
```java
// Product Service calls Account Service
@FeignClient(name = "account-service", url = "${app.services.account-service.url}")
public interface AccountServiceClient {
    @GetMapping("/api/user/{userId}")
    UserProfile getUserById(@PathVariable Long userId);
}
```

**Communication Flow:**
```
Product Service ──HTTP──► Account Service
      │                        │
      │ 1. GET /api/user/123    │
      │ ──────────────────────► │
      │                        │ 2. Query Database
      │                        │ ──────────────►
      │                        │
      │ 3. UserProfile JSON    │ 4. Return User
      │ ◄────────────────────── │ ◄──────────────
      │                        │
   5. Continue Processing
```

**Use Cases:**
- Product Service validates user existence before operations
- Real-time data consistency requirements
- Direct API calls between services

### 2. Asynchronous Communication (Kafka)

**Event Publishing:**
```java
// Account Service publishes user events
@EventListener
public void handleUserRegistration(UserRegistrationEvent event) {
    kafkaTemplate.send("user-events", event.getUserId().toString(), event);
}

// Product Service publishes product events
@EventListener
public void handleProductCreation(ProductCreatedEvent event) {
    kafkaTemplate.send("product-events", event.getProductId().toString(), event);
}
```

**Event Consumption:**
```java
// Cross-service event handling
@KafkaListener(topics = "product-events")
public void handleProductEvent(ProductCreatedEvent event) {
    // Update user statistics
    userService.updateProductStats(event.getCreatorId(), "CREATED");
}
```

**Message Flow:**
```
Service A ──publish──► Kafka Topic ──consume──► Service B
    │                      │                      │
    │ 1. Product Created    │                      │
    │ ────────────────────► │                      │
    │                      │ 2. Event Available   │
    │                      │ ────────────────────► │
    │                      │                      │ 3. Process Event
    │                      │                      │ ──────────────►
```

**Use Cases:**
- Event-driven architecture
- Audit logging and analytics
- Loose coupling between services
- Eventual consistency

## 🗄️ Data Architecture

### Database Per Service Pattern

Each service owns its data completely:

**Benefits:**
- **Data Encapsulation:** Services can't directly access other services' data
- **Technology Diversity:** Each service can use different database technologies
- **Independent Scaling:** Scale databases based on service needs
- **Fault Isolation:** Database issues don't cascade across services

**Challenges:**
- **Data Consistency:** Must handle eventual consistency
- **Cross-Service Queries:** Need to aggregate data from multiple services
- **Transaction Management:** Distributed transactions are complex

### Data Consistency Strategy

**Strong Consistency:** Within service boundaries
```java
@Transactional
public Product createProduct(CreateProductRequest request) {
    // All operations within same database transaction
    Product product = new Product(request);
    product = productRepository.save(product);
    
    // Publish event after successful transaction
    publishProductCreatedEvent(product);
    return product;
}
```

**Eventual Consistency:** Across service boundaries
```java
@KafkaListener(topics = "user-events")
public void handleUserEvent(UserEvent event) {
    // Eventually consistent update based on events
    if (event.getType() == UserEventType.DELETED) {
        productService.handleUserDeletion(event.getUserId());
    }
}
```

## 🔐 Security Architecture

### JWT-Based Authentication Flow

```
┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client    │    │ Account Service │    │ Product Service │
└──────┬──────┘    └─────────┬───────┘    └─────────┬───────┘
       │                     │                      │
       │ 1. Login Request    │                      │
       ├────────────────────►│                      │
       │                     │                      │
       │ 2. JWT Token        │                      │
       │◄────────────────────┤                      │
       │                     │                      │
       │ 3. API Call + JWT   │                      │
       ├─────────────────────┼─────────────────────►│
       │                     │                      │
       │                     │ 4. Validate User    │
       │                     │◄─────────────────────┤
       │                     │                      │
       │                     │ 5. User Info        │
       │                     ├─────────────────────►│
       │                     │                      │
       │ 6. API Response     │                      │
       │◄─────────────────────┼──────────────────────┤
```

### Role-Based Access Control (RBAC)

**Role Hierarchy:**
```
ROLE_ADMIN
    │
    ├── Can create products
    ├── Can update any product
    ├── Can delete any product
    ├── Can access admin endpoints
    └── Has all USER permissions
    
ROLE_USER
    │
    ├── Can view own profile
    ├── Can search products
    ├── Can update own products
    └── Can access user endpoints
```

**Implementation:**
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/api/product")
public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest request) {
    // Only admins can create products
}

@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@GetMapping("/api/user/me")
public ResponseEntity<UserProfile> getCurrentUser() {
    // Both users and admins can access
}

@PreAuthorize("@productService.isOwnerOrAdmin(#productId, authentication.name)")
@PatchMapping("/api/product/{productId}")
public ResponseEntity<Product> updateProduct(@PathVariable Long productId, @RequestBody UpdateProductRequest request) {
    // Only product owner or admin can update
}
```

## 📊 Observability Architecture

### Centralized Logging (ELK Stack)

**Log Flow:**
```
┌─────────────┐    ┌─────────────┐    ┌─────────────────┐    ┌─────────────┐
│   Service   │    │   Fluentd   │    │ Elasticsearch   │    │   Kibana    │
│    Logs     ├───►│ (Collector) ├───►│   (Storage)     ├───►│ (Visualize) │
└─────────────┘    └─────────────┘    └─────────────────┘    └─────────────┘
```

**Structured Logging:**
```java
// Automatic correlation ID injection
@Component
public class CorrelationLoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

**Log Format:**
```json
{
  "@timestamp": "2024-01-01T12:00:00.000Z",
  "level": "INFO",
  "logger_name": "com.demo.service.ProductService",
  "thread_name": "http-nio-8089-exec-1",
  "correlation_id": "abc123-def456-ghi789",
  "request_method": "POST",
  "request_url": "/api/product",
  "message": "Product created successfully",
  "user_id": "123",
  "product_id": "456"
}
```

### Distributed Tracing

**Correlation ID Flow:**
```
Client Request ──correlation_id──► Service A ──same_id──► Service B ──same_id──► Database
      │                              │                      │                      │
      │                              │ Log: correlation_id  │ Log: correlation_id  │ Log: correlation_id
      │                              │ ──────────────────► │ ──────────────────► │ ──────────────────►
      │                              │                      │                      │
      │                              │                      │                      │
      │ ◄──────────────────────────── │ ◄──────────────────── │ ◄──────────────────── │
                                Response with same correlation_id
```

**Implementation:**
```java
// Automatic correlation ID propagation
@Component
public class CorrelationInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, 
            byte[] body, 
            ClientHttpRequestExecution execution) throws IOException {
        
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            request.getHeaders().add("X-Correlation-ID", correlationId);
        }
        
        return execution.execute(request, body);
    }
}
```

## 🚀 Deployment Architecture

### Containerization Strategy

**Multi-stage Docker Build:**
```dockerfile
# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml ./
COPY commons/pom.xml commons/pom.xml
COPY account-service/pom.xml account-service/pom.xml
COPY product-service/pom.xml product-service/pom.xml
RUN mvn -q -DskipTests dependency:go-offline
COPY . .
RUN mvn -q -pl account-service -am -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
RUN apk add --no-cache curl
COPY --from=build --chown=appuser:appgroup /app/account-service/target/*.jar app.jar
USER appuser
EXPOSE 8088 5005
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8088/actuator/health || exit 1
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
```

**Benefits:**
- **Smaller images:** Multi-stage builds reduce final image size
- **Security:** Non-root user execution
- **Debugging:** Debug ports enabled for development
- **Health checks:** Container health monitoring
- **Resource limits:** JVM container awareness

### Container Orchestration

**Docker Compose Structure:**
```yaml
services:
  # Application Services
  account-service-app:
    build: ./account-service
    depends_on:
      account-service-db:
        condition: service_healthy
      kafka:
        condition: service_healthy
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_URL=jdbc:postgresql://account-service-db:5432/account-service
    ports:
      - "8088:8088"
      - "5005:5005"  # Debug port
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8088/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped
    
  # Infrastructure Services
  kafka:
    image: confluentinc/cp-kafka:7.3.2
    depends_on:
      zookeeper:
        condition: service_healthy
    environment:
      KAFKA_ZOOKEEPER_CONNECT: "zookeeper:2181"
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
    healthcheck:
      test: ["CMD", "nc", "-z", "localhost", "9092"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 30s
```

## 🔧 Configuration Management

### Environment-Based Configuration

**Application Properties:**
```yaml
# application.yml (default)
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
    
app:
  services:
    account-service:
      url: ${ACCOUNT_SERVICE_URL:http://localhost:8088}
    product-service:
      url: ${PRODUCT_SERVICE_URL:http://localhost:8089}

---
# application-dev.yml (development)
spring:
  profiles: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/account-service
    username: postgres
    password: 123456

---
# application-docker.yml (container)
spring:
  profiles: docker
  datasource:
    url: ${DB_URL}
    username: postgres
    password: 123456
```

**External Configuration:**
```bash
# Environment variables override application properties
export SPRING_PROFILES_ACTIVE=docker
export DB_URL=jdbc:postgresql://account-service-db:5432/account-service
export KAFKA_BOOTSTRAP_SERVERS=kafka:29092
```

## 📈 API Documentation (Swagger)

### Access Swagger UI

**Account Service API Documentation:**
- **URL:** http://localhost:8088/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8088/v3/api-docs

**Product Service API Documentation:**
- **URL:** http://localhost:8089/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8089/v3/api-docs

### Swagger Configuration

**Implementation:**
```java
@Configuration
@EnableWebSecurity
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Account Service API")
                .version("1.0")
                .description("Microservices Account Management API"))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

**Features:**
- **Interactive API testing** directly in browser
- **JWT authentication** support in Swagger UI
- **Request/response examples** for all endpoints
- **Schema documentation** for all DTOs
- **Try it out** functionality for immediate testing

## 🎯 Design Patterns Implemented

### 1. Microservices Pattern
- **Single Responsibility:** Each service has one business domain
- **Independent Deployment:** Services can be deployed separately
- **Technology Diversity:** Different tech stacks per service (if needed)
- **Fault Isolation:** Failures don't cascade across services

### 2. Database Per Service
- **Data Ownership:** Each service owns its data
- **Schema Independence:** Services can evolve schemas independently
- **Technology Choice:** Different databases per service if needed
- **Scalability:** Scale databases based on service needs

### 3. API Gateway Pattern (Future Enhancement)
- **Single Entry Point:** Centralized client access
- **Cross-cutting Concerns:** Authentication, logging, rate limiting
- **Request Routing:** Route requests to appropriate services
- **Response Aggregation:** Combine responses from multiple services

### 4. Event Sourcing (Partial Implementation)
- **Domain Events:** Business events are captured and stored
- **Event-driven Communication:** Services communicate via events
- **Audit Trail:** Complete history of all changes
- **Eventual Consistency:** Data consistency through events

### 5. CQRS (Command Query Responsibility Segregation) - Future Enhancement
- **Not Currently Implemented:** This pattern is planned for future versions
- **Potential Implementation:** Separate read/write models for product search
- **Benefits:** Would allow optimized queries and independent scaling

### 6. Circuit Breaker (Future Enhancement)
- **Fault Tolerance:** Prevent cascading failures
- **Graceful Degradation:** Fallback responses when services are down
- **Automatic Recovery:** Automatically retry when services recover

## 🔮 Future Architecture Enhancements

### 1. API Gateway (Spring Cloud Gateway)
```yaml
# Future implementation
spring:
  cloud:
    gateway:
      routes:
        - id: account-service
          uri: http://account-service:8088
          predicates:
            - Path=/api/auth/**, /api/user/**
        - id: product-service
          uri: http://product-service:8089
          predicates:
            - Path=/api/product/**
```

### 2. Service Discovery (Eureka/Consul)
```java
// Future implementation
@EnableEurekaClient
@SpringBootApplication
public class AccountServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
```

### 3. Configuration Server (Spring Cloud Config)
```yaml
# Future implementation
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/company/microservices-config
```

### 4. Distributed Caching (Redis)
```java
// Future implementation
@Cacheable(value = "users", key = "#userId")
public UserProfile getUserById(Long userId) {
    return userRepository.findById(userId);
}
```

### 5. Metrics and Monitoring (Prometheus + Grafana)
```yaml
# Future implementation
management:
  endpoints:
    web:
      exposure:
        include: "*"
  metrics:
    export:
      prometheus:
        enabled: true
```

---

This architecture provides a solid foundation for learning microservices patterns while maintaining simplicity for educational purposes. Each component is designed to demonstrate real-world patterns and best practices.

**Next:** [03-API-Reference.md](03-API-Reference.md) for detailed API documentation