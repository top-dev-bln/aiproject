// ApplicationConfig.java
// Centralized configuration management for the job recommendation system.
// Handles all application configuration including database connections,
// external API keys, cache settings, and environment-specific parameters.
//
// Features:
// - Environment-based configuration (dev, staging, production)
// - Secure handling of sensitive information
// - Centralized configuration constants
// - Easy configuration updates and maintenance
//
// Author: Leo Ji

package com.example.jobrec.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ApplicationConfig provides centralized configuration management.
 * Loads configuration from properties files and environment variables
 * for secure and flexible deployment across different environments.
 */
public class ApplicationConfig {
    private static final Properties properties = new Properties();
    private static ApplicationConfig instance;
    
    // Configuration file names
    private static final String DEFAULT_CONFIG = "application.properties";
    private static final String DEV_CONFIG = "application-dev.properties";
    private static final String PROD_CONFIG = "application-prod.properties";
    
    // Environment detection
    private static final String ENVIRONMENT = System.getProperty("app.environment", "dev");
    
    static {
        loadConfiguration();
    }
    
    private ApplicationConfig() {}
    
    public static ApplicationConfig getInstance() {
        if (instance == null) {
            synchronized (ApplicationConfig.class) {
                if (instance == null) {
                    instance = new ApplicationConfig();
                }
            }
        }
        return instance;
    }
    
    /**
     * Loads configuration from properties files based on environment.
     */
    private static void loadConfiguration() {
        try {
            // Load default configuration
            loadPropertiesFile(DEFAULT_CONFIG);
            
            // Load environment-specific configuration
            String configFile = "dev".equals(ENVIRONMENT) ? DEV_CONFIG : PROD_CONFIG;
            loadPropertiesFile(configFile);
            
        } catch (Exception e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
            // Use fallback default values
            setDefaultValues();
        }
    }
    
    private static void loadPropertiesFile(String filename) {
        try (InputStream input = ApplicationConfig.class.getClassLoader().getResourceAsStream(filename)) {
            if (input != null) {
                properties.load(input);
                System.out.println("Loaded configuration from: " + filename);
            }
        } catch (IOException e) {
            System.err.println("Could not load " + filename + ": " + e.getMessage());
        }
    }
    
    private static void setDefaultValues() {
        // Database defaults
        properties.setProperty("db.host", "localhost");
        properties.setProperty("db.port", "3306");
        properties.setProperty("db.name", "jobrec");
        properties.setProperty("db.username", "admin");
        
        // Redis defaults
        properties.setProperty("redis.host", "localhost");
        properties.setProperty("redis.port", "6379");
        properties.setProperty("redis.ttl", "10");
        
        // API defaults
        properties.setProperty("serpapi.base.url", "https://serpapi.com/search");
        properties.setProperty("search.default.keyword", "engineer");
        properties.setProperty("search.results.limit", "20");
        
        System.out.println("Using default configuration values");
    }
    
    // Database Configuration
    public String getDatabaseHost() {
        return getProperty("db.host", "localhost");
    }
    
    public String getDatabasePort() {
        return getProperty("db.port", "3306");
    }
    
    public String getDatabaseName() {
        return getProperty("db.name", "jobrec");
    }
    
    public String getDatabaseUsername() {
        return getProperty("db.username", "admin");
    }
    
    public String getDatabasePassword() {
        return getProperty("db.password", System.getenv("DB_PASSWORD"));
    }
    
    public String getDatabaseUrl() {
        return String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&autoReconnect=true&serverTimezone=UTC",
                getDatabaseHost(), getDatabasePort(), getDatabaseName(), 
                getDatabaseUsername(), getDatabasePassword());
    }
    
    // Redis Configuration
    public String getRedisHost() {
        return getProperty("redis.host", "localhost");
    }
    
    public int getRedisPort() {
        return Integer.parseInt(getProperty("redis.port", "6379"));
    }
    
    public String getRedisPassword() {
        return getProperty("redis.password", System.getenv("REDIS_PASSWORD"));
    }
    
    public int getRedisTTL() {
        return Integer.parseInt(getProperty("redis.ttl", "10"));
    }
    
    // API Configuration
    public String getSerpApiKey() {
        return getProperty("serpapi.key", System.getenv("SERPAPI_KEY"));
    }
    
    public String getSerpApiBaseUrl() {
        return getProperty("serpapi.base.url", "https://serpapi.com/search");
    }
    
    public String getEdenAiApiKey() {
        return getProperty("edenai.key", System.getenv("EDENAI_KEY"));
    }
    
    // Search Configuration
    public String getDefaultSearchKeyword() {
        return getProperty("search.default.keyword", "engineer");
    }
    
    public int getSearchResultsLimit() {
        return Integer.parseInt(getProperty("search.results.limit", "20"));
    }
    
    // Application Configuration
    public String getEnvironment() {
        return ENVIRONMENT;
    }
    
    public boolean isProduction() {
        return "prod".equals(ENVIRONMENT);
    }
    
    public boolean isDevelopment() {
        return "dev".equals(ENVIRONMENT);
    }
    
    // Generic property getter with fallback
    private String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
    
    /**
     * Validates that all required configuration is present.
     * @return true if configuration is valid, false otherwise
     */
    public boolean validateConfiguration() {
        boolean isValid = true;
        
        // Check database configuration
        if (getDatabasePassword() == null) {
            System.err.println("Database password not configured");
            isValid = false;
        }
        
        // Check API keys
        if (getSerpApiKey() == null) {
            System.err.println("SerpAPI key not configured");
            isValid = false;
        }
        
        if (getEdenAiApiKey() == null) {
            System.err.println("EdenAI key not configured");
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Prints current configuration (excluding sensitive data).
     */
    public void printConfiguration() {
        System.out.println("=== Application Configuration ===");
        System.out.println("Environment: " + getEnvironment());
        System.out.println("Database Host: " + getDatabaseHost());
        System.out.println("Database Port: " + getDatabasePort());
        System.out.println("Database Name: " + getDatabaseName());
        System.out.println("Redis Host: " + getRedisHost());
        System.out.println("Redis Port: " + getRedisPort());
        System.out.println("Redis TTL: " + getRedisTTL());
        System.out.println("Default Search Keyword: " + getDefaultSearchKeyword());
        System.out.println("Search Results Limit: " + getSearchResultsLimit());
        System.out.println("=================================");
    }
}
