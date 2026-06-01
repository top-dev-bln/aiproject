# 08 - Monitoring & Logging Guide

📊 **Complete observability setup for microservices monitoring**

## 🎯 Observability Stack Overview

The system includes a complete observability stack:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Services      │    │   Log Pipeline  │    │   Visualization │
│                 │    │                 │    │                 │
│ Account Service ├───►│    Fluentd      ├───►│     Kibana      │
│ Product Service │    │  (Collector)    │    │  (Dashboard)    │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────┬───────┘    └─────────────────┘
                                 │
                       ┌─────────▼───────┐
                       │ Elasticsearch   │
                       │   (Storage)     │
                       └─────────────────┘
```

## 🔍 Accessing Monitoring Tools

### Kibana (Log Visualization)
- **URL:** http://localhost:5601
- **Login:** elastic / elastic
- **Purpose:** View and analyze application logs

### Kafdrop (Kafka Monitoring)
- **URL:** http://localhost:8085
- **Login:** None required
- **Purpose:** Monitor Kafka topics and messages

### Elasticsearch (Direct Access)
- **URL:** http://localhost:9200
- **Login:** elastic / elastic
- **Purpose:** Direct search and data access

### Health Endpoints
- **Account Service:** http://localhost:8088/actuator/health
- **Product Service:** http://localhost:8089/actuator/health

## 📋 Kibana Setup & Usage

### Initial Setup

1. **Open Kibana:** http://localhost:5601
2. **Login:** elastic / elastic
3. **Create Index Pattern:**
   - Go to **Stack Management** → **Index Patterns**
   - Click **Create index pattern**
   - Enter pattern: `fluentd-*`
   - Select timestamp field: `@timestamp`
   - Click **Create index pattern**

### Viewing Logs

1. **Go to Discover** (left sidebar)
2. **Select index pattern:** fluentd-*
3. **Set time range:** Last 15 minutes, 1 hour, etc.
4. **View logs** in real-time

### Key Log Fields

| Field | Description | Example |
|-------|-------------|---------|
| `@timestamp` | Log timestamp | 2024-01-01T12:00:00.000Z |
| `level` | Log level | INFO, WARN, ERROR, DEBUG |
| `logger_name` | Java logger name | com.demo.service.ProductService |
| `message` | Log message | User registered successfully |
| `correlation_id` | Request tracing ID | abc123-def456-ghi789 |
| `container_name` | Docker container | account-service-app |
| `thread_name` | Thread name | http-nio-8088-exec-1 |
| `request_method` | HTTP method | POST, GET, PUT |
| `request_url` | Request URL | /api/auth/signin |
| `user_id` | User identifier | 123 |
| `response_time` | Response time (ms) | 245 |
| `http_status` | HTTP status code | 200, 404, 500 |

### Useful Kibana Queries

#### Filter by Service
```
container_name: "account-service-app"
```

#### Filter by Log Level
```
level: "ERROR"
```

#### Filter by Correlation ID
```
correlation_id: "abc123-def456-ghi789"
```

#### Filter by User
```
user_id: "123"
```

#### Filter by Time Range and Service
```
container_name: "product-service-app" AND @timestamp: [now-1h TO now]
```

#### Find Slow Requests
```
response_time: >1000
```

#### Find Authentication Errors
```
message: "authentication" AND level: "ERROR"
```

### Creating Visualizations

#### 1. Response Time Chart
1. **Go to Visualize** → **Create visualization**
2. **Select:** Line chart
3. **Index pattern:** fluentd-*
4. **Y-axis:** Average of response_time
5. **X-axis:** Date histogram of @timestamp
6. **Save** with name "Response Time"

#### 2. Error Rate Dashboard
1. **Create visualization:** Pie chart
2. **Metrics:** Count
3. **Buckets:** Terms aggregation on level
4. **Filter:** level: ("ERROR" OR "WARN")

#### 3. Service Health Dashboard
1. **Create visualization:** Metric
2. **Metrics:** Unique count of container_name
3. **Filter:** level: "INFO" AND message: "Started"

### Creating Dashboards

1. **Go to Dashboard** → **Create dashboard**
2. **Add visualizations** created above
3. **Arrange panels** as needed
4. **Save dashboard** with name "Microservices Overview"

## 📈 Kafka Monitoring with Kafdrop

### Accessing Kafdrop

1. **Open:** http://localhost:8085
2. **View topics:** user-events, product-events
3. **Monitor messages** in real-time

### Key Features

#### Topic Overview
- **Topic list:** All available topics
- **Partition info:** Partition count and distribution
- **Message count:** Total messages per topic
- **Consumer groups:** Active consumers

#### Message Inspection
- **Browse messages:** View message content
- **Message headers:** Kafka headers and metadata
- **Partition details:** Message distribution across partitions
- **Offset information:** Current and lag offsets

#### Consumer Monitoring
- **Consumer groups:** Active consumer groups
- **Lag monitoring:** Consumer lag per partition
- **Offset tracking:** Current consumer positions

### Monitoring Kafka Events

#### User Events Topic
- **Topic:** user-events
- **Events:** User registration, login, profile updates
- **Key:** User ID
- **Value:** User event details

#### Product Events Topic
- **Topic:** product-events
- **Events:** Product creation, updates, deletions
- **Key:** Product ID
- **Value:** Product event details

## 🔧 Application Health Monitoring

### Health Check Endpoints

#### Account Service Health
```bash
curl http://localhost:8088/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 250685575168,
        "free": 100685575168,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

#### Product Service Health
```bash
curl http://localhost:8089/actuator/health
```

### Detailed Health Information

#### Database Health
```bash
curl http://localhost:8088/actuator/health/db
curl http://localhost:8089/actuator/health/db
```

#### Disk Space Health
```bash
curl http://localhost:8088/actuator/health/diskSpace
```

#### Custom Health Indicators

The services include custom health checks:

```java
@Component
public class KafkaHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // Check Kafka connectivity
            kafkaTemplate.send("health-check", "ping").get(5, TimeUnit.SECONDS);
            return Health.up()
                .withDetail("kafka", "Connected")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("kafka", "Connection failed")
                .withException(e)
                .build();
        }
    }
}
```

## 📊 Metrics and Monitoring

### Actuator Endpoints

#### Available Endpoints
```bash
curl http://localhost:8088/actuator
```

**Key endpoints:**
- `/actuator/health` - Health status
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics
- `/actuator/env` - Environment properties
- `/actuator/loggers` - Logger configuration

#### Application Metrics
```bash
# JVM memory usage
curl http://localhost:8088/actuator/metrics/jvm.memory.used

# HTTP request metrics
curl http://localhost:8088/actuator/metrics/http.server.requests

# Database connection pool
curl http://localhost:8088/actuator/metrics/hikaricp.connections.active
```

### Custom Metrics

The application includes custom metrics:

```java
@Component
public class CustomMetrics {
    
    private final Counter userRegistrations;
    private final Timer requestTimer;
    private final Gauge activeUsers;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.userRegistrations = Counter.builder("user.registrations")
            .description("Number of user registrations")
            .register(meterRegistry);
            
        this.requestTimer = Timer.builder("api.request.duration")
            .description("API request duration")
            .register(meterRegistry);
    }
    
    public void incrementUserRegistrations() {
        userRegistrations.increment();
    }
}
```

## 🔍 Log Analysis Patterns

### Correlation ID Tracing

**Find all logs for a specific request:**
```
correlation_id: "abc123-def456-ghi789"
```

**Trace request flow across services:**
1. Search for correlation ID in Kibana
2. Sort by timestamp
3. Follow request through services
4. Identify bottlenecks or errors

### Error Analysis

**Find all errors in last hour:**
```
level: "ERROR" AND @timestamp: [now-1h TO now]
```

**Group errors by service:**
```
level: "ERROR" AND @timestamp: [now-24h TO now]
```
Then use aggregation on `container_name`

**Find authentication failures:**
```
message: "authentication" AND level: "ERROR"
```

### Performance Analysis

**Find slow requests:**
```
response_time: >1000
```

**Average response time by endpoint:**
Use aggregation on `request_url` with average of `response_time`

**Database query performance:**
```
logger_name: "org.hibernate.SQL" AND @timestamp: [now-1h TO now]
```

## 🚨 Alerting and Monitoring

### Log-based Alerts

#### Error Rate Alert
Create alert when error rate exceeds threshold:
```
level: "ERROR" AND @timestamp: [now-5m TO now]
```
Count > 10 in 5 minutes

#### Response Time Alert
Alert on slow responses:
```
response_time: >5000
```
Any request taking more than 5 seconds

#### Service Down Alert
Alert when service stops logging:
```
container_name: "account-service-app" AND @timestamp: [now-2m TO now]
```
No logs in 2 minutes

### Health Check Monitoring

#### Automated Health Checks
```bash
#!/bin/bash
# health-monitor.sh

while true; do
    # Check Account Service
    if ! curl -f http://localhost:8088/actuator/health > /dev/null 2>&1; then
        echo "ALERT: Account Service is down!"
        # Send notification (email, Slack, etc.)
    fi
    
    # Check Product Service
    if ! curl -f http://localhost:8089/actuator/health > /dev/null 2>&1; then
        echo "ALERT: Product Service is down!"
        # Send notification
    fi
    
    sleep 30
done
```

#### Service Dependency Monitoring
```bash
#!/bin/bash
# dependency-monitor.sh

# Check database connectivity
if ! docker exec account-db pg_isready -U postgres > /dev/null 2>&1; then
    echo "ALERT: Account Database is down!"
fi

# Check Kafka connectivity
if ! docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; then
    echo "ALERT: Kafka is down!"
fi

# Check Elasticsearch
if ! curl -s -u elastic:elastic http://localhost:9200/_cluster/health | grep -q "green\|yellow"; then
    echo "ALERT: Elasticsearch cluster is unhealthy!"
fi
```

## 📈 Performance Monitoring

### Resource Usage Monitoring

#### Container Resource Usage
```bash
# Monitor container resources
docker stats account-service-app product-service-app

# Get resource usage in JSON format
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"
```

#### JVM Monitoring
```bash
# JVM memory metrics
curl http://localhost:8088/actuator/metrics/jvm.memory.used
curl http://localhost:8088/actuator/metrics/jvm.memory.max

# Garbage collection metrics
curl http://localhost:8088/actuator/metrics/jvm.gc.pause

# Thread metrics
curl http://localhost:8088/actuator/metrics/jvm.threads.live
```

### Database Performance

#### Connection Pool Monitoring
```bash
# HikariCP connection pool metrics
curl http://localhost:8088/actuator/metrics/hikaricp.connections.active
curl http://localhost:8088/actuator/metrics/hikaricp.connections.pending
```

#### Database Query Analysis
```sql
-- Connect to database
docker exec -it account-db psql -U postgres -d account-service

-- Check active connections
SELECT * FROM pg_stat_activity;

-- Check slow queries
SELECT query, mean_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;

-- Check table statistics
SELECT * FROM pg_stat_user_tables;
```

## 🔧 Troubleshooting Monitoring Issues

### Kibana Issues

#### Can't Access Kibana
```bash
# Check Kibana container
docker compose logs kibana

# Check Elasticsearch health
curl -u elastic:elastic http://localhost:9200/_cluster/health

# Restart Kibana
docker compose restart kibana
```

#### No Logs in Kibana
```bash
# Check Fluentd logs
docker compose logs fluentd

# Check if logs are being generated
docker compose logs account-service-app | head -10

# Check Elasticsearch indices
curl -u elastic:elastic http://localhost:9200/_cat/indices
```

### Kafdrop Issues

#### Can't Access Kafdrop
```bash
# Check Kafdrop container
docker compose logs kafdrop

# Check Kafka health
docker compose logs kafka

# Restart Kafdrop
docker compose restart kafdrop
```

#### No Topics Visible
```bash
# Check Kafka topics manually
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Check Zookeeper
docker compose logs zookeeper
```

### Health Check Issues

#### Services Show Unhealthy
```bash
# Check service logs
docker compose logs account-service-app

# Check database connectivity
docker exec account-service-app nc -z account-service-db 5432

# Check Kafka connectivity
docker exec account-service-app nc -z kafka 29092
```

## 🚀 Advanced Monitoring

### Custom Dashboards

#### Service Overview Dashboard
Create dashboard with:
- Service health status
- Request rate per service
- Error rate per service
- Response time percentiles
- Active user count

#### Infrastructure Dashboard
Create dashboard with:
- Container resource usage
- Database connection pools
- Kafka topic lag
- Elasticsearch cluster health

### Log Retention and Management

#### Elasticsearch Index Management
```bash
# Check index sizes
curl -u elastic:elastic http://localhost:9200/_cat/indices?v

# Delete old indices (older than 7 days)
curl -X DELETE -u elastic:elastic "http://localhost:9200/fluentd-$(date -d '7 days ago' +%Y.%m.%d)"
```

#### Log Rotation Configuration
```yaml
# fluentd/conf/fluent.conf
<match **>
  @type elasticsearch
  host elasticsearch
  port 9200
  user elastic
  password elastic
  index_name fluentd
  type_name _doc
  time_key @timestamp
  time_format %Y-%m-%dT%H:%M:%S.%NZ
  include_timestamp true
  reload_connections false
  reconnect_on_error true
  reload_on_failure true
  
  # Index lifecycle management
  <buffer time>
    timekey 1d
    timekey_wait 10m
    timekey_use_utc true
  </buffer>
</match>
```

---

**Master your microservices observability! 📊**

Next: [09-Troubleshooting.md](09-Troubleshooting.md) for common issues and solutions