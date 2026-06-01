// RedisConnection.java
// Redis cache management for the job recommendation system.
// Provides caching functionality to improve application performance by storing:
// - Search results (by location and keyword)
// - User favorite items
//
// Features:
// - Automatic cache expiration (10 seconds TTL)
// - Key-value storage for JSON serialized data
// - Search result caching based on location and keywords
// - User favorites caching
//
// Benefits:
// - Reduces external API calls (SerpAPI, EdenAI)
// - Improves response times for repeated requests
// - Reduces database load for favorite items
//
// Author: Leo Ji

package com.example.jobrec.db;

import com.example.jobrec.config.ApplicationConfig;
import com.example.jobrec.util.Logger;
import redis.clients.jedis.Jedis;

/**
 * RedisConnection manages cache operations for the job recommendation system.
 * Uses Redis as an in-memory cache to store frequently accessed data
 * and reduce load on external APIs and database.
 */
public class RedisConnection {
    /** Key template for search results cache */
    private static final String SEARCH_KEY_TEMPLATE = "search:lat=%s&lon=%s&keyword=%s";
    
    /** Key template for user favorites cache */
    private static final String FAVORITE_KEY_TEMPLATE = "history:userId=%s";

    /** Jedis client for Redis operations */
    private Jedis jedis;
    
    /** Application configuration instance */
    private final ApplicationConfig config;

    /**
     * Constructor - establishes connection to Redis server.
     * Uses ApplicationConfig for connection parameters.
     */
    public RedisConnection() {
        config = ApplicationConfig.getInstance();
        try {
            Logger.info(RedisConnection.class, "Establishing Redis connection");
            jedis = new Jedis(config.getRedisHost(), config.getRedisPort());
            
            String password = config.getRedisPassword();
            if (password != null && !password.isEmpty()) {
                jedis.auth(password);
            }
            
            // Test connection
            jedis.ping();
            Logger.info(RedisConnection.class, "Redis connection established successfully");
        } catch (Exception e) {
            Logger.error(RedisConnection.class, "Failed to connect to Redis", e);
            jedis = null;
        }
    }

    /**
     * Closes the Redis connection.
     * Should be called after finishing cache operations.
     */
    public void close() {
        if (jedis != null) {
            try {
                jedis.close();
                Logger.debug(RedisConnection.class, "Redis connection closed");
            } catch (Exception e) {
                Logger.error(RedisConnection.class, "Error closing Redis connection", e);
            }
        }
    }
    /**
     * Retrieves cached search results for a specific location and keyword.
     * @param lat Latitude coordinate
     * @param lon Longitude coordinate
     * @param keyword Search keyword (can be null)
     * @return Cached search results as JSON string, or null if not found
     */
    public String getSearchResult(double lat, double lon, String keyword) {
        if (jedis == null) {
            return null;
        }
        try {
            String key = String.format(SEARCH_KEY_TEMPLATE, lat, lon, keyword);
            String result = jedis.get(key);
            Logger.debug(RedisConnection.class, "Cache " + (result != null ? "HIT" : "MISS") + " for key: " + key);
            return result;
        } catch (Exception e) {
            Logger.error(RedisConnection.class, "Error getting search result from cache", e);
            return null;
        }
    }

    /**
     * Caches search results for a specific location and keyword.
     * Results expire after configured TTL to ensure fresh data.
     * @param lat Latitude coordinate
     * @param lon Longitude coordinate
     * @param keyword Search keyword (can be null)
     * @param value JSON string of search results to cache
     */
    public void setSearchResult(double lat, double lon, String keyword, String value) {
        if (jedis == null) {
            return;
        }
        try {
            String key = String.format(SEARCH_KEY_TEMPLATE, lat, lon, keyword);
            jedis.set(key, value);
            jedis.expire(key, config.getRedisTTL());
            Logger.debug(RedisConnection.class, "Cached search result for key: " + key);
        } catch (Exception e) {
            Logger.error(RedisConnection.class, "Error setting search result in cache", e);
        }
    }

    /**
     * Retrieves cached favorite items for a specific user.
     * @param userId The user ID
     * @return Cached favorite items as JSON string, or null if not found
     */
    public String getFavoriteResult(String userId) {
        if (jedis == null) {
            return null;
        }
        try {
            String key = String.format(FAVORITE_KEY_TEMPLATE, userId);
            return jedis.get(key);
        } catch (Exception e) {
            System.err.println("Error getting favorite result from cache: " + e.getMessage());
            return null;
        }
    }

    /**
     * Caches favorite items for a specific user.
     * Cache expires after 10 seconds to ensure data consistency.
     * @param userId The user ID
     * @param value JSON string of favorite items to cache
     */
    public void setFavoriteResult(String userId, String value) {
        if (jedis == null) {
            return;
        }
        try {
            String key = String.format(FAVORITE_KEY_TEMPLATE, userId);
            jedis.set(key, value);
            jedis.expire(key, 10); // Cache expires in 10 seconds
        } catch (Exception e) {
            System.err.println("Error setting favorite result in cache: " + e.getMessage());
        }
    }

    /**
     * Deletes cached favorite items for a specific user.
     * Called when user's favorites are updated to ensure cache consistency.
     * @param userId The user ID whose cache should be cleared
     */
    public void deleteFavoriteResult(String userId) {
        if (jedis == null) {
            return;
        }
        try {
            String key = String.format(FAVORITE_KEY_TEMPLATE, userId);
            jedis.del(key);
        } catch (Exception e) {
            System.err.println("Error deleting favorite result from cache: " + e.getMessage());
        }
    }
    
    /**
     * Main method for testing Redis connection and operations.
     * Demonstrates basic cache operations: set, get, delete.
     */
    public static void main(String[] args) {
        System.out.println("Testing Redis connection...");
        RedisConnection c = new RedisConnection();
        
        // Test favorite caching
        System.out.println("Testing favorite operations:");
        c.setFavoriteResult("1234", "test_data");
        System.out.println("Get favorite: " + c.getFavoriteResult("1234"));
        c.deleteFavoriteResult("1234");
        System.out.println("After delete: " + c.getFavoriteResult("1234"));
        
        // Test search result caching
        System.out.println("Testing search operations:");
        c.setSearchResult(1, 2, "123", "search_data");
        System.out.println("Get search: " + c.getSearchResult(1, 2, "123"));
        
        c.close();
        System.out.println("Redis testing completed");
    }
}

