// Logger.java
// Centralized logging utility for the job recommendation system.
// Provides structured logging with different levels and formatted output.
//
// Features:
// - Multiple log levels (DEBUG, INFO, WARN, ERROR)
// - Timestamp formatting
// - Class and method context tracking
// - Environment-based log level filtering
// - File and console output support
//
// Author: Leo Ji

package com.example.jobrec.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger provides centralized logging functionality for the application.
 * Supports multiple log levels and formatted output with timestamps.
 */
public class Logger {
    
    public enum Level {
        DEBUG(0), INFO(1), WARN(2), ERROR(3);
        
        private final int value;
        
        Level(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private static final String LOG_FILE = "application.log";
    private static Level currentLevel = Level.INFO;
    private static boolean fileLoggingEnabled = true;
    private static boolean consoleLoggingEnabled = true;
    
    /**
     * Sets the minimum log level for output.
     * @param level minimum level to log
     */
    public static void setLogLevel(Level level) {
        currentLevel = level;
    }
    
    /**
     * Enables or disables file logging.
     * @param enabled true to enable file logging
     */
    public static void setFileLogging(boolean enabled) {
        fileLoggingEnabled = enabled;
    }
    
    /**
     * Enables or disables console logging.
     * @param enabled true to enable console logging
     */
    public static void setConsoleLogging(boolean enabled) {
        consoleLoggingEnabled = enabled;
    }
    
    /**
     * Logs a debug message.
     * @param clazz the calling class
     * @param message the log message
     */
    public static void debug(Class<?> clazz, String message) {
        log(Level.DEBUG, clazz, message, null);
    }
    
    /**
     * Logs an info message.
     * @param clazz the calling class
     * @param message the log message
     */
    public static void info(Class<?> clazz, String message) {
        log(Level.INFO, clazz, message, null);
    }
    
    /**
     * Logs a warning message.
     * @param clazz the calling class
     * @param message the log message
     */
    public static void warn(Class<?> clazz, String message) {
        log(Level.WARN, clazz, message, null);
    }
    
    /**
     * Logs a warning message with exception.
     * @param clazz the calling class
     * @param message the log message
     * @param throwable the exception to log
     */
    public static void warn(Class<?> clazz, String message, Throwable throwable) {
        log(Level.WARN, clazz, message, throwable);
    }
    
    /**
     * Logs an error message.
     * @param clazz the calling class
     * @param message the log message
     */
    public static void error(Class<?> clazz, String message) {
        log(Level.ERROR, clazz, message, null);
    }
    
    /**
     * Logs an error message with exception.
     * @param clazz the calling class
     * @param message the log message
     * @param throwable the exception to log
     */
    public static void error(Class<?> clazz, String message, Throwable throwable) {
        log(Level.ERROR, clazz, message, throwable);
    }
    
    /**
     * Internal logging method that handles formatting and output.
     */
    private static void log(Level level, Class<?> clazz, String message, Throwable throwable) {
        if (level.getValue() < currentLevel.getValue()) {
            return; // Skip if below current log level
        }
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String className = clazz.getSimpleName();
        String logMessage = String.format("[%s] %s %s: %s", 
            timestamp, level.name(), className, message);
        
        // Output to console
        if (consoleLoggingEnabled) {
            if (level == Level.ERROR || level == Level.WARN) {
                System.err.println(logMessage);
            } else {
                System.out.println(logMessage);
            }
            
            if (throwable != null) {
                throwable.printStackTrace();
            }
        }
        
        // Output to file
        if (fileLoggingEnabled) {
            writeToFile(logMessage, throwable);
        }
    }
    
    /**
     * Writes log message to file.
     */
    private static void writeToFile(String message, Throwable throwable) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(message);
            if (throwable != null) {
                throwable.printStackTrace(writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
    
    /**
     * Logs method entry for debugging.
     * @param clazz the calling class
     * @param methodName the method name
     */
    public static void entering(Class<?> clazz, String methodName) {
        debug(clazz, "Entering method: " + methodName);
    }
    
    /**
     * Logs method exit for debugging.
     * @param clazz the calling class
     * @param methodName the method name
     */
    public static void exiting(Class<?> clazz, String methodName) {
        debug(clazz, "Exiting method: " + methodName);
    }
    
    /**
     * Logs method execution time.
     * @param clazz the calling class
     * @param methodName the method name
     * @param startTime start time in milliseconds
     */
    public static void logExecutionTime(Class<?> clazz, String methodName, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        debug(clazz, String.format("Method %s executed in %d ms", methodName, duration));
    }
    
    /**
     * Logs database operation.
     * @param clazz the calling class
     * @param operation the database operation
     * @param success whether the operation was successful
     */
    public static void logDatabaseOperation(Class<?> clazz, String operation, boolean success) {
        String status = success ? "SUCCESS" : "FAILED";
        info(clazz, String.format("Database operation [%s]: %s", operation, status));
    }
    
    /**
     * Logs API call.
     * @param clazz the calling class
     * @param apiName the API name
     * @param url the API URL
     * @param responseCode the HTTP response code
     */
    public static void logApiCall(Class<?> clazz, String apiName, String url, int responseCode) {
        info(clazz, String.format("API call [%s] to %s returned %d", apiName, url, responseCode));
    }
}
