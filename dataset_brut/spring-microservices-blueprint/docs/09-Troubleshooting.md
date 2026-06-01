# 09 - Troubleshooting Guide

🚨 **Common issues and solutions for microservices development**

## 🚀 Quick Fixes

### Services Won't Start
```bash
# Check if ports are in use
netstat -an | findstr :8088
netstat -an | findstr :8089

# Clean restart everything
docker compose down -v
docker system prune -f
mvn clean install -DskipTests
docker compose up -d --build
```

### Build Failures
```bash
# Clear Maven cache
mvn dependency:purge-local-repository

# Rebuild everything
mvn clean install -DskipTests
```

### Out of Memory
```bash
# Clean Docker resources
docker system prune -af --volumes

# Increase Docker memory limit to 8GB+
```

## 🔧 Startup Issues

### Issue: Services Not Starting

#### Symptoms
- Containers exit immediately
- Health checks fail
- Connection refused errors

#### Diagnosis
```bash
# Check container status
docker compose ps

# View container logs
docker compose logs account-service-app
docker compose logs product-service-app

# Check resource usage
docker stats
```

#### Solutions

**1. Port Conflicts**
```bash
# Check what's using the ports
netstat -an | findstr :8088
netstat -an | findstr :8089
netstat -an | findstr :5432

# Kill processes using the ports
taskkill /PID <process_id> /F

# Or change ports in docker-compose.yml
```

**2. Insufficient Resources**
```bash
# Check available memory
wmic OS get TotalVisibleMemorySize,FreePhysicalMemory

# Increase Docker Desktop memory limit
# Docker Desktop → Settings → Resources → Memory → 8GB+

# Check disk space
docker system df
```

**3. Database Connection Issues**
```bash
# Check database container
docker compose logs account-service-db product-service-db

# Verify database is ready
docker exec account-db pg_isready -U postgres

# Check connection from service
docker exec account-service-app nc -z account-service-db 5432
```

### Issue: Services Start But Show Unhealthy

#### Symptoms
- Containers running but health checks fail
- HTTP 500 errors on API calls
- Services can't connect to dependencies

#### Diagnosis
```bash
# Check health status
curl http://localhost:8088/actuator/health
curl http://localhost:8089/actuator/health

# Check detailed health
curl http://localhost:8088/actuator/health/db
curl http://localhost:8089/actuator/health/kafka
```

#### Solutions

**1. Database Not Ready**
```bash
# Wait for database to be fully ready
sleep 30

# Check database logs
docker compose logs account-service-db

# Restart service after database is ready
docker compose restart account-service-app
```

**2. Kafka Not Ready**
```bash
# Check Kafka and Zookeeper
docker compose logs kafka zookeeper

# Verify Kafka topics
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Restart services after Kafka is ready
docker compose restart account-service-app product-service-app
```

**3. Service Dependencies**
```bash
# Check service startup order
docker compose up -d zookeeper
sleep 10
docker compose up -d kafka
sleep 20
docker compose up -d account-service-db product-service-db
sleep 10
docker compose up -d account-service-app product-service-app
```

## 🏗️ Build Issues

### Issue: Maven Build Failures

#### Symptoms
- Compilation errors
- Dependency resolution failures
- Test failures

#### Diagnosis
```bash
# Run build with verbose output
mvn clean install -X

# Check for specific errors
mvn clean compile
mvn dependency:tree
```

#### Solutions

**1. Dependency Issues**
```bash
# Clear local Maven repository
rm -rf ~/.m2/repository

# Or purge specific dependencies
mvn dependency:purge-local-repository

# Rebuild
mvn clean install -DskipTests
```

**2. Java Version Issues**
```bash
# Check Java version
java -version
mvn -version

# Ensure Java 17+ is being used
export JAVA_HOME=/path/to/java17
```

**3. Commons Module Issues**
```bash
# Build commons first
cd commons
mvn clean install -DskipTests

# Then build other modules
cd ..
mvn clean install -DskipTests
```

### Issue: Docker Build Failures

#### Symptoms
- Docker build fails
- Image build timeouts
- Layer caching issues

#### Diagnosis
```bash
# Build with verbose output
docker compose build --progress=plain

# Check Docker daemon
docker info

# Check available space
docker system df
```

#### Solutions

**1. Build Cache Issues**
```bash
# Clear build cache
docker builder prune -f

# Build without cache
docker compose build --no-cache

# Clean everything and rebuild
docker system prune -af
mvn clean install -DskipTests
docker compose build
```

**2. Network Issues During Build**
```bash
# Check network connectivity
ping maven.apache.org

# Use different Maven mirror
# Edit pom.xml or settings.xml

# Build with different network
docker compose build --network=host
```

**3. Resource Limits**
```bash
# Increase Docker build memory
# Docker Desktop → Settings → Resources → Memory

# Use multi-stage build optimization
# Already implemented in Dockerfiles
```

## 🌐 Network Issues

### Issue: Service Communication Failures

#### Symptoms
- Feign client errors
- Connection timeouts
- Service discovery failures

#### Diagnosis
```bash
# Test service connectivity
docker exec account-service-app ping product-service-app
docker exec product-service-app ping account-service-app

# Check network configuration
docker network ls
docker network inspect spring-boot-micro_default

# Test API endpoints
curl http://localhost:8088/api/test/all
curl http://localhost:8089/api/product/search
```

#### Solutions

**1. DNS Resolution Issues**
```bash
# Check container DNS
docker exec account-service-app nslookup product-service-app

# Restart Docker daemon
# Docker Desktop → Restart

# Recreate network
docker compose down
docker network prune -f
docker compose up -d
```

**2. Firewall/Antivirus Issues**
```bash
# Temporarily disable firewall/antivirus
# Test if services work

# Add Docker to firewall exceptions
# Configure antivirus to exclude Docker directories
```

**3. Port Binding Issues**
```bash
# Check port bindings
docker port account-service-app
docker port product-service-app

# Verify ports are accessible
telnet localhost 8088
telnet localhost 8089
```

### Issue: External Access Problems

#### Symptoms
- Can't access services from host
- Swagger UI not loading
- API calls from Postman fail

#### Diagnosis
```bash
# Check if services are listening
netstat -an | findstr :8088
netstat -an | findstr :8089

# Test from inside container
docker exec account-service-app curl http://localhost:8088/actuator/health

# Check Docker port mapping
docker compose ps
```

#### Solutions

**1. Port Mapping Issues**
```bash
# Verify docker-compose.yml port mappings
# Should have:
# ports:
#   - "8088:8088"
#   - "8089:8089"

# Restart with correct ports
docker compose down
docker compose up -d
```

**2. Service Binding Issues**
```bash
# Check if service binds to 0.0.0.0
# Should not bind only to localhost/127.0.0.1

# Verify in application.yml:
# server:
#   address: 0.0.0.0  # Not 127.0.0.1
```

## 💾 Database Issues

### Issue: Database Connection Failures

#### Symptoms
- Database connection timeouts
- Authentication failures
- Connection pool exhausted

#### Diagnosis
```bash
# Check database container
docker compose logs account-service-db product-service-db

# Test database connectivity
docker exec account-db pg_isready -U postgres

# Check database connections
docker exec account-db psql -U postgres -d account-service -c "SELECT * FROM pg_stat_activity;"
```

#### Solutions

**1. Database Not Ready**
```bash
# Wait for database initialization
sleep 30

# Check initialization logs
docker compose logs account-service-db | grep "ready to accept connections"

# Restart service after database is ready
docker compose restart account-service-app
```

**2. Connection Pool Issues**
```bash
# Check connection pool configuration in application.yml
# spring:
#   datasource:
#     hikari:
#       maximum-pool-size: 20
#       minimum-idle: 5

# Monitor connection pool
curl http://localhost:8088/actuator/metrics/hikaricp.connections.active
```

**3. Database Credentials**
```bash
# Verify credentials in docker-compose.yml and application.yml
# Ensure they match:
# POSTGRES_USER: postgres
# POSTGRES_PASSWORD: 123456

# Test manual connection
docker exec -it account-db psql -U postgres -d account-service
```

### Issue: Database Schema Issues

#### Symptoms
- Table not found errors
- Column not found errors
- Data integrity violations

#### Diagnosis
```bash
# Connect to database
docker exec -it account-db psql -U postgres -d account-service

# Check tables
\dt

# Check table structure
\d users
\d roles
\d user_roles
```

#### Solutions

**1. Schema Not Created**
```bash
# Check if data.sql is being executed
docker compose logs account-service-app | grep "data.sql"

# Manually run schema creation
docker exec -it account-db psql -U postgres -d account-service -f /docker-entrypoint-initdb.d/data.sql
```

**2. Schema Mismatch**
```bash
# Drop and recreate database
docker compose down -v
docker compose up -d account-service-db
sleep 30
docker compose up -d account-service-app
```

## 📨 Kafka Issues

### Issue: Kafka Connection Failures

#### Symptoms
- Kafka producer/consumer errors
- Message publishing failures
- Consumer group issues

#### Diagnosis
```bash
# Check Kafka and Zookeeper
docker compose logs kafka zookeeper

# List topics
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Check consumer groups
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

#### Solutions

**1. Kafka Not Ready**
```bash
# Start Zookeeper first, then Kafka
docker compose up -d zookeeper
sleep 10
docker compose up -d kafka
sleep 20

# Verify Kafka is ready
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

**2. Topic Creation Issues**
```bash
# Manually create topics
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --create --topic user-events --partitions 3 --replication-factor 1
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --create --topic product-events --partitions 3 --replication-factor 1
```

**3. Consumer Group Issues**
```bash
# Reset consumer group
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group account-service-group --reset-offsets --to-earliest --all-topics --execute

# Check consumer lag
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group account-service-group
```

## 🔐 Authentication Issues

### Issue: JWT Token Problems

#### Symptoms
- 401 Unauthorized errors
- Token validation failures
- Login failures

#### Diagnosis
```bash
# Test login endpoint
curl -X POST http://localhost:8088/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username": "vito", "password": "123456"}'

# Check if default users exist
docker exec account-db psql -U postgres -d account-service -c "SELECT username FROM users;"
```

#### Solutions

**1. Default Users Not Created**
```bash
# Check data.sql execution
docker compose logs account-service-app | grep "data.sql"

# Manually create admin user
docker exec -it account-db psql -U postgres -d account-service

INSERT INTO users (username, email, password, enabled) 
VALUES ('vito', 'vito@example.com', '$2a$10$encrypted_password', true);
```

**2. Token Expiration**
```bash
# Check token expiration in application.yml
# app:
#   jwtExpirationMs: 86400000  # 24 hours

# Get new token by logging in again
```

**3. JWT Secret Issues**
```bash
# Check JWT secret configuration
# app:
#   jwtSecret: mySecretKey

# Ensure same secret across all instances
```

## 📊 Monitoring Issues

### Issue: Kibana Not Accessible

#### Symptoms
- Kibana UI not loading
- Connection refused to port 5601
- Elasticsearch connection errors

#### Diagnosis
```bash
# Check Kibana container
docker compose logs kibana

# Check Elasticsearch health
curl -u elastic:elastic http://localhost:9200/_cluster/health

# Check port binding
docker port kibana
```

#### Solutions

**1. Elasticsearch Not Ready**
```bash
# Wait for Elasticsearch to be ready
sleep 60

# Check Elasticsearch status
curl -u elastic:elastic http://localhost:9200/_cluster/health

# Restart Kibana after Elasticsearch is ready
docker compose restart kibana
```

**2. Index Pattern Issues**
```bash
# Check if logs are being indexed
curl -u elastic:elastic http://localhost:9200/_cat/indices

# Create index pattern manually in Kibana
# Go to Stack Management → Index Patterns → Create
```

### Issue: No Logs in Kibana

#### Symptoms
- Empty Discover view
- No fluentd indices
- Services running but no logs

#### Diagnosis
```bash
# Check Fluentd logs
docker compose logs fluentd

# Check if services are generating logs
docker compose logs account-service-app | head -10

# Check Elasticsearch indices
curl -u elastic:elastic http://localhost:9200/_cat/indices
```

#### Solutions

**1. Fluentd Configuration Issues**
```bash
# Check Fluentd configuration
cat fluentd/conf/fluent.conf

# Restart Fluentd
docker compose restart fluentd

# Check Fluentd connectivity to Elasticsearch
docker exec fluentd nc -z elasticsearch 9200
```

**2. Log Driver Issues**
```bash
# Check Docker logging driver in docker-compose.yml
# logging:
#   driver: "fluentd"
#   options:
#     fluentd-address: host.docker.internal:24224

# Restart services with correct logging
docker compose up -d --force-recreate account-service-app product-service-app
```

## 🔄 Performance Issues

### Issue: Slow Response Times

#### Symptoms
- API calls taking too long
- Timeouts
- High CPU/memory usage

#### Diagnosis
```bash
# Check response times
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8088/api/test/all

# Monitor resource usage
docker stats account-service-app product-service-app

# Check JVM metrics
curl http://localhost:8088/actuator/metrics/jvm.memory.used
```

#### Solutions

**1. Resource Constraints**
```bash
# Increase Docker memory limit
# Docker Desktop → Settings → Resources → Memory → 8GB+

# Optimize JVM settings in Dockerfile
# -XX:MaxRAMPercentage=75.0
# -XX:+UseG1GC
```

**2. Database Performance**
```bash
# Check database connections
curl http://localhost:8088/actuator/metrics/hikaricp.connections.active

# Analyze slow queries
docker exec -it account-db psql -U postgres -d account-service
SELECT query, mean_time FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;
```

**3. Network Latency**
```bash
# Check inter-service communication
docker exec account-service-app ping product-service-app

# Optimize Feign client configuration
# feign:
#   client:
#     config:
#       default:
#         connectTimeout: 5000
#         readTimeout: 10000
```

## 🧹 Cleanup and Reset

### Complete Environment Reset
```bash
#!/bin/bash
echo "Resetting entire environment..."

# Stop everything
docker compose down -v

# Clean Docker resources
docker system prune -af --volumes

# Clean Maven
mvn clean
rm -rf ~/.m2/repository/spring/boot/microservice

# Rebuild everything
mvn clean install -DskipTests
docker compose build --no-cache
docker compose up -d

echo "Environment reset complete"
```

### Selective Reset
```bash
# Reset only databases
docker compose down account-service-db product-service-db
docker volume rm spring-boot-micro_account-postgres-data spring-boot-micro_product-postgres-data
docker compose up -d account-service-db product-service-db

# Reset only services
docker compose down account-service-app product-service-app
docker compose build account-service-app product-service-app
docker compose up -d account-service-app product-service-app
```

## 📞 Getting Help

### Diagnostic Information Collection
```bash
#!/bin/bash
echo "=== Diagnostic Information ===" > diagnostic.txt
echo "Date: $(date)" >> diagnostic.txt
echo "" >> diagnostic.txt

echo "=== Docker Version ===" >> diagnostic.txt
docker --version >> diagnostic.txt
docker compose version >> diagnostic.txt
echo "" >> diagnostic.txt

echo "=== Java Version ===" >> diagnostic.txt
java -version >> diagnostic.txt 2>&1
mvn -version >> diagnostic.txt
echo "" >> diagnostic.txt

echo "=== Container Status ===" >> diagnostic.txt
docker compose ps >> diagnostic.txt
echo "" >> diagnostic.txt

echo "=== Resource Usage ===" >> diagnostic.txt
docker stats --no-stream >> diagnostic.txt
echo "" >> diagnostic.txt

echo "=== Recent Logs ===" >> diagnostic.txt
docker compose logs --tail=50 account-service-app >> diagnostic.txt
docker compose logs --tail=50 product-service-app >> diagnostic.txt

echo "Diagnostic information saved to diagnostic.txt"
```

### Common Log Patterns to Look For

**Successful Startup:**
```
Started AccountServiceApplication in X.XXX seconds
Started ProductServiceApplication in X.XXX seconds
```

**Database Connection Success:**
```
HikariPool-1 - Start completed
```

**Kafka Connection Success:**
```
Kafka version: X.X.X
```

**Health Check Success:**
```
Health check passed
```

---

**Troubleshooting mastered! 🔧**

Next: [11-Configuration-Reference.md](11-Configuration-Reference.md) for complete configuration options