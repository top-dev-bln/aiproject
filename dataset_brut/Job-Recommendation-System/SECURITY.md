# 🔒 Security Guidelines and API Key Management

## ⚠️ CRITICAL: API Key Security

### ❌ NEVER commit API keys to version control

**What was fixed:**
- Removed hardcoded SerpAPI key from `SerpAPIClient.java`
- Replaced with secure configuration loading
- All API keys now loaded from environment variables

### ✅ Secure API Key Management

**Current Implementation:**
```java
// ✅ SECURE - Loads from environment variables
private static final String API_KEY = ApplicationConfig.getInstance().getSerpApiKey();

// ❌ INSECURE - Never do this
// private static final String API_KEY = "actual_api_key_here";
```

## 🔑 Required Environment Variables

Set these environment variables before running the application:

```bash
# Database Configuration
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=jobrec
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_secure_db_password

# Redis Configuration
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=your_redis_password

# API Keys (REQUIRED)
export SERPAPI_KEY=your_serpapi_key_here
export EDENAI_KEY=your_edenai_key_here

# Application Environment
export APP_ENVIRONMENT=prod
```

## 🛡️ Security Checklist

### ✅ Completed Security Measures

- [x] **API Key Protection**: All API keys loaded from environment variables
- [x] **Input Validation**: Comprehensive validation in `ValidationUtil`
- [x] **SQL Injection Prevention**: Prepared statements used throughout
- [x] **XSS Protection**: Input sanitization implemented
- [x] **Error Handling**: No sensitive information in error messages
- [x] **Session Management**: Secure session handling
- [x] **Password Security**: MD5 hashing (consider upgrading to bcrypt)
- [x] **Configuration Security**: Sensitive data in environment variables

### 🔄 Recommended Improvements

- [ ] **Password Hashing**: Upgrade from MD5 to bcrypt or Argon2
- [ ] **HTTPS**: Implement SSL/TLS in production
- [ ] **Rate Limiting**: Add API rate limiting
- [ ] **CORS**: Configure proper CORS policies
- [ ] **Security Headers**: Add security headers (CSP, HSTS, etc.)
- [ ] **Audit Logging**: Log security events
- [ ] **Dependency Scanning**: Regular security dependency updates

## 🚨 Security Incident Response

### If API Keys Are Compromised

1. **Immediate Actions:**
   ```bash
   # Revoke compromised keys immediately
   # SerpAPI: Go to https://serpapi.com/manage-api-key
   # EdenAI: Go to https://www.edenai.co/account
   
   # Generate new API keys
   # Update environment variables
   # Restart application
   ```

2. **Investigation:**
   - Check git history for exposed keys
   - Review access logs
   - Identify scope of exposure

3. **Prevention:**
   - Use git-secrets or similar tools
   - Implement pre-commit hooks
   - Regular security audits

### Git History Cleanup

If keys were committed to git:

```bash
# Remove sensitive data from git history
git filter-branch --force --index-filter \
'git rm --cached --ignore-unmatch path/to/file/with/keys' \
--prune-empty --tag-name-filter cat -- --all

# Force push (WARNING: This rewrites history)
git push origin --force --all
git push origin --force --tags
```

## 🔍 Security Monitoring

### Log Monitoring

Monitor these security events:

```bash
# Failed login attempts
grep "Authentication failed" /opt/tomcat/logs/application.log

# Invalid input attempts
grep "validation failed" /opt/tomcat/logs/application.log

# Database errors (potential injection attempts)
grep "SQLException" /opt/tomcat/logs/application.log

# API errors
grep "API call.*failed" /opt/tomcat/logs/application.log
```

### Health Checks

Regular security health checks:

```bash
# Check for hardcoded secrets
grep -r "password\|secret\|key" --include="*.java" src/

# Check for TODO security items
grep -r "TODO.*security\|FIXME.*security" --include="*.java" src/

# Verify environment variables
echo "Checking required environment variables..."
[ -z "$SERPAPI_KEY" ] && echo "❌ SERPAPI_KEY not set"
[ -z "$EDENAI_KEY" ] && echo "❌ EDENAI_KEY not set"
[ -z "$DB_PASSWORD" ] && echo "❌ DB_PASSWORD not set"
```

## 📋 Deployment Security

### Production Environment

```bash
# Set secure permissions
chmod 600 .env
chown app:app .env

# Verify no secrets in code
find . -name "*.java" -exec grep -l "api.*key\|password.*=" {} \;

# Check firewall rules
ufw status

# Verify SSL configuration
openssl s_client -connect localhost:8443 -servername localhost
```

### Environment File Template

Create `.env.template` for team members:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=jobrec
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Redis Configuration  
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# API Keys
SERPAPI_KEY=your_serpapi_key
EDENAI_KEY=your_edenai_key

# Application
APP_ENVIRONMENT=dev
```

## 🎯 Security Best Practices

### Code Review Checklist

- [ ] No hardcoded credentials
- [ ] Input validation on all user inputs
- [ ] Proper error handling without information leakage
- [ ] SQL injection prevention (prepared statements)
- [ ] XSS prevention (input sanitization)
- [ ] Secure session management
- [ ] Proper logging without sensitive data

### Development Guidelines

1. **Never commit secrets**: Use environment variables
2. **Validate all inputs**: Use `ValidationUtil` for all user inputs
3. **Sanitize outputs**: Prevent XSS attacks
4. **Use prepared statements**: Prevent SQL injection
5. **Log securely**: No sensitive data in logs
6. **Handle errors gracefully**: No stack traces to users
7. **Keep dependencies updated**: Regular security updates

## 📞 Security Contact

For security issues:

- **Security Issues**: Create a private issue or contact maintainers
- **Vulnerability Reports**: Follow responsible disclosure
- **Emergency Contact**: Use secure communication channels

---

**Author**: Leo Ji  
**Last Updated**: December 2024  
**Security Review**: Required before each release
