// MySQLTableCreation.java
// Database schema creation utility for the job recommendation system.
// This class creates all necessary tables and initial data for the application.
//
// Tables created:
// - users: User account information (user_id, password, first_name, last_name)
// - items: Job listings (item_id, name, address, url)
// - keywords: Job keywords (item_id, keyword) - many-to-many relationship
// - history: User favorites (user_id, item_id, timestamp)
//
// Features:
// - Drops existing tables to ensure clean setup
// - Creates tables with proper foreign key relationships
// - Inserts sample user data for testing
//
// Author: Leo Ji


package com.example.jobrec.db;

import java.sql.*;

/**
 * MySQLTableCreation handles database schema initialization.
 * This utility class creates all required tables and relationships
 * for the job recommendation system.
 * 
 * Run this class once to set up the database schema.
 */
public class MySQLTableCreation {
    /**
     * Main method to initialize database schema.
     * Creates all tables and inserts sample data.
     */
    public static void main(String[] args) {
        try {
            // Step 1: Establish MySQL connection
            System.out.println("Connecting to " + MySQLDBUtil.URL);
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            Connection conn = DriverManager.getConnection(MySQLDBUtil.URL);

            if (conn == null) {
                System.err.println("Failed to establish database connection");
                return;
            }
            System.out.println("Database connection established successfully");

            // Step 2: Drop existing tables (in correct order due to foreign keys)
            Statement statement = conn.createStatement();
            System.out.println("Dropping existing tables...");
            
            String sql = "DROP TABLE IF EXISTS keywords";
            statement.executeUpdate(sql);
            System.out.println("Dropped keywords table");

            sql = "DROP TABLE IF EXISTS history";
            statement.executeUpdate(sql);
            System.out.println("Dropped history table");

            sql = "DROP TABLE IF EXISTS items";
            statement.executeUpdate(sql);
            System.out.println("Dropped items table");

            sql = "DROP TABLE IF EXISTS users";
            statement.executeUpdate(sql);
            System.out.println("Dropped users table");


            // Step 3: Create new tables with proper schema
            System.out.println("Creating new tables...");
            
            // Create items table - stores job listings
            sql = "CREATE TABLE items ("
                    + "item_id VARCHAR(255) NOT NULL,"
                    + "name VARCHAR(255),"
                    + "address VARCHAR(255),"
                    + "url VARCHAR(255),"
                    + "PRIMARY KEY (item_id)"
                    + ")";
            statement.executeUpdate(sql);
            System.out.println("Created items table");

            // Create users table - stores user account information
            sql = "CREATE TABLE users ("
                    + "user_id VARCHAR(255) NOT NULL,"
                    + "password VARCHAR(255) NOT NULL,"
                    + "first_name VARCHAR(255),"
                    + "last_name VARCHAR(255),"
                    + "PRIMARY KEY (user_id)"
                    + ")";
            statement.executeUpdate(sql);
            System.out.println("Created users table");

            // Create keywords table - stores job keywords (many-to-many relationship)
            sql = "CREATE TABLE keywords ("
                    + "item_id VARCHAR(255) NOT NULL,"
                    + "keyword VARCHAR(255) NOT NULL,"
                    + "PRIMARY KEY (item_id, keyword)," // Composite primary key
                    + "FOREIGN KEY (item_id) REFERENCES items(item_id)"
                    + ")";
            statement.executeUpdate(sql);
            System.out.println("Created keywords table");

            // Create history table - stores user favorite items
            sql = "CREATE TABLE history ("
                    + "user_id VARCHAR(255) NOT NULL,"
                    + "item_id VARCHAR(255) NOT NULL,"
                    + "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "PRIMARY KEY (user_id, item_id),"
                    + "FOREIGN KEY (user_id) REFERENCES users(user_id),"
                    + "FOREIGN KEY (item_id) REFERENCES items(item_id)"
                    + ")";
            statement.executeUpdate(sql);
            System.out.println("Created history table");

            // Step 4: Insert sample user data for testing
            // Password is MD5 hashed: original password is "secret"
            System.out.println("Inserting sample data...");
            sql = "INSERT INTO users VALUES('1111', '3229c1097c00d497a0fd282d586be050', 'John', 'Smith')";
            statement.executeUpdate(sql);
            System.out.println("Inserted sample user: 1111 (John Smith)");

            statement.close();
            conn.close();
            System.out.println("Database schema creation completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error creating database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

}



