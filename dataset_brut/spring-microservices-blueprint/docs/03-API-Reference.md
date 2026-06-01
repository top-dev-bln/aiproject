# 03 - API Reference Guide

📚 **Complete API documentation with examples and testing instructions**

## 🔗 Service Endpoints

- **Account Service:** http://localhost:8088
- **Product Service:** http://localhost:8089

## 🔐 Authentication Overview

Most endpoints require JWT authentication. Get your token by logging in first.

### Quick Auth Flow
```bash
# 1. Register user (optional if user exists)
curl -X POST http://localhost:8088/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "email": "demo@example.com", "password": "password123"}'

# 2. Login to get JWT token
curl -X POST http://localhost:8088/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "password": "password123"}'

# 3. Save token and use in requests
TOKEN="your_jwt_token_here"
curl -H "Authorization: Bearer $TOKEN" http://localhost:8088/api/user/me
```

---

## 👤 Account Service APIs

### 🔐 Authentication Endpoints

#### Register User
```http
POST /api/auth/signup
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "vito",
  "email": "vito@example.com",
  "password": "secret123",
  "roles": ["ADMIN"]
}
```

**Role Options:**
- `[]` or omit - Default USER role
- `["ADMIN"]` - Admin role with full permissions

**Success Response (200):**
```json
{
  "message": "User registered successfully!"
}
```

**Validation Errors (400):**
```json
{
  "statusCode": 400,
  "messages": [
    "username: size must be between 3 and 20",
    "email: must be a well-formed email address"
  ],
  "errorCode": null
}
```

**Example:**
```bash
curl -X POST http://localhost:8088/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "email": "alice@example.com", "password": "password123", "roles": ["ADMIN"]}'
```

#### Login User
```http
POST /api/auth/signin
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "vito1",
  "password": "secret123"
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsInVzZXJJZCI6IjEwIiwiaWF0IjoxNzU3NjAxMzEzLCJleHAiOjE3NTc2ODc3MTN9.vSasdMos4hvnX7x0wKx43ii7uLbTYxeNr7k7-A3D7ws",
  "type": "Bearer",
  "id": 10,
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

**Invalid Credentials (401):**
```json
{
  "statusCode": 401,
  "messages": ["Bad credentials"],
  "errorCode": null
}
```

**Example:**
```bash
curl -X POST http://localhost:8088/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "password": "password123"}'
```

### 👤 User Management Endpoints

#### Get Current User Profile
```http
GET /api/user/me
Authorization: Bearer {token}
```

**Success Response (200):**
```json
{
  "id": 10,
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

**Unauthorized (401):**
```json
{
  "statusCode": 401,
  "messages": ["Unauthorized"],
  "errorCode": null
}
```

**Example:**
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8088/api/user/me
```

#### Get User by ID
```http
GET /api/user/{userId}
```

**Success Response (200):**
```json
{
  "id": 1,
  "username": "wyatt.ratke",
  "email": "chase.waelchi@yahoo.com",
  "roles": []
}
```

**User Not Found (404):**
```json
{
  "statusCode": 404,
  "messages": ["User not found"],
  "errorCode": null
}
```

**Example:**
```bash
curl http://localhost:8088/api/user/1
```

### 🧪 Test Endpoints

#### Public Test Endpoint
```http
GET /api/test/all
```

**Response:** `"Public Content."`

**Example:**
```bash
curl http://localhost:8088/api/test/all
```

#### User Test Endpoint
```http
GET /api/test/user
Authorization: Bearer {token}
```

**Response:** `"User Content."`

**Example:**
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8088/api/test/user
```

#### Admin Test Endpoint
```http
GET /api/test/admin
Authorization: Bearer {admin_token}
```

**Response:** `"Admin Board."`

**Access Denied (403):**
```json
{
  "statusCode": 403,
  "messages": ["Access Denied"],
  "errorCode": null
}
```

---

## 🛍️ Product Service APIs

### 🔍 Product Search (Public Access)

#### Search Products
```http
GET /api/product/search
```

**Query Parameters:**
- `id` - Search by product ID
- `name` - Search by name (partial match, case-insensitive)
- `description` - Search by description (partial match)
- `creatorId` - Filter by creator user ID
- `minPrice` - Minimum price filter
- `maxPrice` - Maximum price filter
- `page` - Page number (default: 0)
- `size` - Page size (default: 10)
- `sortBy` - Sort field (default: createdAt)
- `sortDirection` - Sort direction: asc/desc (default: desc)

**Examples:**
```bash
# Get all products
curl "http://localhost:8089/api/product/search"

# Search by name
curl "http://localhost:8089/api/product/search?name=Sample"

# Search by creator
curl "http://localhost:8089/api/product/search?creatorId=1"

# Price range search
curl "http://localhost:8089/api/product/search?minPrice=50&maxPrice=200"

# Paginated search with sorting
curl "http://localhost:8089/api/product/search?page=0&size=5&sortBy=name&sortDirection=asc"

# Combined filters
curl "http://localhost:8089/api/product/search?name=Product&creatorId=1&minPrice=100&page=0&size=10"
```

**Success Response (200) - Paginated:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Sample Product A",
      "description": "A great product for testing",
      "price": 99.99,
      "creatorId": 1,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "pageable": {
    "sort": [
      {
        "direction": "ASC",
        "property": "name",
        "ignoreCase": false,
        "nullHandling": "NATIVE",
        "ascending": true,
        "descending": false
      }
    ],
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1
}
```

### 🛍️ Product Management

#### Get Products by User ID
```http
GET /api/product?userId={userId}
```

**Success Response (200):**
```json
[
  {
    "id": 1,
    "name": "Sample Product A",
    "price": null,
    "createdBy": 1
  },
  {
    "id": 2,
    "name": "Sample Product B",
    "price": null,
    "createdBy": 1
  }
]
```

**User Not Found (404):**
```json
{
  "statusCode": 404,
  "messages": ["User not found"],
  "errorCode": null
}
```

**Example:**
```bash
curl "http://localhost:8089/api/product?userId=1"
```

#### Get Current User's Products
```http
GET /api/product/me
Authorization: Bearer {token}
```

**Success Response (200):**
```json
[
  {
    "id": 3,
    "name": "My Product",
    "description": "Created by me",
    "price": 199.99,
    "creatorId": 10,
    "createdAt": "2024-01-01T12:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  }
]
```

**Example:**
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8089/api/product/me
```

#### Create Product (Admin Only)
```http
POST /api/product
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "My New Product",
  "description": "A product created via API",
  "price": 199.99
}
```

**Success Response (201):**
```json
{
  "id": 3,
  "name": "My New Product",
  "description": "A product created via API",
  "price": 199.99,
  "creatorId": 2,
  "createdAt": "2024-01-01T12:00:00",
  "updatedAt": "2024-01-01T12:00:00"
}
```

**Access Denied (403):**
```json
{
  "statusCode": 403,
  "messages": ["Access Denied"],
  "errorCode": null
}
```

**Example:**
```bash
curl -X POST http://localhost:8089/api/product \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"name": "My Product", "description": "A great product", "price": 299.99}'
```

#### Update Product
```http
PATCH /api/product/{productId}
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body (partial update):**
```json
{
  "name": "Updated Product Name",
  "price": 299.99
}
```

**Success Response (200):**
```json
{
  "id": 1,
  "name": "Updated Product Name",
  "description": "Original description",
  "price": 299.99,
  "creatorId": 1,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T12:30:00"
}
```

**Authorization Rules:**
- Product creators can update their own products
- Users with ROLE_ADMIN can update any product

**Example:**
```bash
curl -X PATCH http://localhost:8089/api/product/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name": "Updated Name", "price": 399.99}'
```

### 🔄 Kafka Integration

#### Generate Random User (Kafka Event)
```http
POST /api/user
correlation_id: {optional_correlation_id}
```

**Purpose:** Triggers Kafka event for testing event-driven architecture

**Success Response (200):**
```json
{
  "message": "Random user generated and published to Kafka"
}
```

**Example:**
```bash
curl -X POST http://localhost:8089/api/user \
  -H "correlation_id: test123"
```

---

## 🚨 Error Response Format

All APIs return consistent error responses:

```json
{
  "statusCode": 400,
  "messages": ["Error message 1", "Error message 2"],
  "errorCode": "VALIDATION_ERROR"
}
```

### HTTP Status Codes

| Code | Meaning | When |
|------|---------|------|
| **200** | OK | Success |
| **201** | Created | Resource created |
| **400** | Bad Request | Validation errors |
| **401** | Unauthorized | Missing/invalid JWT |
| **403** | Forbidden | Insufficient permissions |
| **404** | Not Found | Resource not found |
| **500** | Internal Server Error | Server error |

### Common Error Examples

#### Validation Error (400)
```bash
curl -X POST http://localhost:8088/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username": "ab", "email": "", "password": "123"}'
```

**Response:**
```json
{
  "statusCode": 400,
  "messages": [
    "username: size must be between 3 and 20",
    "password: size must be between 6 and 40",
    "email: must not be blank"
  ],
  "errorCode": null
}
```

#### Unauthorized (401)
```bash
curl http://localhost:8088/api/user/me
```

**Response:**
```json
{
  "statusCode": 401,
  "messages": ["Unauthorized"],
  "errorCode": null
}
```

#### Forbidden (403)
```bash
# Try to create product with USER role
curl -X POST http://localhost:8089/api/product \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{"name": "Test"}'
```

**Response:**
```json
{
  "statusCode": 403,
  "messages": ["Access Denied"],
  "errorCode": null
}
```

---

## 🧪 Complete Testing Script

```bash
#!/bin/bash

echo "=== Spring Boot Microservices API Testing ==="

BASE_ACCOUNT="http://localhost:8088"
BASE_PRODUCT="http://localhost:8089"

# 1. Register admin user
echo "1. Registering admin user..."
curl -s -X POST $BASE_ACCOUNT/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username": "admin_test", "email": "admin@test.com", "password": "password123", "roles": ["ADMIN"]}'

# 2. Register regular user
echo "2. Registering regular user..."
curl -s -X POST $BASE_ACCOUNT/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username": "user_test", "email": "user@test.com", "password": "password123"}'

# 3. Login as admin
echo "3. Logging in as admin..."
ADMIN_RESPONSE=$(curl -s -X POST $BASE_ACCOUNT/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username": "admin_test", "password": "password123"}')
ADMIN_TOKEN=$(echo $ADMIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 4. Login as user
echo "4. Logging in as user..."
USER_RESPONSE=$(curl -s -X POST $BASE_ACCOUNT/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username": "user_test", "password": "password123"}')
USER_TOKEN=$(echo $USER_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 5. Test user profile
echo "5. Getting user profile..."
curl -s -H "Authorization: Bearer $USER_TOKEN" $BASE_ACCOUNT/api/user/me

# 6. Create product (admin only)
echo "6. Creating product as admin..."
curl -s -X POST $BASE_PRODUCT/api/product \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"name": "Test Product", "description": "Created via API", "price": 99.99}'

# 7. Search products
echo "7. Searching products..."
curl -s "$BASE_PRODUCT/api/product/search?name=Test"

# 8. Get products by user
echo "8. Getting products by user..."
curl -s "$BASE_PRODUCT/api/product?userId=1"

# 9. Test error case
echo "9. Testing error case..."
curl -s "$BASE_PRODUCT/api/product?userId=999"

echo "=== Testing Complete ==="
```

---

## 📚 Related Documentation

- **Postman Collection:** [10-Postman-Collection.md](10-Postman-Collection.md)
- **Testing Guide:** [05-Testing-Guide.md](05-Testing-Guide.md)
- **Development Guide:** [04-Development-Guide.md](04-Development-Guide.md)

---

**Ready to explore the APIs! 🚀**