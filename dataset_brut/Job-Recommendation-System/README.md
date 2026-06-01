# 🚀 Job Recommendation System

An intelligent job recommendation system that provides location-based job search with personalized recommendations, built with Java, MySQL, Redis, and modern web technologies.

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Usage](#-usage)
- [API Documentation](#-api-documentation)
- [Development](#-development)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)
- [License](#-license)

## ✨ Features

### Core Functionality
- **🌍 Location-Based Search**: Find jobs near any geographic location
- **⭐ Smart Favorites**: Save and manage favorite job listings
- **🔍 Keyword Extraction**: Automatic job keyword extraction using AI
- **📊 Personalized Recommendations**: AI-powered job recommendations
- **⚡ Redis Caching**: High-performance caching for improved response times

### Security & Performance
- **🔐 Secure Authentication**: MD5-hashed passwords with session management
- **🛡️ Input Validation**: Comprehensive input sanitization and validation
- **📝 Structured Logging**: Centralized logging with multiple levels
- **⚙️ Environment Configuration**: Flexible configuration management
- **🚦 Error Handling**: Structured exception handling with user-friendly messages

### User Experience
- **💻 Responsive Web Interface**: Modern, mobile-friendly UI
- **🔄 Real-time Updates**: Dynamic content loading with AJAX
- **📱 Cross-platform Compatibility**: Works on desktop, tablet, and mobile
- **🎨 Professional Design**: Clean, intuitive user interface

## 🏗️ Architecture

### System Components

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │    Backend      │    │   External APIs │
│                 │    │                 │    │                 │
│ • HTML/CSS/JS   │◄──►│ • Java Servlets │◄──►│ • SerpAPI       │
│ • Bootstrap     │    │ • REST APIs     │    │ • EdenAI        │
│ • AJAX          │    │ • Business Logic│    │ • GeoConverter  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   Data Layer    │    │     Cache       │
                       │                 │    │                 │
                       │ • MySQL DB      │◄──►│ • Redis         │
                       │ • Connection    │    │ • Session Store │
                       │   Pooling       │    │ • Search Cache  │
                       └─────────────────┘    └─────────────────┘
```

### Technology Stack

**Backend:**
- Java 11+ with Servlet API
- Maven for dependency management
- MySQL 8.0+ for data persistence
- Redis for caching and session management

**Frontend:**
- HTML5, CSS3, JavaScript (ES6+)
- Bootstrap for responsive design
- AJAX for asynchronous communication

**External Services:**
- **SerpAPI**: Google Jobs search integration
- **EdenAI**: AI-powered keyword extraction
- **GeoConverter**: Coordinate to location code conversion

**Development & Deployment:**
- JUnit 5 for unit testing
- Apache Tomcat 9+ as servlet container
- Environment-based configuration
- Comprehensive logging system

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 11 or higher**
- **Apache Maven 3.6+**
- **MySQL 8.0+**
- **Redis 6.0+**
- **Apache Tomcat 9.0+**

### Required API Keys
- **SerpAPI Key**: [Get your key here](https://serpapi.com/)
- **EdenAI Key**: [Get your key here](https://www.edenai.co/)

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/job-recommendation-system.git
cd job-recommendation-system
```

### 2. Database Setup

```bash
# Start MySQL server
# Create database and user
mysql -u root -p

# Run the initialization script
mysql -u root -p < scripts/init-database.sql
```

### 3. Redis Setup

```bash
# Start Redis server
redis-server

# Test Redis connection
redis-cli ping
```

### 4. Environment Configuration

Run the setup script to configure environment variables:

```bash
# Windows
scripts\setup-env.bat

# Or manually set environment variables:
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=jobrec
set DB_USERNAME=admin
set DB_PASSWORD=your_password
set REDIS_HOST=localhost
set REDIS_PORT=6379
set REDIS_PASSWORD=your_redis_password
set SERPAPI_KEY=your_serpapi_key
set EDENAI_KEY=your_edenai_key
set APP_ENVIRONMENT=dev
```

### 5. Build the Application

```bash
mvn clean compile package
```

### 6. Deploy to Tomcat

```bash
# Copy WAR file to Tomcat webapps directory
cp target/JobSearch-1.0-SNAPSHOT.war $TOMCAT_HOME/webapps/jobrec.war

# Start Tomcat
$TOMCAT_HOME/bin/startup.sh  # Linux/Mac
# or
%TOMCAT_HOME%\bin\startup.bat  # Windows
```

## ⚙️ Configuration

### Application Properties

The application supports environment-specific configuration:

- `application.properties` - Default configuration
- `application-dev.properties` - Development environment
- `application-prod.properties` - Production environment

### Key Configuration Options

```properties
# Database Configuration
db.host=localhost
db.port=3306
db.name=jobrec
db.username=admin

# Redis Configuration
redis.host=localhost
redis.port=6379
redis.ttl=60

# Search Configuration
search.default.keyword=engineer
search.results.limit=20
search.cache.enabled=true

# Logging Configuration
logging.level=INFO
logging.file.enabled=true
logging.console.enabled=true
```

## 🎯 Usage

### Web Interface

1. **Access the Application**: Navigate to `http://localhost:8080/jobrec`

2. **User Registration**: 
   - Click "New User? Register"
   - Fill in username, password, first name, and last name
   - Click "Register"

3. **Login**:
   - Enter your username and password
   - Click "Login"

4. **Search for Jobs**:
   - The system will use your browser's geolocation
   - Browse nearby job listings
   - Use the search functionality for specific keywords

5. **Manage Favorites**:
   - Click the heart icon to favorite/unfavorite jobs
   - View your favorites in the "My Favorites" section

6. **Get Recommendations**:
   - Click "Recommendation" to see personalized job suggestions
   - Recommendations are based on your favorite jobs and keywords

### Sample Users

The database comes with pre-configured test users:

| Username | Password | Description |
|----------|----------|-------------|
| `admin` | `password123` | Administrator account |
| `john_doe` | `password123` | Sample user with favorites |
| `jane_smith` | `password123` | Sample user with applications |
| `test_user` | `password123` | Basic test user |

## 📚 API Documentation

### Authentication Endpoints

#### POST /login
Authenticate user and create session.

**Request:**
```json
{
    "user_id": "john_doe",
    "password": "password123"
}
```

**Response:**
```json
{
    "result": "SUCCESS",
    "name": "John Doe"
}
```

#### POST /register
Register a new user account.

**Request:**
```json
{
    "user_id": "new_user",
    "password": "secure_password",
    "first_name": "John",
    "last_name": "Doe"
}
```

**Response:**
```json
{
    "result": "Registration successful"
}
```

#### GET /logout
Logout user and invalidate session.

**Response:**
```json
{
    "result": "Logout successful"
}
```

### Job Search Endpoints

#### GET /search
Search for jobs based on location and keywords.

**Parameters:**
- `user_id` (required): User identifier
- `lat` (required): Latitude coordinate
- `lon` (required): Longitude coordinate  
- `keyword` (optional): Search keyword

**Example:**
```
GET /search?user_id=john_doe&lat=37.7749&lon=-122.4194&keyword=software engineer
```

**Response:**
```json
[
    {
        "id": "job_12345",
        "title": "Senior Software Engineer",
        "company_name": "TechCorp Inc.",
        "location": "San Francisco, CA",
        "description": "Develop and maintain web applications...",
        "url": "https://example.com/job/12345",
        "keywords": ["Java", "Spring Boot", "REST API"],
        "favorite": false
    }
]
```

### Favorites Management

#### POST /history
Add or remove job from favorites.

**Request:**
```json
{
    "user_id": "john_doe",
    "favorite": [
        {
            "item_id": "job_12345",
            "favorite": true
        }
    ]
}
```

**Response:**
```json
{
    "result": "SUCCESS"
}
```

#### GET /history
Get user's favorite jobs.

**Parameters:**
- `user_id` (required): User identifier

**Response:**
```json
[
    {
        "id": "job_12345",
        "title": "Senior Software Engineer",
        "company_name": "TechCorp Inc.",
        "location": "San Francisco, CA",
        "url": "https://example.com/job/12345",
        "keywords": ["Java", "Spring Boot"],
        "favorite": true
    }
]
```

### Recommendations

#### GET /recommendation
Get personalized job recommendations.

**Parameters:**
- `user_id` (required): User identifier
- `lat` (required): Latitude coordinate
- `lon` (required): Longitude coordinate

**Response:**
```json
[
    {
        "id": "job_67890",
        "title": "Java Developer",
        "company_name": "DevCorp",
        "location": "San Francisco, CA",
        "description": "Work with Java and Spring framework...",
        "url": "https://example.com/job/67890",
        "keywords": ["Java", "Spring"],
        "favorite": false
    }
]
```

## 💻 Development

### Project Structure

```
job-recommendation-system/
├── Source Code/
│   ├── Backend/
│   │   ├── config/           # Configuration management
│   │   ├── db/              # Database connections
│   │   ├── entity/          # Data models
│   │   ├── exception/       # Custom exceptions
│   │   ├── external/        # External API clients
│   │   ├── recommendation/  # Recommendation algorithms
│   │   ├── servlet/         # HTTP request handlers
│   │   ├── test/           # Unit tests
│   │   └── util/           # Utility classes
│   ├── Frontend/
│   │   ├── scripts/        # JavaScript files
│   │   ├── styles/         # CSS files
│   │   ├── WEB-INF/        # Web configuration
│   │   └── index.jsp       # Main page
│   └── resources/          # Configuration files
├── scripts/                # Setup and deployment scripts
├── pom.xml                # Maven configuration
└── README.md              # This file
```

### Code Style Guidelines

- **Java**: Follow Oracle Java Code Conventions
- **Comments**: Use English for all code comments and documentation
- **Naming**: Use descriptive names for classes, methods, and variables
- **Error Handling**: Use structured exception handling with proper logging
- **Security**: Validate and sanitize all user inputs

### Adding New Features

1. **Create Feature Branch**:
   ```bash
   git checkout -b feature/new-feature-name
   ```

2. **Implement Feature**:
   - Add necessary classes in appropriate packages
   - Include comprehensive error handling
   - Add logging statements
   - Write unit tests

3. **Test Thoroughly**:
   ```bash
   mvn test
   ```

4. **Submit Pull Request**:
   - Include description of changes
   - Reference any related issues
   - Ensure all tests pass

## 🧪 Testing

### Running Unit Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ValidationUtilTest

# Run tests with coverage
mvn test jacoco:report
```

### Test Categories

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **API Tests**: Test HTTP endpoints
- **Database Tests**: Test database operations

### Test Data

The application includes comprehensive test data:
- Sample users with different roles
- Mock job listings with various attributes
- Test scenarios for edge cases

## 🚀 Deployment

### Development Deployment

1. **Configure Environment**:
   ```bash
   export APP_ENVIRONMENT=dev
   ```

2. **Start Services**:
   ```bash
   # Start MySQL
   sudo systemctl start mysql
   
   # Start Redis
   sudo systemctl start redis
   
   # Deploy to Tomcat
   mvn clean package
   cp target/*.war $TOMCAT_HOME/webapps/
   ```

### Production Deployment

1. **Environment Setup**:
   ```bash
   export APP_ENVIRONMENT=prod
   export DB_HOST=your-production-db-host
   export REDIS_HOST=your-production-redis-host
   # Set other production environment variables
   ```

2. **Security Configuration**:
   - Use strong passwords for all accounts
   - Configure SSL/TLS certificates
   - Set up firewall rules
   - Enable database encryption

3. **Performance Optimization**:
   - Configure connection pooling
   - Set appropriate cache TTL values
   - Monitor system resources
   - Set up load balancing if needed

4. **Monitoring**:
   - Configure application logs
   - Set up health checks
   - Monitor database performance
   - Track API response times

### Docker Deployment (Optional)

```dockerfile
# Dockerfile example
FROM tomcat:9.0-jdk11-openjdk

# Copy WAR file
COPY target/JobSearch-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/jobrec.war

# Set environment variables
ENV DB_HOST=localhost
ENV REDIS_HOST=localhost

# Expose port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
```

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### How to Contribute

1. **Fork the Repository**
2. **Create Feature Branch**: `git checkout -b feature/amazing-feature`
3. **Commit Changes**: `git commit -m 'Add amazing feature'`
4. **Push to Branch**: `git push origin feature/amazing-feature`
5. **Open Pull Request**

### Contribution Guidelines

- Follow existing code style and conventions
- Add tests for new functionality
- Update documentation as needed
- Ensure all tests pass
- Write clear commit messages

### Bug Reports

When reporting bugs, please include:
- Detailed description of the issue
- Steps to reproduce
- Expected vs actual behavior
- Environment information
- Error logs if applicable

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **SerpAPI** for providing job search data
- **EdenAI** for AI-powered keyword extraction
- **Bootstrap** for responsive UI components
- **Apache Tomcat** for servlet container
- **MySQL** for reliable data storage
- **Redis** for high-performance caching
---

**Made with ❤️ by Leo Ji**

*Last updated: December 2024*