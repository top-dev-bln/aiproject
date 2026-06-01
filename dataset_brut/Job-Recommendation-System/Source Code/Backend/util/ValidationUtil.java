// ValidationUtil.java
// Input validation utility for the job recommendation system.
// Provides comprehensive validation for user inputs, API parameters,
// and data integrity checks to enhance security and prevent errors.
//
// Features:
// - User input validation (email, password, names)
// - Geographic coordinate validation
// - SQL injection prevention helpers
// - Data sanitization methods
// - Parameter validation for API calls
//
// Author: Leo Ji

package com.example.jobrec.util;

import java.util.regex.Pattern;

/**
 * ValidationUtil provides comprehensive input validation and sanitization.
 * All user inputs should be validated through this utility to ensure
 * security and data integrity.
 */
public class ValidationUtil {
    
    // Regular expression patterns for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_-]{3,20}$"
    );
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z\\s'-]{1,50}$"
    );
    
    private static final Pattern KEYWORD_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9\\s+#.-]{1,100}$"
    );
    
    // Geographic coordinate limits
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    
    /**
     * Validates if a string is not null and not empty after trimming.
     * @param value the string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Validates email address format.
     * @param email the email to validate
     * @return true if valid email format, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validates username format.
     * Must be 3-20 characters, alphanumeric with underscore and hyphen allowed.
     * @param username the username to validate
     * @return true if valid username, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (!isNotEmpty(username)) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }
    
    /**
     * Validates password strength.
     * Must be at least 6 characters long.
     * @param password the password to validate
     * @return true if valid password, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (!isNotEmpty(password)) {
            return false;
        }
        return password.length() >= 6;
    }
    
    /**
     * Validates name format (first name, last name).
     * Allows letters, spaces, apostrophes, and hyphens.
     * @param name the name to validate
     * @return true if valid name, false otherwise
     */
    public static boolean isValidName(String name) {
        if (!isNotEmpty(name)) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }
    
    /**
     * Validates search keyword format.
     * Allows alphanumeric characters, spaces, and common symbols.
     * @param keyword the keyword to validate
     * @return true if valid keyword, false otherwise
     */
    public static boolean isValidKeyword(String keyword) {
        if (keyword == null) {
            return true; // Keywords are optional
        }
        if (keyword.trim().isEmpty()) {
            return true; // Empty keywords are allowed
        }
        return KEYWORD_PATTERN.matcher(keyword.trim()).matches();
    }
    
    /**
     * Validates latitude coordinate.
     * Must be between -90 and 90 degrees.
     * @param latitude the latitude to validate
     * @return true if valid latitude, false otherwise
     */
    public static boolean isValidLatitude(double latitude) {
        return latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE;
    }
    
    /**
     * Validates longitude coordinate.
     * Must be between -180 and 180 degrees.
     * @param longitude the longitude to validate
     * @return true if valid longitude, false otherwise
     */
    public static boolean isValidLongitude(double longitude) {
        return longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE;
    }
    
    /**
     * Validates both latitude and longitude coordinates.
     * @param latitude the latitude coordinate
     * @param longitude the longitude coordinate
     * @return true if both coordinates are valid, false otherwise
     */
    public static boolean areValidCoordinates(double latitude, double longitude) {
        return isValidLatitude(latitude) && isValidLongitude(longitude);
    }
    
    /**
     * Sanitizes string input by removing potentially harmful characters.
     * Prevents basic XSS and injection attacks.
     * @param input the input string to sanitize
     * @return sanitized string
     */
    public static String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove or escape potentially harmful characters
        return input.trim()
                   .replaceAll("<", "&lt;")
                   .replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;")
                   .replaceAll("'", "&#x27;")
                   .replaceAll("&", "&amp;");
    }
    
    /**
     * Validates and sanitizes user ID.
     * @param userId the user ID to validate
     * @return sanitized user ID, or null if invalid
     */
    public static String validateAndSanitizeUserId(String userId) {
        if (!isValidUsername(userId)) {
            return null;
        }
        return sanitizeString(userId);
    }
    
    /**
     * Validates and sanitizes search keyword.
     * @param keyword the keyword to validate
     * @return sanitized keyword, or null if invalid
     */
    public static String validateAndSanitizeKeyword(String keyword) {
        if (!isValidKeyword(keyword)) {
            return null;
        }
        return sanitizeString(keyword);
    }
    
    /**
     * Validates user registration data.
     * @param userId the user ID
     * @param password the password
     * @param firstName the first name
     * @param lastName the last name
     * @return ValidationResult with validation status and messages
     */
    public static ValidationResult validateRegistrationData(String userId, String password, 
                                                           String firstName, String lastName) {
        ValidationResult result = new ValidationResult();
        
        if (!isValidUsername(userId)) {
            result.addError("Invalid username. Must be 3-20 characters, alphanumeric with _ and - allowed.");
        }
        
        if (!isValidPassword(password)) {
            result.addError("Invalid password. Must be at least 6 characters long.");
        }
        
        if (!isValidName(firstName)) {
            result.addError("Invalid first name. Must contain only letters, spaces, apostrophes, and hyphens.");
        }
        
        if (!isValidName(lastName)) {
            result.addError("Invalid last name. Must contain only letters, spaces, apostrophes, and hyphens.");
        }
        
        return result;
    }
    
    /**
     * Validates search parameters.
     * @param latitude the latitude coordinate
     * @param longitude the longitude coordinate
     * @param keyword the search keyword (optional)
     * @return ValidationResult with validation status and messages
     */
    public static ValidationResult validateSearchParameters(double latitude, double longitude, String keyword) {
        ValidationResult result = new ValidationResult();
        
        if (!isValidLatitude(latitude)) {
            result.addError("Invalid latitude. Must be between -90 and 90 degrees.");
        }
        
        if (!isValidLongitude(longitude)) {
            result.addError("Invalid longitude. Must be between -180 and 180 degrees.");
        }
        
        if (!isValidKeyword(keyword)) {
            result.addError("Invalid keyword format.");
        }
        
        return result;
    }
    
    /**
     * Inner class to hold validation results.
     */
    public static class ValidationResult {
        private boolean valid = true;
        private StringBuilder errors = new StringBuilder();
        
        public void addError(String error) {
            valid = false;
            if (errors.length() > 0) {
                errors.append("; ");
            }
            errors.append(error);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrors() {
            return errors.toString();
        }
        
        public boolean hasErrors() {
            return !valid;
        }
    }
}
