# 🚀 Deployment Guide

This guide provides detailed instructions for deploying the Job Recommendation System in different environments.

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Environment Setup](#environment-setup)
- [Development Deployment](#development-deployment)
- [Production Deployment](#production-deployment)
- [Docker Deployment](#docker-deployment)
- [Cloud Deployment](#cloud-deployment)
- [Monitoring and Maintenance](#monitoring-and-maintenance)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

**Minimum Requirements:**
- CPU: 2 cores
- RAM: 4GB
- Storage: 20GB
- OS: Linux (Ubuntu 18.04+), Windows Server 2016+, or macOS 10.14+

**Recommended Requirements:**
- CPU: 4+ cores
- RAM: 8GB+
- Storage: 50GB+ SSD
- OS: Linux (Ubuntu 20.04+)

### Software Dependencies

| Component | Version | Purpose |
|-----------|---------|---------|
| Java JDK | 11+ | Runtime environment |
| Apache Maven | 3.6+ | Build tool |
| MySQL | 8.0+ | Database |
| Redis | 6.0+ | Caching |
| Apache Tomcat | 9.0+ | Application server |

### External Services

- **SerpAPI Account**: For job search functionality
- **EdenAI Account**: For keyword extraction
- **Email Service** (optional): For notifications

## Environment Setup

### 1. Java Installation

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-11-jdk
java -version
```

**CentOS/RHEL:**
```bash
sudo yum install java-11-openjdk-devel
java -version
```

**Windows:**
1. Download Oracle JDK 11+ or OpenJDK
2. Install and set JAVA_HOME environment variable
3. Add Java to system PATH

### 2. Maven Installation

**Ubuntu/Debian:**
```bash
sudo apt install maven
mvn -version
```

**Windows:**
1. Download Maven from Apache Maven website
2. Extract to C:\Program Files\Maven
3. Set M2_HOME and add to PATH

### 3. MySQL Installation

**Ubuntu/Debian:**
```bash
sudo apt install mysql-server
sudo mysql_secure_installation
sudo systemctl start mysql
sudo systemctl enable mysql
```

**Windows:**
1. Download MySQL Installer from MySQL website
2. Follow installation wizard
3. Configure root password and create database user

### 4. Redis Installation

**Ubuntu/Debian:**
```bash
sudo apt install redis-server
sudo systemctl start redis-server
sudo systemctl enable redis-server
redis-cli ping  # Should return PONG
```

**Windows:**
1. Download Redis for Windows
2. Install and start Redis service
3. Test connection with redis-cli

### 5. Tomcat Installation

**Ubuntu/Debian:**
```bash
sudo apt install tomcat9
sudo systemctl start tomcat9
sudo systemctl enable tomcat9
```

**Manual Installation:**
```bash
# Download Tomcat
wget https://downloads.apache.org/tomcat/tomcat-9/v9.0.65/bin/apache-tomcat-9.0.65.tar.gz
tar -xzf apache-tomcat-9.0.65.tar.gz
sudo mv apache-tomcat-9.0.65 /opt/tomcat

# Set permissions
sudo chown -R tomcat:tomcat /opt/tomcat
sudo chmod +x /opt/tomcat/bin/*.sh
```

## Development Deployment

### 1. Clone and Build

```bash
# Clone repository
git clone https://github.com/your-username/job-recommendation-system.git
cd job-recommendation-system

# Build application
mvn clean compile package
```

### 2. Database Setup

```bash
# Connect to MySQL
mysql -u root -p

# Create database and user
CREATE DATABASE jobrec_dev;
CREATE USER 'dev_user'@'localhost' IDENTIFIED BY 'dev_password';
GRANT ALL PRIVILEGES ON jobrec_dev.* TO 'dev_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# Initialize database
mysql -u dev_user -p jobrec_dev < scripts/init-database.sql
```

### 3. Environment Configuration

Create `.env` file:
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=jobrec_dev
DB_USERNAME=dev_user
DB_PASSWORD=dev_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# API Keys
SERPAPI_KEY=your_serpapi_key_here
EDENAI_KEY=your_edenai_key_here

# Application Environment
APP_ENVIRONMENT=dev
```

### 4. Deploy to Tomcat

```bash
# Copy WAR file
sudo cp target/JobSearch-1.0-SNAPSHOT.war /opt/tomcat/webapps/jobrec.war

# Restart Tomcat
sudo systemctl restart tomcat9

# Check deployment
curl http://localhost:8080/jobrec/
```

### 5. Verify Deployment

```bash
# Check application logs
tail -f /opt/tomcat/logs/catalina.out

# Test API endpoints
curl http://localhost:8080/jobrec/search?user_id=test_user&lat=37.7749&lon=-122.4194

# Check database connection
mysql -u dev_user -p jobrec_dev -e "SELECT COUNT(*) FROM users;"

# Test Redis connection
redis-cli ping
```

## Production Deployment

### 1. Security Hardening

**Database Security:**
```sql
-- Create production user with limited privileges
CREATE USER 'jobrec_prod'@'localhost' IDENTIFIED BY 'strong_production_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON jobrec.* TO 'jobrec_prod'@'localhost';

-- Remove test users and data
DELETE FROM users WHERE user_id LIKE 'test%';
```

**System Security:**
```bash
# Configure firewall
sudo ufw enable
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw allow 8080/tcp  # Tomcat (if needed)

# Secure MySQL
mysql_secure_installation

# Configure Redis security
sudo nano /etc/redis/redis.conf
# Add: requirepass your_redis_password
sudo systemctl restart redis
```

### 2. SSL/TLS Configuration

**Generate SSL Certificate:**
```bash
# For testing (self-signed)
keytool -genkey -alias tomcat -keyalg RSA -keystore /opt/tomcat/conf/keystore.jks

# For production (Let's Encrypt)
sudo apt install certbot
sudo certbot certonly --standalone -d yourdomain.com
```

**Configure Tomcat SSL:**
Edit `/opt/tomcat/conf/server.xml`:
```xml
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="150" SSLEnabled="true">
    <SSLHostConfig>
        <Certificate certificateKeystoreFile="conf/keystore.jks"
                     certificateKeystorePassword="your_keystore_password"
                     type="RSA" />
    </SSLHostConfig>
</Connector>
```

### 3. Production Environment Variables

```bash
# Set production environment variables
export APP_ENVIRONMENT=prod
export DB_HOST=your-production-db-host
export DB_NAME=jobrec
export DB_USERNAME=jobrec_prod
export DB_PASSWORD=your_secure_password
export REDIS_HOST=your-production-redis-host
export REDIS_PASSWORD=your_redis_password
export SERPAPI_KEY=your_production_serpapi_key
export EDENAI_KEY=your_production_edenai_key
```

### 4. Performance Optimization

**Tomcat Configuration (`/opt/tomcat/conf/server.xml`):**
```xml
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443"
           maxThreads="200"
           minSpareThreads="10"
           maxSpareThreads="50"
           acceptCount="100"
           compression="on"
           compressionMinSize="2048"
           compressableMimeType="text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json" />
```

**JVM Tuning (`/opt/tomcat/bin/setenv.sh`):**
```bash
export CATALINA_OPTS="$CATALINA_OPTS -Xms2048m -Xmx4096m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UseG1GC"
export CATALINA_OPTS="$CATALINA_OPTS -XX:MaxGCPauseMillis=200"
export CATALINA_OPTS="$CATALINA_OPTS -Dapp.environment=prod"
```

**MySQL Optimization (`/etc/mysql/mysql.conf.d/mysqld.cnf`):**
```ini
[mysqld]
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
max_connections = 200
query_cache_size = 128M
tmp_table_size = 64M
max_heap_table_size = 64M
```

**Redis Optimization (`/etc/redis/redis.conf`):**
```ini
maxmemory 1gb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000
```

## Docker Deployment

### 1. Create Dockerfile

```dockerfile
FROM tomcat:9.0-jdk11-openjdk

# Install required packages
RUN apt-get update && apt-get install -y \
    mysql-client \
    redis-tools \
    && rm -rf /var/lib/apt/lists/*

# Copy application
COPY target/JobSearch-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/jobrec.war

# Copy configuration files
COPY Backend/resources/ /usr/local/tomcat/conf/

# Set environment variables
ENV CATALINA_OPTS="-Xms1024m -Xmx2048m -Dapp.environment=prod"

# Expose ports
EXPOSE 8080 8443

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/jobrec/ || exit 1

# Start Tomcat
CMD ["catalina.sh", "run"]
```

### 2. Docker Compose Configuration

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
      - "8443:8443"
    environment:
      - DB_HOST=mysql
      - DB_NAME=jobrec
      - DB_USERNAME=jobrec_user
      - DB_PASSWORD=jobrec_password
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - SERPAPI_KEY=${SERPAPI_KEY}
      - EDENAI_KEY=${EDENAI_KEY}
    depends_on:
      - mysql
      - redis
    volumes:
      - app_logs:/usr/local/tomcat/logs
    restart: unless-stopped

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=root_password
      - MYSQL_DATABASE=jobrec
      - MYSQL_USER=jobrec_user
      - MYSQL_PASSWORD=jobrec_password
    volumes:
      - mysql_data:/var/lib/mysql
      - ./scripts/init-database.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"
    restart: unless-stopped

  redis:
    image: redis:6.0-alpine
    command: redis-server --requirepass redis_password
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/ssl/certs
    depends_on:
      - app
    restart: unless-stopped

volumes:
  mysql_data:
  redis_data:
  app_logs:
```

### 3. Deploy with Docker

```bash
# Build and start services
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f app

# Scale application (if needed)
docker-compose up -d --scale app=3
```

## Cloud Deployment

### AWS Deployment

**1. EC2 Instance Setup:**
```bash
# Launch EC2 instance (t3.medium recommended)
# Configure security groups (ports 22, 80, 443, 8080)
# Connect via SSH

# Install Docker
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

**2. RDS Database Setup:**
```bash
# Create RDS MySQL instance
# Configure security groups for database access
# Update environment variables with RDS endpoint
```

**3. ElastiCache Redis Setup:**
```bash
# Create ElastiCache Redis cluster
# Configure security groups
# Update Redis connection settings
```

### Google Cloud Platform

**1. Compute Engine Setup:**
```bash
# Create VM instance
gcloud compute instances create jobrec-instance \
    --zone=us-central1-a \
    --machine-type=e2-medium \
    --image-family=ubuntu-2004-lts \
    --image-project=ubuntu-os-cloud

# SSH into instance
gcloud compute ssh jobrec-instance --zone=us-central1-a
```

**2. Cloud SQL Setup:**
```bash
# Create Cloud SQL MySQL instance
gcloud sql instances create jobrec-db \
    --database-version=MYSQL_8_0 \
    --tier=db-f1-micro \
    --region=us-central1

# Create database and user
gcloud sql databases create jobrec --instance=jobrec-db
gcloud sql users create jobrec-user --instance=jobrec-db --password=secure_password
```

## Monitoring and Maintenance

### 1. Application Monitoring

**Log Monitoring:**
```bash
# Application logs
tail -f /opt/tomcat/logs/catalina.out
tail -f /opt/tomcat/logs/application.log

# System logs
journalctl -u tomcat9 -f
journalctl -u mysql -f
journalctl -u redis -f
```

**Performance Monitoring:**
```bash
# System resources
htop
iostat -x 1
free -h
df -h

# Database monitoring
mysql -e "SHOW PROCESSLIST;"
mysql -e "SHOW ENGINE INNODB STATUS\G"

# Redis monitoring
redis-cli info
redis-cli monitor
```

### 2. Health Checks

Create health check script (`/opt/scripts/health-check.sh`):
```bash
#!/bin/bash

# Check application health
APP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/jobrec/)
if [ "$APP_STATUS" != "200" ]; then
    echo "Application health check failed: $APP_STATUS"
    exit 1
fi

# Check database connection
mysql -h localhost -u jobrec_user -p$DB_PASSWORD -e "SELECT 1;" > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "Database health check failed"
    exit 1
fi

# Check Redis connection
redis-cli ping > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "Redis health check failed"
    exit 1
fi

echo "All health checks passed"
```

### 3. Backup Strategy

**Database Backup:**
```bash
#!/bin/bash
# /opt/scripts/backup-db.sh

BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="jobrec"

# Create backup
mysqldump -u jobrec_user -p$DB_PASSWORD $DB_NAME > $BACKUP_DIR/jobrec_$DATE.sql

# Compress backup
gzip $BACKUP_DIR/jobrec_$DATE.sql

# Remove old backups (keep 30 days)
find $BACKUP_DIR -name "jobrec_*.sql.gz" -mtime +30 -delete

echo "Database backup completed: jobrec_$DATE.sql.gz"
```

**Application Backup:**
```bash
#!/bin/bash
# /opt/scripts/backup-app.sh

BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)
APP_DIR="/opt/tomcat/webapps/jobrec"

# Create application backup
tar -czf $BACKUP_DIR/app_$DATE.tar.gz -C /opt/tomcat/webapps jobrec

# Remove old backups
find $BACKUP_DIR -name "app_*.tar.gz" -mtime +7 -delete

echo "Application backup completed: app_$DATE.tar.gz"
```

### 4. Automated Maintenance

**Crontab Configuration:**
```bash
# Edit crontab
crontab -e

# Add maintenance tasks
0 2 * * * /opt/scripts/backup-db.sh
0 3 * * 0 /opt/scripts/backup-app.sh
*/5 * * * * /opt/scripts/health-check.sh
0 4 * * * /opt/scripts/cleanup-logs.sh
```

## Troubleshooting

### Common Issues

**1. Application Won't Start**
```bash
# Check Java version
java -version

# Check Tomcat logs
tail -f /opt/tomcat/logs/catalina.out

# Check port conflicts
netstat -tlnp | grep :8080

# Check permissions
ls -la /opt/tomcat/webapps/jobrec.war
```

**2. Database Connection Issues**
```bash
# Test database connection
mysql -h localhost -u jobrec_user -p

# Check MySQL status
systemctl status mysql

# Check database logs
tail -f /var/log/mysql/error.log

# Verify user permissions
mysql -e "SHOW GRANTS FOR 'jobrec_user'@'localhost';"
```

**3. Redis Connection Issues**
```bash
# Test Redis connection
redis-cli ping

# Check Redis status
systemctl status redis

# Check Redis logs
tail -f /var/log/redis/redis-server.log

# Check Redis configuration
redis-cli config get "*"
```

**4. Performance Issues**
```bash
# Check system resources
top
free -h
iostat -x 1

# Check database performance
mysql -e "SHOW PROCESSLIST;"
mysql -e "SHOW ENGINE INNODB STATUS\G"

# Check application logs for slow queries
grep -i "slow" /opt/tomcat/logs/application.log
```

### Recovery Procedures

**1. Database Recovery**
```bash
# Stop application
sudo systemctl stop tomcat9

# Restore from backup
gunzip /opt/backups/jobrec_20231201_020000.sql.gz
mysql -u root -p jobrec < /opt/backups/jobrec_20231201_020000.sql

# Restart services
sudo systemctl start mysql
sudo systemctl start tomcat9
```

**2. Application Recovery**
```bash
# Stop Tomcat
sudo systemctl stop tomcat9

# Remove current application
rm -rf /opt/tomcat/webapps/jobrec*

# Restore from backup
tar -xzf /opt/backups/app_20231201_030000.tar.gz -C /opt/tomcat/webapps/

# Restart Tomcat
sudo systemctl start tomcat9
```

### Support Contacts

- **System Administrator**: Please create a GitHub issue for system-related questions
- **Database Administrator**: Please create a GitHub issue for database-related questions  
- **Development Team**: Please create a GitHub issue for development questions
- **Emergency Support**: Contact through official project channels

---

**Author**: Leo Ji  
**Last Updated**: December 2024  
**Version**: 1.0.0
