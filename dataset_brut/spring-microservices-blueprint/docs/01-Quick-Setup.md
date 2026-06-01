# 01 - Quick Setup Guide

🚀 **Get the Spring Microservices Blueprint running in 5 minutes!**

## ⚡ One-Command Setup

**Build and start everything:**
```bash
# Step 1: Build Maven artifacts
mvn clean install -DskipTests

# Step 2: Build Docker images and start all services
docker compose up -d --build
```

> **Why both commands?** Maven builds the JAR files, Docker builds the container images with those JARs.

## ⏱️ Wait for Services (30-60 seconds)

**Check service status:**
```bash
docker compose ps
```

**Expected output - all services should show `Up` and `(healthy)`:**
```
NAME                  STATUS
account-service-app   Up (healthy)
product-service-app   Up (healthy)
kafka                 Up (healthy)
elasticsearch         Up (healthy)
zookeeper             Up (healthy)
account-db            Up (healthy)
product-db            Up (healthy)
kibana                Up
fluentd               Up
kafdrop               Up (healthy)
```

## ✅ Verify Everything Works

### Test 1: Account Service
```bash
curl http://localhost:8088/api/test/all
```
**Expected:** `"Public Content."`

### Test 2: Product Service
```bash
curl http://localhost:8089/api/product/search
```
**Expected:** JSON response with products

### Test 3: Health Checks
```bash
curl http://localhost:8088/actuator/health
curl http://localhost:8089/actuator/health
```
**Expected:** `{"status":"UP"}`

## 🌐 Access All Services

Once everything is running:

| Service | URL | Login | Purpose |
|---------|-----|-------|---------|
| **Account Service API** | http://localhost:8088 | - | User management & auth |
| **Product Service API** | http://localhost:8089 | - | Product operations |
| **Kibana (Logs)** | http://localhost:5601 | elastic/elastic | View application logs |
| **Kafdrop (Kafka UI)** | http://localhost:8085 | - | Monitor Kafka messages |
| **Elasticsearch** | http://localhost:9200 | elastic/elastic | Search engine |

## 🎮 Quick Demo

### Register and Login
```bash
# Register a new user
curl -X POST http://localhost:8088/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "email": "test@example.com", "password": "password123"}'

# Login to get JWT token
curl -X POST http://localhost:8088/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
```

### Search Products
```bash
# Search all products (no authentication needed)
curl "http://localhost:8089/api/product/search"

# Search with filters
curl "http://localhost:8089/api/product/search?name=Sample&page=0&size=5"
```

### View Logs
1. Open http://localhost:5601
2. Login with `elastic/elastic`
3. Create index pattern: `fluentd-*`
4. Go to Discover to view logs

## 🔧 Troubleshooting Setup

### Issue: Services not starting
```bash
# Check what's running
docker compose ps

# View logs for failed services
docker compose logs account-service-app
docker compose logs product-service-app
```

### Issue: Port conflicts
```bash
# Check if ports are in use
netstat -an | findstr :8088
netstat -an | findstr :8089

# Kill processes using the ports or change ports in docker-compose.yml
```

### Issue: Build failures
```bash
# Clean Maven cache
mvn clean

# Rebuild everything
mvn clean install -DskipTests

# Clean Docker and rebuild
docker compose down -v
docker system prune -f
docker compose up -d --build
```

### Issue: Out of disk space
```bash
# Clean up Docker resources
docker system prune -af --volumes

# This will free up space used by unused containers, images, and volumes
```

### Issue: Services show "unhealthy"
```bash
# Wait longer - services need time to start
sleep 60
docker compose ps

# Check specific service logs
docker compose logs -f account-service-app
```

## 🛑 Stop Everything

```bash
# Stop all services (keeps data)
docker compose down

# Stop and remove all data (clean slate)
docker compose down -v
```

## 🔄 Daily Development Commands

### After changing code:
```bash
# For commons module changes (affects all services)
mvn clean install -DskipTests
docker compose up -d --build --force-recreate

# For account-service changes only
docker compose up -d --build --force-recreate account-service-app

# For product-service changes only
docker compose up -d --build --force-recreate product-service-app
```

### View logs:
```bash
# All services
docker compose logs -f

# Specific services
docker compose logs -f account-service-app product-service-app

# Last 100 lines
docker compose logs --tail=100 account-service-app
```

## 📊 System Resource Usage

**Typical resource usage:**
- **RAM:** ~6-8GB total
- **CPU:** Moderate during startup, low during idle
- **Disk:** ~2-3GB for images and data
- **Network:** Local ports 5432, 5434, 5601, 8085, 8088, 8089, 9092, 9200, 24224

**If you have limited resources:**
```bash
# Start only essential services
docker compose up -d account-service-db product-service-db kafka zookeeper
docker compose up -d account-service-app product-service-app

# Add monitoring later
docker compose up -d elasticsearch kibana fluentd kafdrop
```

## 🎯 Next Steps

Now that everything is running:

1. **Try the APIs** → [03-API-Reference.md](03-API-Reference.md)
2. **Understand the system** → [02-System-Architecture.md](02-System-Architecture.md)
3. **Import Postman collection** → [10-Postman-Collection.md](10-Postman-Collection.md)
4. **Start developing** → [04-Development-Guide.md](04-Development-Guide.md)

## 🆘 Need Help?

- **Common issues:** [09-Troubleshooting.md](09-Troubleshooting.md)
- **Configuration:** [11-Configuration-Reference.md](11-Configuration-Reference.md)
- **Docker operations:** [07-Docker-Operations.md](07-Docker-Operations.md)

---

**Setup complete! 🎉 Ready to explore microservices!**