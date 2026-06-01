// MySQLDBUtil.java
// Database configuration utility class for MySQL connection parameters.
// Contains all necessary connection settings for the job recommendation system database.
// 
// Configuration includes:
// - Database instance endpoint
// - Port number (default MySQL port 3306)
// - Database name
// - Authentication credentials
// - Connection parameters (auto-reconnect, timezone)
//
// Author: Leo Ji
// Date: 2024

package com.example.jobrec.db;

/**
 * MySQLDBUtil provides database configuration constants for MySQL connections.
 * All database connection parameters are centralized here for easy maintenance.
 * 
 * Usage: Used by MySQLConnection class to establish database connections.
 */
public class MySQLDBUtil {
    /** Database instance endpoint (replace with your actual instance) */
    private static final String INSTANCE = "YOUR_AMAZON_DB_INSTANCE";
    
    /** MySQL port number (default 3306) */
    private static final String PORT_NUM = "3306";
    
    /** Database name for the job recommendation system */
    public static final String DB_NAME = "YOUR_DB_NAME";
    
    /** Database username */
    private static final String USERNAME = "admin";
    
    /** Database password (replace with your actual password) */
    private static final String PASSWORD = "YOUR_PASSWORD";
    
    /** Complete JDBC URL with connection parameters */
    public static final String URL = "jdbc:mysql://"
            + INSTANCE + ":" + PORT_NUM + "/" + DB_NAME
            + "?user=" + USERNAME + "&password=" + PASSWORD
            + "&autoReconnect=true&serverTimezone=UTC";
}
