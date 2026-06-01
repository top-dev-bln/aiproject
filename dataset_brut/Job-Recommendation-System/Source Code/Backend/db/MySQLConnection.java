// MySQLConnection.java
// This class provides database connection and operations for the job recommendation system.
// It handles user authentication, favorite items management, and job data persistence.
// All database operations use prepared statements to prevent SQL injection attacks.
//
// Features:
// - User login verification
// - User registration
// - Favorite items management (add/remove/list)
// - Job items storage and retrieval
// - Keywords management
//
// Author: Leo Ji

package com.example.jobrec.db;

import com.example.jobrec.entity.Item;
import com.example.jobrec.config.ApplicationConfig;
import com.example.jobrec.util.Logger;

import java.sql.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * MySQLConnection handles all database operations for the job recommendation system.
 * This class manages connections to MySQL database and provides methods for:
 * - User authentication and registration
 * - Job items storage and retrieval  
 * - Favorite items management
 * - Keywords extraction and storage
 */
public class MySQLConnection {
    private Connection conn;

    /**
     * Constructor - establishes connection to MySQL database.
     * Uses configuration from ApplicationConfig for connection parameters.
     */
    public MySQLConnection() {
        try {
            Logger.info(MySQLConnection.class, "Establishing database connection");
            // Load MySQL JDBC driver and establish connection
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            
            ApplicationConfig config = ApplicationConfig.getInstance();
            conn = DriverManager.getConnection(config.getDatabaseUrl());
            
            Logger.info(MySQLConnection.class, "Database connection established successfully");
        } catch (Exception e) {
            Logger.error(MySQLConnection.class, "Failed to establish database connection", e);
        }
    }

    /**
     * Closes the database connection.
     * Should be called after finishing database operations to prevent connection leaks.
     */
    public void close() {
        if (conn != null) {
            try {
                conn.close();
                Logger.debug(MySQLConnection.class, "Database connection closed");
            } catch (Exception e) {
                Logger.error(MySQLConnection.class, "Error closing database connection", e);
            }
        }
    }
    /**
     * Saves a job item to the database.
     * Uses INSERT IGNORE to prevent duplicate entries.
     * @param item The job item to save
     */
    public void saveItem(Item item) {
        if (conn == null) {
            System.err.println("Database connection failed - cannot save item");
            return;
        }
        
        // Insert item into items table (ignore if already exists)
        String insertItemSql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(insertItemSql);
            statement.setString(1, item.getId());
            statement.setString(2, item.getTitle());
            statement.setString(3, item.getLocation());
            statement.setString(4, item.getUrl());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving item: " + e.getMessage());
            e.printStackTrace();
        }

        // Insert keywords associated with this item
        String insertKeywordSql = "INSERT IGNORE INTO keywords VALUES (?, ?)";
        try {
            PreparedStatement keywordStatement = conn.prepareStatement(insertKeywordSql);
            for (String keyword : item.getKeywords()) {
                keywordStatement.setString(1, item.getId());
                keywordStatement.setString(2, keyword);
                keywordStatement.executeUpdate();
            }
            keywordStatement.close();
        } catch (SQLException e) {
            System.err.println("Error saving keywords: " + e.getMessage());
            e.printStackTrace();
        }

    }
    /**
     * Adds an item to user's favorites list.
     * First saves the item to database, then creates favorite relationship.
     * @param userId The user ID
     * @param item The job item to favorite
     */
    public void setFavoriteItems(String userId, Item item) {
        if (conn == null) {
            System.err.println("Database connection failed - cannot set favorite");
            return;
        }
        
        // Save item to database first
        saveItem(item);
        
        // Create favorite relationship (timestamp auto-generated)
        String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, item.getId());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error setting favorite item: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Removes an item from user's favorites list.
     * Only removes the favorite relationship, keeps the item in database.
     * @param userId The user ID
     * @param itemId The job item ID to unfavorite
     */
    public void unsetFavoriteItems(String userId, String itemId) {
        if (conn == null) {
            System.err.println("Database connection failed - cannot unset favorite");
            return;
        }
        
        // Remove favorite relationship (keep item in database)
        String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, itemId);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error removing favorite item: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Retrieves all favorite item IDs for a specific user.
     * @param userId The user ID
     * @return Set of favorite item IDs
     */
    public Set<String> getFavoriteItemIds(String userId) {
        if (conn == null) {
            System.err.println("Database connection failed - cannot get favorite IDs");
            return new HashSet<>();
        }

        Set<String> favoriteItems = new HashSet<>();
        String sql = "SELECT item_id FROM history WHERE user_id = ?";
        
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String itemId = rs.getString("item_id");
                favoriteItems.add(itemId);
            }
            
            rs.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error getting favorite item IDs: " + e.getMessage());
            e.printStackTrace();
        }

        return favoriteItems;
    }

    /**
     * Retrieves complete favorite items data for a specific user.
     * @param userId The user ID
     * @return Set of complete Item objects marked as favorites
     */
    public Set<Item> getFavoriteItems(String userId) {
        if (conn == null) {
            System.err.println("Database connection failed - cannot get favorite items");
            return new HashSet<>();
        }
        
        Set<Item> favoriteItems = new HashSet<>();
        Set<String> favoriteItemIds = getFavoriteItemIds(userId);

        String sql = "SELECT * FROM items WHERE item_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (String itemId : favoriteItemIds) {
                statement.setString(1, itemId);
                ResultSet rs = statement.executeQuery();
                
                if (rs.next()) {
                    // Create Item object with database data
                    favoriteItems.add(new Item(
                            rs.getString("item_id"),
                            rs.getString("name"),
                            null, // company_name not stored in this table
                            rs.getString("address"),
                            null, // via not stored
                            null, // description not stored
                            null, // job_highlights not stored
                            rs.getString("url"),
                            getKeywords(itemId),
                            true // marked as favorite
                    ));
                }
                rs.close();
            }
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error getting favorite items: " + e.getMessage());
            e.printStackTrace();
        }
        return favoriteItems;
    }
    /**
     * Retrieves all keywords associated with a specific item.
     * @param itemId The job item ID
     * @return Set of keywords for the item
     */
    public Set<String> getKeywords(String itemId) {
        if (conn == null) {
            System.err.println("Database connection failed - cannot get keywords");
            return Collections.emptySet();
        }
        
        Set<String> keywords = new HashSet<>();
        String sql = "SELECT keyword FROM keywords WHERE item_id = ?";
        
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, itemId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String keyword = rs.getString("keyword");
                keywords.add(keyword);
            }
            
            rs.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error getting keywords: " + e.getMessage());
            e.printStackTrace();
        }
        return keywords;
    }
    /**
     * Retrieves the full name of a user by combining first and last name.
     * @param userId The user ID
     * @return Full name as "FirstName LastName" or empty string if not found
     */
    public String getFullname(String userId) {
        if (conn == null) {
            System.err.println("Database connection failed - cannot get user name");
            return "";
        }
        
        String name = "";
        String sql = "SELECT first_name, last_name FROM users WHERE user_id = ?";
        
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                name = firstName + " " + lastName;
            }
            
            rs.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error getting user full name: " + e.getMessage());
        }
        return name;
    }
    /**
     * Verifies user login credentials against the database.
     * @param userId The user ID to verify
     * @param password The hashed password to verify
     * @return true if credentials are valid, false otherwise
     */
    public boolean verifyLogin(String userId, String password) {
        if (conn == null) {
            System.err.println("Database connection failed - cannot verify login");
            return false;
        }
        
        String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            
            boolean isValid = rs.next();
            
            rs.close();
            statement.close();
            return isValid;
        } catch (SQLException e) {
            System.err.println("Error verifying login: " + e.getMessage());
        }
        return false;
    }
    /**
     * Adds a new user to the database.
     * Uses INSERT IGNORE to prevent duplicate user registrations.
     * @param userId The unique user ID
     * @param password The hashed password
     * @param firstname The user's first name
     * @param lastname The user's last name
     * @return true if user was successfully added, false if user already exists or error occurred
     */
    public boolean addUser(String userId, String password, String firstname, String lastname) {
        if (conn == null) {
            System.err.println("Database connection failed - cannot add user");
            return false;
        }

        String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, password);
            statement.setString(3, firstname);
            statement.setString(4, lastname);

            int rowsAffected = statement.executeUpdate();
            statement.close();
            
            return rowsAffected == 1; // 1 = successful insert, 0 = user already exists
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}

