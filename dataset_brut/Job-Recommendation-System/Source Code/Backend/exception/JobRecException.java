// JobRecException.java
// Custom exception class for the job recommendation system.
// Provides structured error handling with error codes and user-friendly messages.
//
// Features:
// - Categorized error types (DATABASE, API, VALIDATION, etc.)
// - Error codes for programmatic handling
// - User-friendly error messages
// - Exception chaining support
// - Logging integration
//
// Author: Leo Ji

package com.example.jobrec.exception;

/**
 * JobRecException is the base exception class for the job recommendation system.
 * Provides structured error handling with categorized error types and codes.
 */
public class JobRecException extends Exception {
    
    /**
     * Error categories for different types of exceptions.
     */
    public enum ErrorType {
        DATABASE("DB", "Database operation failed"),
        API("API", "External API call failed"),
        VALIDATION("VAL", "Input validation failed"),
        AUTHENTICATION("AUTH", "Authentication failed"),
        AUTHORIZATION("AUTHZ", "Authorization failed"),
        CACHE("CACHE", "Cache operation failed"),
        CONFIGURATION("CFG", "Configuration error"),
        NETWORK("NET", "Network operation failed"),
        INTERNAL("INT", "Internal server error");
        
        private final String code;
        private final String description;
        
        ErrorType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private final ErrorType errorType;
    private final String errorCode;
    private final String userMessage;
    private final String technicalMessage;
    
    /**
     * Constructor with error type and messages.
     * @param errorType the category of error
     * @param userMessage user-friendly error message
     * @param technicalMessage detailed technical message
     */
    public JobRecException(ErrorType errorType, String userMessage, String technicalMessage) {
        super(technicalMessage);
        this.errorType = errorType;
        this.errorCode = generateErrorCode(errorType);
        this.userMessage = userMessage;
        this.technicalMessage = technicalMessage;
    }
    
    /**
     * Constructor with error type, messages, and cause.
     * @param errorType the category of error
     * @param userMessage user-friendly error message
     * @param technicalMessage detailed technical message
     * @param cause the underlying cause
     */
    public JobRecException(ErrorType errorType, String userMessage, String technicalMessage, Throwable cause) {
        super(technicalMessage, cause);
        this.errorType = errorType;
        this.errorCode = generateErrorCode(errorType);
        this.userMessage = userMessage;
        this.technicalMessage = technicalMessage;
    }
    
    /**
     * Constructor with error type and single message.
     * @param errorType the category of error
     * @param message the error message (used for both user and technical)
     */
    public JobRecException(ErrorType errorType, String message) {
        this(errorType, message, message);
    }
    
    /**
     * Constructor with error type, message, and cause.
     * @param errorType the category of error
     * @param message the error message
     * @param cause the underlying cause
     */
    public JobRecException(ErrorType errorType, String message, Throwable cause) {
        this(errorType, message, message, cause);
    }
    
    /**
     * Generates a unique error code based on error type and timestamp.
     * @param errorType the error type
     * @return formatted error code
     */
    private String generateErrorCode(ErrorType errorType) {
        long timestamp = System.currentTimeMillis();
        return String.format("%s-%d", errorType.getCode(), timestamp % 100000);
    }
    
    /**
     * Gets the error type.
     * @return the error type
     */
    public ErrorType getErrorType() {
        return errorType;
    }
    
    /**
     * Gets the unique error code.
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the user-friendly error message.
     * @return the user message
     */
    public String getUserMessage() {
        return userMessage;
    }
    
    /**
     * Gets the technical error message.
     * @return the technical message
     */
    public String getTechnicalMessage() {
        return technicalMessage;
    }
    
    /**
     * Creates a database exception.
     * @param message the error message
     * @return JobRecException with DATABASE type
     */
    public static JobRecException database(String message) {
        return new JobRecException(ErrorType.DATABASE, "Database operation failed", message);
    }
    
    /**
     * Creates a database exception with cause.
     * @param message the error message
     * @param cause the underlying cause
     * @return JobRecException with DATABASE type
     */
    public static JobRecException database(String message, Throwable cause) {
        return new JobRecException(ErrorType.DATABASE, "Database operation failed", message, cause);
    }
    
    /**
     * Creates an API exception.
     * @param message the error message
     * @return JobRecException with API type
     */
    public static JobRecException api(String message) {
        return new JobRecException(ErrorType.API, "External service unavailable", message);
    }
    
    /**
     * Creates an API exception with cause.
     * @param message the error message
     * @param cause the underlying cause
     * @return JobRecException with API type
     */
    public static JobRecException api(String message, Throwable cause) {
        return new JobRecException(ErrorType.API, "External service unavailable", message, cause);
    }
    
    /**
     * Creates a validation exception.
     * @param message the error message
     * @return JobRecException with VALIDATION type
     */
    public static JobRecException validation(String message) {
        return new JobRecException(ErrorType.VALIDATION, message, message);
    }
    
    /**
     * Creates an authentication exception.
     * @param message the error message
     * @return JobRecException with AUTHENTICATION type
     */
    public static JobRecException authentication(String message) {
        return new JobRecException(ErrorType.AUTHENTICATION, "Authentication failed", message);
    }
    
    /**
     * Creates an authorization exception.
     * @param message the error message
     * @return JobRecException with AUTHORIZATION type
     */
    public static JobRecException authorization(String message) {
        return new JobRecException(ErrorType.AUTHORIZATION, "Access denied", message);
    }
    
    /**
     * Creates a cache exception.
     * @param message the error message
     * @return JobRecException with CACHE type
     */
    public static JobRecException cache(String message) {
        return new JobRecException(ErrorType.CACHE, "Cache operation failed", message);
    }
    
    /**
     * Creates a cache exception with cause.
     * @param message the error message
     * @param cause the underlying cause
     * @return JobRecException with CACHE type
     */
    public static JobRecException cache(String message, Throwable cause) {
        return new JobRecException(ErrorType.CACHE, "Cache operation failed", message, cause);
    }
    
    /**
     * Creates a configuration exception.
     * @param message the error message
     * @return JobRecException with CONFIGURATION type
     */
    public static JobRecException configuration(String message) {
        return new JobRecException(ErrorType.CONFIGURATION, "Configuration error", message);
    }
    
    /**
     * Creates an internal server exception.
     * @param message the error message
     * @return JobRecException with INTERNAL type
     */
    public static JobRecException internal(String message) {
        return new JobRecException(ErrorType.INTERNAL, "Internal server error", message);
    }
    
    /**
     * Creates an internal server exception with cause.
     * @param message the error message
     * @param cause the underlying cause
     * @return JobRecException with INTERNAL type
     */
    public static JobRecException internal(String message, Throwable cause) {
        return new JobRecException(ErrorType.INTERNAL, "Internal server error", message, cause);
    }
    
    @Override
    public String toString() {
        return String.format("JobRecException{errorCode='%s', errorType=%s, userMessage='%s', technicalMessage='%s'}", 
                           errorCode, errorType, userMessage, technicalMessage);
    }
}
