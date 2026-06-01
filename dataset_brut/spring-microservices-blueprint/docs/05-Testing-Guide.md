# 05 - Testing Guide

🧪 **Complete testing strategy for microservices system**

## 🚀 Quick Test

**Verify everything is working:**
```bash
# 1. Start services
mvn clean install -DskipTests
docker compose up -d --build

# 2. Wait for services to be ready (30-60 seconds)
docker compose ps

# 3. Quick health check
curl http://localhost:8088/api/test/all
curl http://localhost:8089/api/product/search
```

If both return data, you're ready to go! 🎉

## 🧪 Testing Levels

### 1. Smoke Tests (Basic Functionality)

**Purpose:** Verify services are up and responding

```bash
#!/bin/bash
echo "=== Smoke Tests ==="

# Test Account Service
echo "Testing Account Service..."
ACCOUNT_RESPONSE=$(curl -s http://localhost:8088/api/test/all)
if [ "$ACCOUNT_RESPONSE" = '"Public Content."' ]; then
    echo "✅ Account Service: OK"
else
    echo "❌ Account Service: FAILED"
    echo "Response: $ACCOUNT_RESPONSE"
fi

# Test Product Service
echo "Testing Product Service..."
PRODUCT_RESPONSE=$(curl -s http://localhost:8089/api/product/search)
if [[ $PRODUCT_RESPONSE == *"content"* ]]; then
    echo "✅ Product Service: OK"
else
    echo "❌ Product Service: FAILED"
    echo "Response: $PRODUCT_RESPONSE"
fi

# Test Health Endpoints
echo "Testing Health Endpoints..."
ACCOUNT_HEALTH=$(curl -s http://localhost:8088/actuator/health | grep -o '"status":"UP"')
PRODUCT_HEALTH=$(curl -s http://localhost:8089/actuator/health | grep -o '"status":"UP"')

if [ "$ACCOUNT_HEALTH" = '"status":"UP"' ]; then
    echo "✅ Account Health: OK"
else
    echo "❌ Account Health: FAILED"
fi

if [ "$PRODUCT_HEALTH" = '"status":"UP"' ]; then
    echo "✅ Product Health: OK"
else
    echo "❌ Product Health: FAILED"
fi

echo "=== Smoke Tests Complete ==="
```

### 2. API Tests (Functional Testing)

#### Authentication Flow Test
```bash
#!/bin/bash
echo "=== Authentication Flow Test ==="

BASE_URL="http://localhost:8088"
TIMESTAMP=$(date +%s)

# Register new user
echo "1. Registering user..."
REGISTER_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser_'$TIMESTAMP'",
    "email": "test'$TIMESTAMP'@example.com",
    "password": "password123"
  }')

if [[ $REGISTER_RESPONSE == *"successfully"* ]]; then
    echo "✅ User Registration: OK"
    USERNAME="testuser_$TIMESTAMP"
else
    echo "❌ User Registration: FAILED"
    echo "Response: $REGISTER_RESPONSE"
    exit 1
fi

# Login user
echo "2. Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser_'$TIMESTAMP'",
    "password": "password123"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ ! -z "$TOKEN" ]; then
    echo "✅ User Login: OK"
    echo "Token: ${TOKEN:0:20}..."
else
    echo "❌ User Login: FAILED"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi

# Test authenticated endpoint
echo "3. Testing authenticated endpoint..."
PROFILE_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" $BASE_URL/api/user/me)

if [[ $PROFILE_RESPONSE == *"username"* ]]; then
    echo "✅ Authenticated Access: OK"
else
    echo "❌ Authenticated Access: FAILED"
    echo "Response: $PROFILE_RESPONSE"
fi

echo "=== Authentication Flow Test Complete ==="
```

#### Product Service Test
```bash
#!/bin/bash
echo "=== Product Service Test ==="

BASE_URL="http://localhost:8089"

# Test public search
echo "1. Testing public product search..."
SEARCH_RESPONSE=$(curl -s "$BASE_URL/api/product/search")

if [[ $SEARCH_RESPONSE == *"content"* ]]; then
    echo "✅ Product Search: OK"
else
    echo "❌ Product Search: FAILED"
    echo "Response: $SEARCH_RESPONSE"
fi

# Test search with parameters
echo "2. Testing parameterized search..."
PARAM_SEARCH=$(curl -s "$BASE_URL/api/product/search?name=Sample&page=0&size=5")

if [[ $PARAM_SEARCH == *"pageable"* ]]; then
    echo "✅ Parameterized Search: OK"
else
    echo "❌ Parameterized Search: FAILED"
    echo "Response: $PARAM_SEARCH"
fi

# Test user products endpoint
echo "3. Testing user products..."
USER_PRODUCTS=$(curl -s "$BASE_URL/api/product?userId=1")

if [[ $USER_PRODUCTS == *"["* ]]; then
    echo "✅ User Products: OK"
else
    echo "❌ User Products: FAILED"
    echo "Response: $USER_PRODUCTS"
fi

# Test error handling
echo "4. Testing error handling..."
ERROR_RESPONSE=$(curl -s "$BASE_URL/api/product?userId=999")

if [[ $ERROR_RESPONSE == *"not found"* ]] || [[ $ERROR_RESPONSE == *"404"* ]]; then
    echo "✅ Error Handling: OK"
else
    echo "❌ Error Handling: FAILED"
    echo "Response: $ERROR_RESPONSE"
fi

echo "=== Product Service Test Complete ==="
```

### 3. Integration Tests (Service Communication)

#### Inter-Service Communication Test
```bash
#!/bin/bash
echo "=== Inter-Service Communication Test ==="

# This tests that Product Service can communicate with Account Service
echo "Testing Feign client communication..."

# Get products for user 1 (should trigger Account Service call)
RESPONSE=$(curl -s "http://localhost:8089/api/product?userId=1")

if [[ $RESPONSE == *"["* ]]; then
    echo "✅ Feign Client Communication: OK"
    echo "Product Service successfully called Account Service"
else
    echo "❌ Feign Client Communication: FAILED"
    echo "Response: $RESPONSE"
fi

# Test with non-existent user (should return 404 from Account Service)
ERROR_RESPONSE=$(curl -s "http://localhost:8089/api/product?userId=999")

if [[ $ERROR_RESPONSE == *"404"* ]] || [[ $ERROR_RESPONSE == *"not found"* ]]; then
    echo "✅ Error Propagation: OK"
    echo "Account Service error properly propagated through Product Service"
else
    echo "❌ Error Propagation: FAILED"
    echo "Response: $ERROR_RESPONSE"
fi

echo "=== Inter-Service Communication Test Complete ==="
```

#### Kafka Integration Test
```bash
#!/bin/bash
echo "=== Kafka Integration Test ==="

# First, get admin token
ADMIN_LOGIN=$(curl -s -X POST http://localhost:8088/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username": "vito", "password": "123456"}')

ADMIN_TOKEN=$(echo $ADMIN_LOGIN | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$ADMIN_TOKEN" ]; then
    echo "❌ Could not get admin token for Kafka test"
    exit 1
fi

# Create a product (should trigger Kafka event)
echo "Creating product to trigger Kafka event..."
PRODUCT_RESPONSE=$(curl -s -X POST http://localhost:8089/api/product \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "name": "Kafka Test Product",
    "description": "Testing Kafka integration",
    "price": 99.99
  }')

if [[ $PRODUCT_RESPONSE == *"Kafka Test Product"* ]]; then
    echo "✅ Product Creation: OK"
    echo "Kafka event should be published"
    
    # Check Kafdrop for the event (manual verification)
    echo "📋 Manual Check: Visit http://localhost:8085 to verify Kafka message"
    echo "   - Look for 'product-events' topic"
    echo "   - Check for recent message with product name 'Kafka Test Product'"
else
    echo "❌ Product Creation: FAILED"
    echo "Response: $PRODUCT_RESPONSE"
fi

echo "=== Kafka Integration Test Complete ==="
```

### 4. Load Tests (Performance Testing)

#### Simple Load Test
```bash
#!/bin/bash
echo "=== Simple Load Test ==="

# Test concurrent requests to public endpoint
echo "Testing 10 concurrent requests..."

for i in {1..10}; do
    curl -s "http://localhost:8089/api/product/search?page=0&size=5" > /dev/null &
done

wait
echo "✅ Load Test Complete"

# Test response time
echo "Testing response time..."
START_TIME=$(date +%s%N)
curl -s "http://localhost:8089/api/product/search" > /dev/null
END_TIME=$(date +%s%N)

RESPONSE_TIME=$(( (END_TIME - START_TIME) / 1000000 ))
echo "Response time: ${RESPONSE_TIME}ms"

if [ $RESPONSE_TIME -lt 1000 ]; then
    echo "✅ Response Time: OK (< 1000ms)"
else
    echo "⚠️  Response Time: SLOW (> 1000ms)"
fi

echo "=== Load Test Complete ==="
```

## 🔍 Advanced Testing

### Database Testing
```bash
#!/bin/bash
echo "=== Database Testing ==="

# Test Account Service database
echo "Testing Account Service database..."
ACCOUNT_DB_TEST=$(docker exec account-db psql -U postgres -d account-service -c "SELECT COUNT(*) FROM users;" 2>/dev/null)

if [[ $ACCOUNT_DB_TEST == *"count"* ]]; then
    echo "✅ Account Database: OK"
    USER_COUNT=$(echo "$ACCOUNT_DB_TEST" | grep -o '[0-9]\+' | tail -1)
    echo "   Users in database: $USER_COUNT"
else
    echo "❌ Account Database: FAILED"
fi

# Test Product Service database
echo "Testing Product Service database..."
PRODUCT_DB_TEST=$(docker exec product-db psql -U postgres -d product-service -c "SELECT COUNT(*) FROM product;" 2>/dev/null)

if [[ $PRODUCT_DB_TEST == *"count"* ]]; then
    echo "✅ Product Database: OK"
    PRODUCT_COUNT=$(echo "$PRODUCT_DB_TEST" | grep -o '[0-9]\+' | tail -1)
    echo "   Products in database: $PRODUCT_COUNT"
else
    echo "❌ Product Database: FAILED"
fi

echo "=== Database Testing Complete ==="
```

### Logging System Test
```bash
#!/bin/bash
echo "=== Logging System Test ==="

# Test Elasticsearch
echo "Testing Elasticsearch..."
ES_RESPONSE=$(curl -s -u elastic:elastic http://localhost:9200/_cluster/health)

if [[ $ES_RESPONSE == *"green"* ]] || [[ $ES_RESPONSE == *"yellow"* ]]; then
    echo "✅ Elasticsearch: OK"
    STATUS=$(echo "$ES_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
    echo "   Cluster status: $STATUS"
else
    echo "❌ Elasticsearch: FAILED"
    echo "Response: $ES_RESPONSE"
fi

# Test Kibana
echo "Testing Kibana..."
KIBANA_RESPONSE=$(curl -s http://localhost:5601/api/status)

if [[ $KIBANA_RESPONSE == *"available"* ]] || [[ $KIBANA_RESPONSE == *"green"* ]]; then
    echo "✅ Kibana: OK"
else
    echo "⚠️  Kibana: Check manually at http://localhost:5601"
fi

# Check for log entries
echo "Checking for log entries..."
LOG_COUNT=$(curl -s -u elastic:elastic "http://localhost:9200/fluentd-*/_count" 2>/dev/null | grep -o '"count":[0-9]*' | cut -d':' -f2)

if [ "$LOG_COUNT" -gt 0 ] 2>/dev/null; then
    echo "✅ Log Entries Found: $LOG_COUNT"
else
    echo "⚠️  No log entries found (may need time to populate)"
fi

echo "=== Logging System Test Complete ==="
```

## 🎯 Test Scenarios

### Scenario 1: New User Journey
```bash
#!/bin/bash
echo "=== New User Journey Test ==="

TIMESTAMP=$(date +%s)
USERNAME="journey_user_$TIMESTAMP"
EMAIL="journey_$TIMESTAMP@example.com"

# 1. Register
echo "1. User registers..."
REGISTER=$(curl -s -X POST http://localhost:8088/api/auth/signup \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$USERNAME\", \"email\": \"$EMAIL\", \"password\": \"password123\"}")

if [[ $REGISTER == *"successfully"* ]]; then
    echo "✅ Registration: OK"
else
    echo "❌ Registration: FAILED"
    echo "Response: $REGISTER"
    exit 1
fi

# 2. Login
echo "2. User logs in..."
LOGIN=$(curl -s -X POST http://localhost:8088/api/auth/signin \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$USERNAME\", \"password\": \"password123\"}")

TOKEN=$(echo $LOGIN | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ ! -z "$TOKEN" ]; then
    echo "✅ Login: OK"
else
    echo "❌ Login: FAILED"
    exit 1
fi

# 3. View profile
echo "3. User views profile..."
PROFILE=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8088/api/user/me)

if [[ $PROFILE == *"$USERNAME"* ]]; then
    echo "✅ Profile Access: OK"
else
    echo "❌ Profile Access: FAILED"
fi

# 4. Browse products
echo "4. User browses products..."
PRODUCTS=$(curl -s "http://localhost:8089/api/product/search?page=0&size=5")

if [[ $PRODUCTS == *"content"* ]]; then
    echo "✅ Product Browsing: OK"
else
    echo "❌ Product Browsing: FAILED"
fi

# 5. Search products
echo "5. User searches products..."
SEARCH=$(curl -s "http://localhost:8089/api/product/search?name=Sample")

if [[ $SEARCH == *"content"* ]]; then
    echo "✅ Product Search: OK"
else
    echo "❌ Product Search: FAILED"
fi

echo "✅ New User Journey: Complete"
```

### Scenario 2: Admin Operations
```bash
#!/bin/bash
echo "=== Admin Operations Test ==="

# Login as admin (using default admin user)
ADMIN_LOGIN=$(curl -s -X POST http://localhost:8088/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username": "vito", "password": "123456"}')

ADMIN_TOKEN=$(echo $ADMIN_LOGIN | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$ADMIN_TOKEN" ]; then
    echo "❌ Admin login failed"
    exit 1
fi

# 1. Create product
echo "1. Admin creates product..."
CREATE_PRODUCT=$(curl -s -X POST http://localhost:8089/api/product \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "name": "Admin Test Product",
    "description": "Created by admin",
    "price": 199.99
  }')

PRODUCT_ID=$(echo $CREATE_PRODUCT | grep -o '"id":[0-9]*' | cut -d':' -f2)

if [ ! -z "$PRODUCT_ID" ]; then
    echo "✅ Product Creation: OK (ID: $PRODUCT_ID)"
else
    echo "❌ Product Creation: FAILED"
    echo "Response: $CREATE_PRODUCT"
fi

# 2. Update product
if [ ! -z "$PRODUCT_ID" ]; then
    echo "2. Admin updates product..."
    UPDATE_PRODUCT=$(curl -s -X PATCH http://localhost:8089/api/product/$PRODUCT_ID \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -d '{"price": 299.99}')

    if [[ $UPDATE_PRODUCT == *"299.99"* ]]; then
        echo "✅ Product Update: OK"
    else
        echo "❌ Product Update: FAILED"
    fi
fi

# 3. Access admin endpoint
echo "3. Admin accesses admin endpoint..."
ADMIN_ACCESS=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8088/api/test/admin)

if [[ $ADMIN_ACCESS == *"Admin Board"* ]]; then
    echo "✅ Admin Access: OK"
else
    echo "❌ Admin Access: FAILED"
    echo "Response: $ADMIN_ACCESS"
fi

echo "✅ Admin Operations: Complete"
```

## 📊 Test Reports

### Generate Test Report
```bash
#!/bin/bash
echo "=== Microservices Test Report ===" > test_report.txt
echo "Generated: $(date)" >> test_report.txt
echo "" >> test_report.txt

# Service Status
echo "## Service Status" >> test_report.txt
docker compose ps >> test_report.txt
echo "" >> test_report.txt

# Health Checks
echo "## Health Checks" >> test_report.txt
echo "Account Service: $(curl -s http://localhost:8088/actuator/health | grep -o '"status":"[^"]*"')" >> test_report.txt
echo "Product Service: $(curl -s http://localhost:8089/actuator/health | grep -o '"status":"[^"]*"')" >> test_report.txt
echo "" >> test_report.txt

# API Tests
echo "## API Test Results" >> test_report.txt
echo "Public Endpoint: $(curl -s http://localhost:8088/api/test/all)" >> test_report.txt
echo "Product Search: $(curl -s http://localhost:8089/api/product/search | wc -c) bytes" >> test_report.txt
echo "" >> test_report.txt

# Infrastructure
echo "## Infrastructure Status" >> test_report.txt
echo "Elasticsearch: $(curl -s -u elastic:elastic http://localhost:9200/_cluster/health | grep -o '"status":"[^"]*"')" >> test_report.txt
echo "Kafka Topics: $(curl -s http://localhost:8085/topic 2>/dev/null | grep -c 'topic' || echo 'N/A')" >> test_report.txt

echo "Test report generated: test_report.txt"
```

## 🛠️ Testing Tools

### Postman Collection
Use the provided Postman collection for GUI testing:

1. **Import collection:** `spring microservices.postman_collection.json`
2. **Set environment variables:**
   - `account_base_url`: http://localhost:8088
   - `product_base_url`: http://localhost:8089
3. **Run collection** with automated tests

### Swagger UI Testing
Interactive API testing:
- **Account Service:** http://localhost:8088/swagger-ui/index.html
- **Product Service:** http://localhost:8089/swagger-ui/index.html

### curl Scripts
All test scripts use curl for maximum compatibility:

```bash
# Make all scripts executable
chmod +x test-*.sh

# Run specific test
./test-smoke.sh
./test-api.sh
./test-integration.sh
```

## 🚨 Troubleshooting Tests

### Common Test Failures

#### Services Not Ready
```bash
# Wait for services
echo "Waiting for services to be ready..."
sleep 30

# Check status
docker compose ps

# Check health
curl http://localhost:8088/actuator/health
curl http://localhost:8089/actuator/health
```

#### Connection Refused
```bash
# Check if ports are accessible
netstat -an | findstr :8088
netstat -an | findstr :8089

# Restart services if needed
docker compose restart account-service-app product-service-app
```

#### Authentication Failures
```bash
# Check if default users exist
docker exec account-db psql -U postgres -d account-service -c "SELECT username FROM users;"

# Reset test data if needed
docker compose down -v
mvn clean install -DskipTests
docker compose up -d --build
```

#### Database Connection Issues
```bash
# Check database connectivity
docker exec account-db pg_isready -U postgres
docker exec product-db pg_isready -U postgres

# Check database logs
docker compose logs account-service-db product-service-db
```

### Test Environment Reset
```bash
#!/bin/bash
echo "Resetting test environment..."

# Stop everything
docker compose down -v

# Clean up
docker system prune -f

# Rebuild and start
mvn clean install -DskipTests
docker compose up -d --build

# Wait for readiness
sleep 60

echo "Test environment reset complete"
```

## 📈 Continuous Testing

### Automated Test Pipeline
```bash
#!/bin/bash
# ci-test.sh - Continuous Integration Test Script

set -e  # Exit on any error

echo "=== CI Test Pipeline ==="

# 1. Build
echo "Building application..."
mvn clean install -DskipTests

# 2. Start services
echo "Starting services..."
docker compose up -d --build

# 3. Wait for readiness
echo "Waiting for services..."
sleep 60

# 4. Run tests
echo "Running smoke tests..."
./test-smoke.sh

echo "Running API tests..."
./test-api.sh

echo "Running integration tests..."
./test-integration.sh

# 5. Generate report
echo "Generating test report..."
./generate-test-report.sh

# 6. Cleanup
echo "Cleaning up..."
docker compose down -v

echo "=== CI Test Pipeline Complete ==="
```

### Test Monitoring
```bash
# Monitor test results
watch -n 30 './test-smoke.sh'

# Continuous health monitoring
while true; do
  curl -f http://localhost:8088/actuator/health || echo "Account service down"
  curl -f http://localhost:8089/actuator/health || echo "Product service down"
  sleep 30
done
```

---

**Happy Testing! 🧪**

Next: [06-Debugging-Guide.md](06-Debugging-Guide.md) for debugging in containers