# Spring Microservices Blueprint

🚀 Java Spring Boot microservices with complete ecosystem: PostgreSQL, Kafka+Zookeeper, Elasticsearch+Kibana+Fluentd, JWT auth, Swagger. Two simple commands launch 10+ pre-configured services!

This project demonstrates event-driven microservices with request tracing: each request carries a correlation_id across REST calls and Kafka messages, making logging and observability easy.

## 🏗️ Multi-Module Maven Architecture

This project demonstrates **Maven Multi-Module** best practices:

```
spring-microservices-blueprint/
├── pom.xml                    # Parent POM with dependency management
├── commons/                   # Shared utilities and DTOs
│   ├── pom.xml
│   └── src/main/java/
├── account-service/           # User management microservice
│   ├── pom.xml
│   └── src/main/java/
└── product-service/           # Product management microservice
    ├── pom.xml
    └── src/main/java/
```

**Benefits:**
- **Shared Dependencies**: Common libraries managed in parent POM
- **Code Reusability**: Shared DTOs and utilities in commons module
- **Consistent Versioning**: All modules use same version from parent
- **Easy Setup & Launch**: Only two commands needed to build and start all services
- **Simplified Development**: Debugging, logging, and monitoring pre-configured for convenience

## 🔧 Prerequisites

**Required:**
- **Java 17+**
- **Maven 3.9+**
- **Docker Desktop**

**Verify installation:**
```bash
java -version    # Should show Java 17+
mvn -version     # Should show Maven 3.9+
docker --version # Should show Docker 20.10+
```

## 🚀 Quick Start (5 Minutes)

**Build and start everything:**
```bash
# Build Maven artifacts first
mvn clean install -DskipTests

# Build Docker images and start all services
docker compose up -d --build
```

**Wait for services to be ready (30-60 seconds):**
```bash
docker compose ps
```

**Test the system:**
```bash
# Test Account Service
curl http://localhost:8088/api/test/all

# Test Product Service  
curl http://localhost:8089/api/product/search
```

If both return data, you're ready! 🎉

## 📚 Complete Documentation

### 🏁 Getting Started
- **[01-Quick-Setup.md](docs/01-Quick-Setup.md)** - Get running in 5 minutes
- **[02-System-Architecture.md](docs/02-System-Architecture.md)** - Understand the design
- **[03-API-Reference.md](docs/03-API-Reference.md)** - Complete API documentation

### 🛠️ Development & Testing  
- **[04-Development-Guide.md](docs/04-Development-Guide.md)** - Local development workflow
- **[05-Testing-Guide.md](docs/05-Testing-Guide.md)** - Testing strategies and scripts
- **[06-Debugging-Guide.md](docs/06-Debugging-Guide.md)** - Debug in containers

### 🐳 Operations & Deployment
- **[07-Docker-Operations.md](docs/07-Docker-Operations.md)** - Container management
- **[08-Monitoring-Logging.md](docs/08-Monitoring-Logging.md)** - Observability setup
- **[09-Troubleshooting.md](docs/09-Troubleshooting.md)** - Common issues & solutions

### 📋 Reference Materials
- **[10-Postman-Collection.md](docs/10-Postman-Collection.md)** - API testing with Postman
- **[11-Configuration-Reference.md](docs/11-Configuration-Reference.md)** - All configuration options

## 🎯 What You'll Learn

### Core Microservices Patterns
- **Service Decomposition** - Separate services for different business domains
- **Database Per Service** - Independent data storage for each service
- **API Gateway Pattern** - Centralized entry point (future enhancement)
- **Service Discovery** - Dynamic service location (future enhancement)

### Communication Patterns
- **Synchronous Communication** - REST APIs with Feign clients
- **Asynchronous Messaging** - Event-driven architecture with Kafka
- **Request/Response** - Direct service-to-service calls
- **Publish/Subscribe** - Event broadcasting for loose coupling

### Cross-Cutting Concerns
- **Authentication & Authorization** - JWT tokens with role-based access
- **Centralized Logging** - ELK Stack for log aggregation
- **Distributed Tracing** - Correlation IDs across service calls
- **Health Monitoring** - Service health checks and metrics
- **Distributed Tracing & Logging** - request body, response body, and Kafka messages carry correlation_id, making it easy to trace interactions across services.


### Infrastructure & DevOps
- **Multi-Module Architecture** - Maven parent-child module structure
- **Containerization** - Docker for consistent environments
- **Container Orchestration** - Docker Compose for multi-service deployment
- **Configuration Management** - Environment-based configuration
- **Database Management** - PostgreSQL with proper schema design

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Layer                            │
│  Web Browser, Mobile App, Postman, curl, etc.             │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP/REST
┌─────────────────────▼───────────────────────────────────────┐
│                  Service Layer                              │
│                                                             │
│  ┌─────────────────┐         ┌─────────────────┐           │
│  │ Account Service │◄────────┤ Product Service │           │
│  │   Port 8088     │  Feign  │   Port 8089     │           │
│  │                 │  Client │                 │           │
│  │ • Authentication│         │ • Product CRUD  │           │
│  │ • User Management│        │ • Search & Filter│          │
│  │ • JWT Tokens    │         │ • Authorization │           │
│  └─────────┬───────┘         └─────────┬───────┘           │
└────────────┼─────────────────────────────┼───────────────────┘
             │                           │
             │ Kafka Events              │ Kafka Events
             ▼                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  Message Layer                              │
│                                                             │
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
│                        │   │                               │
│ ┌─────────┐ ┌────────┐ │   │ ┌─────────────┐ ┌───────────┐ │
│ │Account  │ │Product │ │   │ │Elasticsearch│ │  Kibana   │ │
│ │   DB    │ │   DB   │ │   │ │Port 9200    │ │Port 5601  │ │
│ │Port 5432│ │Port 5434│ │   │ │             │ │           │ │
│ │         │ │        │ │   │ │ Log Storage │ │Log Viewer │ │
│ └─────────┘ └────────┘ │   │ └─────────────┘ └───────────┘ │
└─────────────────────────┘   │ ┌─────────────┐ ┌───────────┐ │
                              │ │   Fluentd   │ │  Kafdrop  │ │
                              │ │Port 24224   │ │Port 8085  │ │
                              │ │             │ │           │ │
                              │ │Log Collector│ │Kafka UI   │ │
                              │ └─────────────┘ └───────────┘ │
                              └─────────────────────────────────┘
```

## 🎮 Try It Out (Interactive Demo)

### 1. Register and Login
```bash
# Register a new user
curl -X POST http://localhost:8088/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username": "demo_user", "email": "demo@example.com", "password": "password123"}'

# Login to get JWT token
curl -X POST http://localhost:8088/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username": "demo_user", "password": "password123"}'
```

### 2. Explore Products
```bash
# Search all products (no authentication needed)
curl "http://localhost:8089/api/product/search"

# Search with filters
curl "http://localhost:8089/api/product/search?name=Sample&page=0&size=5"
```

### 3. Monitor the System
- **API Documentation:** 
  - http://localhost:8088/swagger-ui.html (Account Service)
  - http://localhost:8089/swagger-ui.html (Product Service)
- **View Logs:** http://localhost:5601 (elastic/elastic)
- **Monitor Kafka:** http://localhost:8085
- **Check Health:** 
  - http://localhost:8088/actuator/health
  - http://localhost:8089/actuator/health

## 🛠️ Development Commands

### First Time Setup
```bash
# Build everything from scratch
mvn clean install -DskipTests
docker compose build
docker compose up -d
```

### Daily Development
```bash
# After changing commons module
mvn clean install -DskipTests
docker compose up -d --build --force-recreate

# After changing account-service only
docker compose up -d --build --force-recreate account-service-app

# After changing product-service only  
docker compose up -d --build --force-recreate product-service-app
```

### Debugging & Monitoring
```bash
# View logs
docker compose logs -f account-service-app product-service-app

# Check service status
docker compose ps

# Clean restart everything
docker compose down -v && mvn clean install -DskipTests && docker compose up -d --build
```

## 📊 Module & Service Overview

### Maven Modules
| Module | Purpose | Dependencies |
|--------|---------|-------------|
| **Parent POM** | Dependency management | Spring Boot BOM, Maven plugins |
| **Commons** | Shared utilities, DTOs | Spring Boot Starter |
| **Account Service** | User management, JWT auth | Commons, Spring Security, PostgreSQL |
| **Product Service** | Product CRUD, search | Commons, Spring JPA, Feign Client |

### Runtime Services
| Service | Port | Purpose | Technology Stack |
|---------|------|---------|------------------|
| **Account Service** | 8088 | User management, JWT auth | Spring Boot, Spring Security, PostgreSQL |
| **Product Service** | 8089 | Product CRUD, search | Spring Boot, JPA, PostgreSQL, Feign |
| **Account Database** | 5432 | User data storage | PostgreSQL 15 |
| **Product Database** | 5434 | Product data storage | PostgreSQL 15 |
| **Kafka** | 9092 | Event streaming | Apache Kafka |
| **Zookeeper** | 2181 | Kafka coordination | Apache Zookeeper |
| **Elasticsearch** | 9200 | Log storage & search | Elasticsearch 7.17 |
| **Kibana** | 5601 | Log visualization | Kibana 7.17 |
| **Fluentd** | 24224 | Log collection | Fluentd |
| **Kafdrop** | 8085 | Kafka monitoring | Kafdrop UI |

## 🎓 Learning Path

### **Beginner (Week 1-2)**
1. **Start Here:** [01-Quick-Setup.md](docs/01-Quick-Setup.md)
2. **Understand:** [02-System-Architecture.md](docs/02-System-Architecture.md)
3. **Try APIs:** [03-API-Reference.md](docs/03-API-Reference.md)
4. **Import:** [10-Postman-Collection.md](docs/10-Postman-Collection.md)

### **Intermediate (Week 3-4)**
1. **Develop:** [04-Development-Guide.md](docs/04-Development-Guide.md)
2. **Test:** [05-Testing-Guide.md](docs/05-Testing-Guide.md)
3. **Debug:** [06-Debugging-Guide.md](docs/06-Debugging-Guide.md)
4. **Monitor:** [08-Monitoring-Logging.md](docs/08-Monitoring-Logging.md)

### **Advanced (Week 5+)**
1. **Deploy:** [07-Docker-Operations.md](docs/07-Docker-Operations.md)
2. **Configure:** [11-Configuration-Reference.md](docs/11-Configuration-Reference.md)
3. **Troubleshoot:** [09-Troubleshooting.md](docs/09-Troubleshooting.md)
4. **Extend:** Add new services and features



## 🚨 Common Issues & Quick Fixes

### Services won't start?
```bash
# Check if ports are in use
netstat -an | findstr :8088
netstat -an | findstr :8089

# Clean restart
docker compose down -v
docker system prune -f
mvn clean install -DskipTests
docker compose up -d --build
```

### Build failures?
```bash
# Clear Maven cache
mvn dependency:purge-local-repository

# Rebuild everything
mvn clean install -DskipTests
```

### Out of memory?
```bash
# Clean Docker resources
docker system prune -af --volumes

# Increase Docker memory limit to 8GB+
```

## 🤝 Contributing

This is a learning project! Contributions welcome:

1. **Fork** the repository
2. **Create** a feature branch
3. **Make** your changes
4. **Test** thoroughly
5. **Submit** a pull request

### Ideas for Contributions
- Add new microservices (notification, payment, etc.)
- Implement API Gateway (Spring Cloud Gateway)
- Add caching layer (Redis)
- Implement circuit breakers (Resilience4j)
- Add metrics collection (Prometheus/Grafana)
- Create Kubernetes deployment manifests

## 📄 License

This project is for **educational purposes**. Feel free to use, modify, and learn from it!

---

## 🎉 Ready to Start?

1. **Quick Start:** Follow the 5-minute setup above
2. **Deep Dive:** Read [01-Quick-Setup.md](docs/01-Quick-Setup.md)
3. **Get Help:** Check [09-Troubleshooting.md](docs/09-Troubleshooting.md)

**Happy Learning! 🚀**

⭐ **Found this helpful?** Give it a star to support the project!

*Built with ❤️ for the microservices learning community*