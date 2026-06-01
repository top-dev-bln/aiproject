# 07 - Docker Operations Guide

🐳 **Complete Docker container management for microservices development**

## 🚀 Essential Commands

### First Time Setup
```bash
# Build Maven artifacts
mvn clean install -DskipTests

# Build Docker images and start all services
docker compose up -d --build
```

### Daily Development Workflow
```bash
# After changing commons module (affects all services)
mvn clean install -DskipTests
docker compose up -d --build --force-recreate

# After changing account-service only
docker compose up -d --build --force-recreate account-service-app

# After changing product-service only
docker compose up -d --build --force-recreate product-service-app
```

## 📊 Service Management

### Check Service Status
```bash
# View all services status
docker compose ps

# Expected output:
# NAME                  STATUS
# account-service-app   Up (healthy)
# product-service-app   Up (healthy)
# kafka                 Up (healthy)
# elasticsearch         Up (healthy)
# ...
```

### Start/Stop Services
```bash
# Start all services
docker compose up -d

# Start specific services
docker compose up -d account-service-app product-service-app

# Stop all services (keeps data)
docker compose down

# Stop and remove all data
docker compose down -v

# Stop specific service
docker compose stop account-service-app

# Restart specific service
docker compose restart account-service-app
```

### Service Dependencies
```bash
# Start infrastructure first, then applications
docker compose up -d zookeeper kafka elasticsearch kibana fluentd
docker compose up -d account-service-db product-service-db
docker compose up -d account-service-app product-service-app

# Or start everything at once (Docker handles dependencies)
docker compose up -d
```

## 🔍 Monitoring & Logs

### View Logs
```bash
# All services logs
docker compose logs -f

# Specific services
docker compose logs -f account-service-app product-service-app

# Last 100 lines
docker compose logs --tail=100 account-service-app

# Filter by timestamp
docker compose logs --since="2024-01-01T12:00:00" account-service-app

# Follow logs with timestamps
docker compose logs -f -t account-service-app
```

### Container Inspection
```bash
# Container details
docker inspect account-service-app

# Container resource usage
docker stats account-service-app product-service-app

# Container processes
docker exec account-service-app ps aux

# Container network info
docker exec account-service-app netstat -an
```

### Health Checks
```bash
# Check health status
docker compose ps

# Manual health check
curl http://localhost:8088/actuator/health
curl http://localhost:8089/actuator/health

# Container health check logs
docker inspect account-service-app | grep -A 10 "Health"
```

## 🔧 Development Operations

### Code Changes Workflow

#### Commons Module Changes
```bash
# 1. Build commons and install to local Maven repo
mvn clean install -DskipTests

# 2. Rebuild all services that depend on commons
docker compose build account-service-app product-service-app

# 3. Recreate containers with new images
docker compose up -d --force-recreate account-service-app product-service-app
```

#### Single Service Changes
```bash
# For account-service changes
mvn -pl account-service -am clean install -DskipTests
docker compose build account-service-app
docker compose up -d --force-recreate account-service-app

# For product-service changes
mvn -pl product-service -am clean install -DskipTests
docker compose build product-service-app
docker compose up -d --force-recreate product-service-app
```

#### Quick Rebuild (Docker only)
```bash
# If only Java code changed (no new dependencies)
docker compose up -d --build --force-recreate account-service-app
```

### Database Operations
```bash
# Connect to databases
docker exec -it account-db psql -U postgres -d account-service
docker exec -it product-db psql -U postgres -d product-service

# Database backup
docker exec account-db pg_dump -U postgres account-service > account_backup.sql

# Database restore
docker exec -i account-db psql -U postgres account-service < account_backup.sql

# View database logs
docker compose logs account-service-db product-service-db
```

### Kafka Operations
```bash
# View Kafka logs
docker compose logs kafka

# List Kafka topics
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Create topic manually
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --create --topic test-topic --partitions 3 --replication-factor 1

# Consume messages from topic
docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic user-events --from-beginning

# Produce test message
docker exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic user-events
```

## 🧹 Cleanup Operations

### Clean Restart
```bash
# Complete clean restart
docker compose down -v
docker system prune -f
mvn clean install -DskipTests
docker compose up -d --build
```

### Selective Cleanup
```bash
# Remove only containers (keep images and volumes)
docker compose down

# Remove containers and volumes (keep images)
docker compose down -v

# Remove everything including images
docker compose down -v --rmi all

# Clean unused Docker resources
docker system prune -f

# Clean everything (be careful!)
docker system prune -af --volumes
```

### Disk Space Management
```bash
# Check Docker disk usage
docker system df

# Remove unused images
docker image prune -f

# Remove unused containers
docker container prune -f

# Remove unused volumes
docker volume prune -f

# Remove unused networks
docker network prune -f
```

## 🔨 Build Operations

### Image Management
```bash
# Build all images
docker compose build

# Build specific image
docker compose build account-service-app

# Build without cache (clean build)
docker compose build --no-cache account-service-app

# Pull latest base images
docker compose pull

# List images
docker images | grep -E "(account-service|product-service)"
```

### Multi-stage Build Optimization
```bash
# Build with specific target (for debugging)
docker build --target build -t account-service-debug ./account-service

# Build with build arguments
docker build --build-arg MAVEN_OPTS="-Xmx1024m" ./account-service
```

## 🌐 Network Operations

### Network Inspection
```bash
# List networks
docker network ls

# Inspect network
docker network inspect spring-boot-micro_default

# Check service connectivity
docker exec account-service-app ping product-service-app
docker exec product-service-app ping kafka
```

### Port Management
```bash
# Check port mappings
docker port account-service-app
docker port product-service-app

# Check if ports are in use
netstat -an | findstr :8088
netstat -an | findstr :8089

# Test connectivity
curl http://localhost:8088/actuator/health
curl http://localhost:8089/actuator/health
```

## 🔧 Troubleshooting

### Common Issues

#### Services Won't Start
```bash
# Check logs for errors
docker compose logs account-service-app

# Check dependencies
docker compose ps

# Verify ports aren't in use
netstat -an | findstr :8088

# Check disk space
docker system df
df -h
```

#### Build Failures
```bash
# Clean Maven cache
mvn dependency:purge-local-repository

# Clean Docker build cache
docker builder prune -f

# Rebuild from scratch
docker compose build --no-cache
```

#### Memory Issues
```bash
# Check container memory usage
docker stats

# Increase Docker memory limit (Docker Desktop)
# Settings > Resources > Memory > Increase to 8GB+

# Check system memory
free -h  # Linux
wmic OS get TotalVisibleMemorySize,FreePhysicalMemory  # Windows
```

#### Network Issues
```bash
# Reset Docker networks
docker compose down
docker network prune -f
docker compose up -d

# Check DNS resolution
docker exec account-service-app nslookup product-service-app
```

### Debug Container Issues
```bash
# Enter container shell
docker exec -it account-service-app sh

# Check container environment
docker exec account-service-app env

# Check container processes
docker exec account-service-app ps aux

# Check container files
docker exec account-service-app ls -la /app

# Check Java process
docker exec account-service-app jps -v
```

## 📈 Performance Optimization

### Resource Limits
```yaml
# docker-compose.yml
services:
  account-service-app:
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '1.0'
        reservations:
          memory: 512M
          cpus: '0.5'
```

### JVM Optimization
```dockerfile
# Dockerfile
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-jar", "/app/app.jar"]
```

### Build Performance
```bash
# Parallel builds
mvn -T 4 clean install -DskipTests

# Use BuildKit for faster Docker builds
export DOCKER_BUILDKIT=1
docker compose build
```

## 🔒 Security Operations

### Container Security
```bash
# Run containers as non-root user (already configured)
docker exec account-service-app whoami  # Should return 'appuser'

# Check for security vulnerabilities
docker scout cves account-service-app

# Scan images for vulnerabilities
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image account-service-app
```

### Network Security
```bash
# Check exposed ports
docker compose ps

# Verify only necessary ports are exposed
netstat -an | grep LISTEN
```

## 📋 Production Considerations

### Environment Variables
```bash
# Set production environment
export SPRING_PROFILES_ACTIVE=prod
export DB_PASSWORD=secure_password
export JWT_SECRET=secure_jwt_secret

# Use .env file for Docker Compose
echo "SPRING_PROFILES_ACTIVE=prod" > .env
echo "DB_PASSWORD=secure_password" >> .env
```

### Health Monitoring
```bash
# Set up health check monitoring
while true; do
  curl -f http://localhost:8088/actuator/health || echo "Account service down"
  curl -f http://localhost:8089/actuator/health || echo "Product service down"
  sleep 30
done
```

### Backup Strategy
```bash
# Database backups
docker exec account-db pg_dump -U postgres account-service | gzip > account_backup_$(date +%Y%m%d).sql.gz

# Configuration backups
cp docker-compose.yml docker-compose.yml.bak
cp -r fluentd/ fluentd.bak/
```

## 🚀 Advanced Operations

### Multi-Environment Setup
```bash
# Development environment
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# Production environment
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Testing environment
docker compose -f docker-compose.yml -f docker-compose.test.yml up -d
```

### Service Scaling
```bash
# Scale specific service
docker compose up -d --scale product-service-app=3

# Load balancer would be needed for multiple instances
```

### Container Updates
```bash
# Rolling update strategy
docker compose pull
docker compose up -d --no-deps account-service-app
docker compose up -d --no-deps product-service-app
```

---

**Master Docker operations! 🐳**

Next: [08-Monitoring-Logging.md](08-Monitoring-Logging.md) for observability setup