# Configuration Reference

Complete reference for all configuration options in the microservices system.

## 📋 Table of Contents
- [Environment Variables](#environment-variables)
- [Application Properties](#application-properties)
- [Docker Configuration](#docker-configuration)
- [Database Configuration](#database-configuration)
- [Kafka Configuration](#kafka-configuration)
- [Security Configuration](#security-configuration)
- [Logging Configuration](#logging-configuration)
- [Monitoring Configuration](#monitoring-configuration)

## 🌍 Environment Variables

### Account Service
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://account-db:5432/account_db
SPRING_DATASOURCE_USERNAME=account_user
SPRING_DATASOURCE_PASSWORD=account_password

# JWT Security
JWT_SECRET=mySecretKey
JWT_EXPIRATION_MS=86400000

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Logging
LOGGING_LEVEL_COM_MICROSERVICES=DEBUG
FLUENTD_HOST=fluentd
FLUENTD_PORT=24224
```

### Product Service
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://product-db:5434/product_db
SPRING_DATASOURCE_USERNAME=product_user
SPRING_DATASOURCE_PASSWORD=product_password

# Account Service Integration
ACCOUNT_SERVICE_URL=http://account-service-app:8088

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Logging
LOGGING_LEVEL_COM_MICROSERVICES=DEBUG
FLUENTD_HOST=fluentd
FLUENTD_PORT=24224
```

## ⚙️ Application Properties

### Account Service (application.yml)
```yaml
server:
  port: 8088

spring:
  application:
    name: account-service
  
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/account_db}
    username: ${SPRING_DATASOURCE_USERNAME:account_user}
    password: ${SPRING_DATASOURCE_PASSWORD:account_password}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: account-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"

# JWT Configuration
app:
  jwtSecret: ${JWT_SECRET:mySecretKey}
  jwtExpirationMs: ${JWT_EXPIRATION_MS:86400000}

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

# Logging
logging:
  level:
    com.microservices: ${LOGGING_LEVEL_COM_MICROSERVICES:INFO}
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

### Product Service (application.yml)
```yaml
server:
  port: 8089

spring:
  application:
    name: product-service
  
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5434/product_db}
    username: ${SPRING_DATASOURCE_USERNAME:product_user}
    password: ${SPRING_DATASOURCE_PASSWORD:product_password}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: product-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"

# External Services
external:
  account-service:
    url: ${ACCOUNT_SERVICE_URL:http://localhost:8088}

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

# Logging
logging:
  level:
    com.microservices: ${LOGGING_LEVEL_COM_MICROSERVICES:INFO}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

## 🐳 Docker Configuration

### Docker Compose Environment Variables
```yaml
# Account Service
account-service-app:
  environment:
    - SPRING_DATASOURCE_URL=jdbc:postgresql://account-db:5432/account_db
    - SPRING_DATASOURCE_USERNAME=account_user
    - SPRING_DATASOURCE_PASSWORD=account_password
    - JWT_SECRET=mySecretKey
    - JWT_EXPIRATION_MS=86400000
    - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    - LOGGING_LEVEL_COM_MICROSERVICES=DEBUG
    - FLUENTD_HOST=fluentd
    - FLUENTD_PORT=24224

# Product Service
product-service-app:
  environment:
    - SPRING_DATASOURCE_URL=jdbc:postgresql://product-db:5434/product_db
    - SPRING_DATASOURCE_USERNAME=product_user
    - SPRING_DATASOURCE_PASSWORD=product_password
    - ACCOUNT_SERVICE_URL=http://account-service-app:8088
    - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    - LOGGING_LEVEL_COM_MICROSERVICES=DEBUG
    - FLUENTD_HOST=fluentd
    - FLUENTD_PORT=24224
```

### Container Resource Limits
```yaml
# Memory and CPU limits
deploy:
  resources:
    limits:
      memory: 1G
      cpus: '1.0'
    reservations:
      memory: 512M
      cpus: '0.5'
```

## 🗄️ Database Configuration

### PostgreSQL Settings
```yaml
# Account Database
account-db:
  environment:
    POSTGRES_DB: account_db
    POSTGRES_USER: account_user
    POSTGRES_PASSWORD: account_password
    POSTGRES_INITDB_ARGS: "--encoding=UTF-8"
  command: >
    postgres -c log_statement=all
             -c log_destination=stderr
             -c log_min_duration_statement=0
             -c max_connections=200
             -c shared_buffers=256MB

# Product Database  
product-db:
  environment:
    POSTGRES_DB: product_db
    POSTGRES_USER: product_user
    POSTGRES_PASSWORD: product_password
    POSTGRES_INITDB_ARGS: "--encoding=UTF-8"
```

### Connection Pool Settings
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
```

## 📨 Kafka Configuration

### Kafka Broker Settings
```yaml
kafka:
  environment:
    KAFKA_BROKER_ID: 1
    KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
    KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
    KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'
    KAFKA_NUM_PARTITIONS: 3
    KAFKA_DEFAULT_REPLICATION_FACTOR: 1
```

### Producer Configuration
```yaml
spring:
  kafka:
    producer:
      acks: all
      retries: 3
      batch-size: 16384
      linger-ms: 1
      buffer-memory: 33554432
      compression-type: snappy
```

### Consumer Configuration
```yaml
spring:
  kafka:
    consumer:
      auto-offset-reset: earliest
      enable-auto-commit: true
      auto-commit-interval: 1000
      session-timeout-ms: 30000
      max-poll-records: 500
```

## 🔐 Security Configuration

### JWT Settings
```yaml
app:
  jwtSecret: mySecretKey  # Change in production!
  jwtExpirationMs: 86400000  # 24 hours
  
# Security headers
security:
  headers:
    frame-options: DENY
    content-type-options: nosniff
    xss-protection: "1; mode=block"
```

### CORS Configuration
```java
@CrossOrigin(origins = "*", maxAge = 3600)
// Or configure globally:
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}
```

## 📝 Logging Configuration

### Fluentd Configuration
```ruby
# fluentd/conf/fluent.conf
<source>
  @type forward
  port 24224
  bind 0.0.0.0
</source>

<match **>
  @type elasticsearch
  host elasticsearch
  port 9200
  logstash_format true
  logstash_prefix microservices
  logstash_dateformat %Y.%m.%d
  include_tag_key true
  type_name access_log
  tag_key @log_name
  flush_interval 1s
</match>
```

### Logback Configuration
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FLUENTD" class="ch.qos.logback.more.appenders.FluentdAppender">
        <remoteHost>${FLUENTD_HOST:-localhost}</remoteHost>
        <port>${FLUENTD_PORT:-24224}</port>
        <tag>microservices</tag>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="FLUENTD"/>
    </root>
</configuration>
```

## 📊 Monitoring Configuration

### Actuator Endpoints
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      show-components: always
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

### Health Check Configuration
```yaml
# Docker health checks
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8088/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 40s
```

### ELK Stack Configuration
```yaml
# Elasticsearch
elasticsearch:
  environment:
    - discovery.type=single-node
    - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    - xpack.security.enabled=true
    - ELASTIC_PASSWORD=elastic

# Kibana
kibana:
  environment:
    - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    - ELASTICSEARCH_USERNAME=elastic
    - ELASTICSEARCH_PASSWORD=elastic
```

## 🔧 Development Configuration

### IDE Configuration
```yaml
# application-dev.yml
spring:
  profiles:
    active: dev
  
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
  kafka:
    bootstrap-servers: localhost:9092

logging:
  level:
    com.microservices: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
```

### Debug Configuration
```yaml
# JVM debug options in Dockerfile
ENV JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

## 🚀 Production Configuration

### Production Overrides
```yaml
# application-prod.yml
spring:
  jpa:
    show-sql: false
  
  kafka:
    producer:
      acks: all
      retries: 10
    consumer:
      enable-auto-commit: false

logging:
  level:
    com.microservices: INFO
    org.springframework.web: WARN

# Security
app:
  jwtSecret: ${JWT_SECRET}  # Must be set via environment
  jwtExpirationMs: 3600000  # 1 hour in production
```

### Performance Tuning
```yaml
# JVM Options
JAVA_OPTS: >
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+UseContainerSupport
  -XX:MaxRAMPercentage=75.0
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/tmp/heapdump.hprof
```

## 📚 Configuration Best Practices

### 1. Environment-Specific Configuration
- Use Spring profiles (`dev`, `test`, `prod`)
- Override via environment variables
- Never commit secrets to version control

### 2. Security Configuration
- Use strong JWT secrets (256-bit minimum)
- Enable HTTPS in production
- Configure proper CORS policies
- Use database connection encryption

### 3. Performance Configuration
- Tune connection pool sizes
- Configure appropriate timeouts
- Enable compression for Kafka
- Use appropriate JVM settings

### 4. Monitoring Configuration
- Enable all necessary actuator endpoints
- Configure proper log levels
- Set up health checks
- Monitor resource usage

## 🔍 Configuration Validation

### Startup Validation Script
```bash
#!/bin/bash
# validate-config.sh

echo "Validating configuration..."

# Check required environment variables
required_vars=("JWT_SECRET" "SPRING_DATASOURCE_PASSWORD")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "ERROR: $var is not set"
        exit 1
    fi
done

# Check service connectivity
curl -f http://localhost:8088/actuator/health || exit 1
curl -f http://localhost:8089/actuator/health || exit 1

echo "Configuration validation passed!"
```

### Configuration Testing
```bash
# Test different profiles
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn spring-boot:run -Dspring-boot.run.profiles=test
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## 📞 Need Help?

- **Configuration Issues**: Check [09-Troubleshooting.md](09-Troubleshooting.md)
- **Development Setup**: See [04-Development-Guide.md](04-Development-Guide.md)
- **Docker Problems**: Review [07-Docker-Operations.md](07-Docker-Operations.md)

**Remember**: Always validate configuration changes in a development environment first!