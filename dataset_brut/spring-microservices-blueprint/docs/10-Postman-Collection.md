# 10 - Postman Collection Guide

🚀 **Complete API testing with the provided Postman collection**

## 📥 Import Collection

The project includes a ready-to-use Postman collection: `spring microservices.postman_collection.json`

### Import Steps
1. **Open Postman**
2. **Click Import** (top left)
3. **Select the file:** `spring microservices.postman_collection.json`
4. **Click Import**

## 📋 Collection Overview

The collection includes all endpoints organized by functionality:

### 🔐 Authentication Endpoints
- **Sign up** - Register new users (with role selection)
- **Sign in** - Login and get JWT token (auto-saves token)

### 👤 User Management
- **Get user by id** - Fetch user profile by ID
- **Get current user** - Get authenticated user's profile
- **Test all** - Public test endpoint
- **User content** - User-only test endpoint

### 🛍️ Product Operations
- **Add product (only ADMIN)** - Create new products (admin required)
- **Get product by created user** - Fetch products by user ID
- **Get product created by me** - Get current user's products
- **Search product** - Search with various filters
- **Generate random user using kafka** - Trigger Kafka events

## 🔧 Setup Environment Variables

The collection uses variables for easy configuration:

### Global Variables (Auto-set)
- `token` - JWT token (automatically captured from login)
- `authHeader` - Bearer token header (auto-generated)
- `jwt` - Mirror of token for compatibility

### Manual Variables (Optional)
Create a Postman environment with:
```json
{
  "account_base_url": "http://localhost:8088",
  "product_base_url": "http://localhost:8089",
  "admin_username": "vito",
  "admin_password": "123456"
}
```

## 🎯 Testing Workflow

### 1. Start Services
```bash
mvn clean install -DskipTests
docker compose up -d --build
```

### 2. Basic Authentication Flow

#### Register New User
- **Endpoint:** `Sign up`
- **Method:** POST
- **Body:**
```json
{
    "username": "vito",
    "email": "vito@example.com",
    "password": "secret123",
    "roles": ["ADMIN"]
}
```
- **Note:** Leave `roles` empty for regular user, use `["ADMIN"]` for admin

#### Login User
- **Endpoint:** `Sign in`
- **Method:** POST
- **Body:**
```json
{
    "username": "vito1",
    "password": "secret123"
}
```
- **Auto-magic:** Token is automatically captured and saved for other requests

### 3. Test User Endpoints

#### Get Current User Profile
- **Endpoint:** `Get current user`
- **Auth:** Uses saved token automatically
- **Expected:** User profile with roles

#### Get User by ID
- **Endpoint:** `Get user by id`
- **URL:** `http://localhost:8088/api/user/1`
- **Auth:** Not required (public endpoint)

### 4. Test Product Endpoints

#### Search Products (Public)
- **Endpoint:** `Search product`
- **URL:** `http://localhost:8089/api/product/search?creatorId=1`
- **Auth:** Not required
- **Filters available:**
  - `name` - Product name
  - `creatorId` - User who created
  - `minPrice` / `maxPrice` - Price range
  - `page` / `size` - Pagination

#### Create Product (Admin Only)
- **Endpoint:** `Add product (only ADMIN)`
- **Auth:** Requires admin token
- **Body:**
```json
{
    "name": "My New Product",
    "description": "A product created via API",
    "price": 199.99
}
```

#### Get Products by User
- **Endpoint:** `Get product by created user`
- **URL:** `http://localhost:8089/api/product?userId=1`
- **Auth:** Not required

## 🧪 Advanced Testing Scenarios

### Scenario 1: Complete User Journey
1. **Sign up** → Register new user
2. **Sign in** → Login and capture token
3. **Get current user** → Verify profile
4. **Search product** → Browse available products
5. **Get product created by me** → View own products (if any)

### Scenario 2: Admin Operations
1. **Sign up** → Register admin user with `"roles": ["ADMIN"]`
2. **Sign in** → Login as admin
3. **Add product** → Create new product
4. **Search product** → Verify product appears in search

### Scenario 3: Error Testing
1. **Sign in** → Try invalid credentials
2. **Get current user** → Try without token
3. **Add product** → Try with regular user token (should fail)

## 🔍 Request Details

### Authentication Headers
The collection automatically handles authentication:
- **Bearer Token:** Uses `{{token}}` variable
- **Auto-capture:** Login response automatically saves token
- **Auto-apply:** Subsequent requests use saved token

### Correlation IDs
Some requests include correlation ID headers for tracing:
```
correlation_id: lodash129182112
```

### Content Types
All POST requests use:
```
Content-Type: application/json
```

## 📊 Response Examples

### Successful Login Response
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "id": 10,
    "username": "john_doe",
    "email": "john@example.com",
    "roles": ["ROLE_USER"]
}
```

### User Profile Response
```json
{
    "id": 1,
    "username": "wyatt.ratke",
    "email": "chase.waelchi@yahoo.com",
    "roles": []
}
```

### Product Search Response
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

## 🚨 Common Issues & Solutions

### Issue: Token not captured
**Solution:**
1. Check the `Sign in` request has the test script
2. Manually copy token from response
3. Set as global variable: `pm.globals.set("token", "your_token_here")`

### Issue: 401 Unauthorized
**Solutions:**
1. Ensure you're logged in first
2. Check token is set: `{{token}}`
3. Verify token hasn't expired (24 hours default)

### Issue: 403 Forbidden
**Solutions:**
1. Check user has required role (ADMIN for product creation)
2. Re-register with correct roles: `"roles": ["ADMIN"]`

### Issue: Connection refused
**Solutions:**
1. Ensure services are running: `docker compose ps`
2. Check correct ports: 8088 (account), 8089 (product)
3. Wait for services to be healthy

## 🔄 Collection Maintenance

### Update Base URLs
If running on different ports/hosts:
1. **Create environment** in Postman
2. **Set variables:**
   - `account_base_url`: Your account service URL
   - `product_base_url`: Your product service URL
3. **Update requests** to use `{{account_base_url}}` and `{{product_base_url}}`

### Add New Endpoints
When adding new API endpoints:
1. **Duplicate similar request**
2. **Update URL and method**
3. **Modify body/headers as needed**
4. **Add to appropriate folder**

## 🎯 Testing Best Practices

### Pre-request Scripts
Use for setup:
```javascript
// Set correlation ID
pm.request.headers.add({
    key: 'correlation_id',
    value: 'test_' + Date.now()
});
```

### Test Scripts
Use for validation:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has token", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.token).to.not.be.empty;
});
```

### Environment Management
- **Development:** localhost URLs
- **Staging:** staging server URLs  
- **Production:** production URLs (be careful!)

## 📈 Collection Runner

For automated testing:
1. **Select collection**
2. **Click Runner**
3. **Choose environment**
4. **Set iterations**
5. **Run collection**

This will execute all requests in sequence and show results.

## 🔗 Integration with CI/CD

Export collection and environment for automated testing:
```bash
# Run with Newman (Postman CLI)
npm install -g newman

newman run "spring microservices.postman_collection.json" \
  --environment "microservices-env.json" \
  --reporters cli,json
```

---

**Happy API Testing! 🧪**

Next: [03-API-Reference.md](03-API-Reference.md) for detailed API documentation