// ValidationUtilTest.java
// Unit tests for ValidationUtil class.
// Tests input validation methods to ensure security and data integrity.
//
// Author: Leo Ji

package com.example.jobrec.test;

import com.example.jobrec.util.ValidationUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationUtil class.
 * Covers all validation methods with various input scenarios.
 */
public class ValidationUtilTest {
    
    @Test
    public void testIsNotEmpty() {
        // Valid cases
        assertTrue(ValidationUtil.isNotEmpty("valid"));
        assertTrue(ValidationUtil.isNotEmpty("  valid  "));
        
        // Invalid cases
        assertFalse(ValidationUtil.isNotEmpty(null));
        assertFalse(ValidationUtil.isNotEmpty(""));
        assertFalse(ValidationUtil.isNotEmpty("   "));
    }
    
    @Test
    public void testIsValidEmail() {
        // Valid emails
        assertTrue(ValidationUtil.isValidEmail("user@example.com"));
        assertTrue(ValidationUtil.isValidEmail("test.email+tag@domain.co.uk"));
        assertTrue(ValidationUtil.isValidEmail("user123@test-domain.org"));
        
        // Invalid emails
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail("invalid-email"));
        assertFalse(ValidationUtil.isValidEmail("@domain.com"));
        assertFalse(ValidationUtil.isValidEmail("user@"));
        assertFalse(ValidationUtil.isValidEmail("user@domain"));
    }
    
    @Test
    public void testIsValidUsername() {
        // Valid usernames
        assertTrue(ValidationUtil.isValidUsername("user123"));
        assertTrue(ValidationUtil.isValidUsername("test_user"));
        assertTrue(ValidationUtil.isValidUsername("user-name"));
        assertTrue(ValidationUtil.isValidUsername("abc"));
        assertTrue(ValidationUtil.isValidUsername("12345678901234567890")); // 20 chars
        
        // Invalid usernames
        assertFalse(ValidationUtil.isValidUsername(null));
        assertFalse(ValidationUtil.isValidUsername(""));
        assertFalse(ValidationUtil.isValidUsername("ab")); // too short
        assertFalse(ValidationUtil.isValidUsername("123456789012345678901")); // too long
        assertFalse(ValidationUtil.isValidUsername("user@name")); // invalid char
        assertFalse(ValidationUtil.isValidUsername("user name")); // space
    }
    
    @Test
    public void testIsValidPassword() {
        // Valid passwords
        assertTrue(ValidationUtil.isValidPassword("123456"));
        assertTrue(ValidationUtil.isValidPassword("password123"));
        assertTrue(ValidationUtil.isValidPassword("P@ssw0rd!"));
        
        // Invalid passwords
        assertFalse(ValidationUtil.isValidPassword(null));
        assertFalse(ValidationUtil.isValidPassword(""));
        assertFalse(ValidationUtil.isValidPassword("12345")); // too short
    }
    
    @Test
    public void testIsValidName() {
        // Valid names
        assertTrue(ValidationUtil.isValidName("John"));
        assertTrue(ValidationUtil.isValidName("Mary Jane"));
        assertTrue(ValidationUtil.isValidName("O'Connor"));
        assertTrue(ValidationUtil.isValidName("Smith-Jones"));
        assertTrue(ValidationUtil.isValidName("José"));
        
        // Invalid names
        assertFalse(ValidationUtil.isValidName(null));
        assertFalse(ValidationUtil.isValidName(""));
        assertFalse(ValidationUtil.isValidName("John123"));
        assertFalse(ValidationUtil.isValidName("John@Doe"));
        assertFalse(ValidationUtil.isValidName("A".repeat(51))); // too long
    }
    
    @Test
    public void testIsValidKeyword() {
        // Valid keywords
        assertTrue(ValidationUtil.isValidKeyword("engineer"));
        assertTrue(ValidationUtil.isValidKeyword("software developer"));
        assertTrue(ValidationUtil.isValidKeyword("C++"));
        assertTrue(ValidationUtil.isValidKeyword("data-scientist"));
        assertTrue(ValidationUtil.isValidKeyword(null)); // null is allowed
        assertTrue(ValidationUtil.isValidKeyword("")); // empty is allowed
        
        // Invalid keywords
        assertFalse(ValidationUtil.isValidKeyword("A".repeat(101))); // too long
        assertFalse(ValidationUtil.isValidKeyword("keyword@#$%"));
    }
    
    @Test
    public void testIsValidLatitude() {
        // Valid latitudes
        assertTrue(ValidationUtil.isValidLatitude(0.0));
        assertTrue(ValidationUtil.isValidLatitude(90.0));
        assertTrue(ValidationUtil.isValidLatitude(-90.0));
        assertTrue(ValidationUtil.isValidLatitude(45.5));
        assertTrue(ValidationUtil.isValidLatitude(-45.5));
        
        // Invalid latitudes
        assertFalse(ValidationUtil.isValidLatitude(90.1));
        assertFalse(ValidationUtil.isValidLatitude(-90.1));
        assertFalse(ValidationUtil.isValidLatitude(180.0));
        assertFalse(ValidationUtil.isValidLatitude(-180.0));
    }
    
    @Test
    public void testIsValidLongitude() {
        // Valid longitudes
        assertTrue(ValidationUtil.isValidLongitude(0.0));
        assertTrue(ValidationUtil.isValidLongitude(180.0));
        assertTrue(ValidationUtil.isValidLongitude(-180.0));
        assertTrue(ValidationUtil.isValidLongitude(90.5));
        assertTrue(ValidationUtil.isValidLongitude(-90.5));
        
        // Invalid longitudes
        assertFalse(ValidationUtil.isValidLongitude(180.1));
        assertFalse(ValidationUtil.isValidLongitude(-180.1));
        assertFalse(ValidationUtil.isValidLongitude(360.0));
        assertFalse(ValidationUtil.isValidLongitude(-360.0));
    }
    
    @Test
    public void testAreValidCoordinates() {
        // Valid coordinate pairs
        assertTrue(ValidationUtil.areValidCoordinates(0.0, 0.0));
        assertTrue(ValidationUtil.areValidCoordinates(37.7749, -122.4194)); // San Francisco
        assertTrue(ValidationUtil.areValidCoordinates(-33.8688, 151.2093)); // Sydney
        
        // Invalid coordinate pairs
        assertFalse(ValidationUtil.areValidCoordinates(91.0, 0.0)); // invalid lat
        assertFalse(ValidationUtil.areValidCoordinates(0.0, 181.0)); // invalid lon
        assertFalse(ValidationUtil.areValidCoordinates(91.0, 181.0)); // both invalid
    }
    
    @Test
    public void testSanitizeString() {
        // Test XSS prevention
        assertEquals("&lt;script&gt;alert(&#x27;xss&#x27;)&lt;/script&gt;", 
                    ValidationUtil.sanitizeString("<script>alert('xss')</script>"));
        
        // Test quote escaping
        assertEquals("&quot;quoted text&quot;", 
                    ValidationUtil.sanitizeString("\"quoted text\""));
        
        // Test ampersand escaping
        assertEquals("Tom &amp; Jerry", 
                    ValidationUtil.sanitizeString("Tom & Jerry"));
        
        // Test null handling
        assertNull(ValidationUtil.sanitizeString(null));
        
        // Test normal text
        assertEquals("normal text", 
                    ValidationUtil.sanitizeString("normal text"));
    }
    
    @Test
    public void testValidateRegistrationData() {
        // Valid registration data
        ValidationUtil.ValidationResult result = ValidationUtil.validateRegistrationData(
            "john123", "password123", "John", "Doe");
        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
        
        // Invalid registration data
        result = ValidationUtil.validateRegistrationData(
            "ab", "123", "John123", "");
        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().contains("Invalid username"));
        assertTrue(result.getErrors().contains("Invalid password"));
        assertTrue(result.getErrors().contains("Invalid first name"));
        assertTrue(result.getErrors().contains("Invalid last name"));
    }
    
    @Test
    public void testValidateSearchParameters() {
        // Valid search parameters
        ValidationUtil.ValidationResult result = ValidationUtil.validateSearchParameters(
            37.7749, -122.4194, "software engineer");
        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
        
        // Valid with null keyword
        result = ValidationUtil.validateSearchParameters(37.7749, -122.4194, null);
        assertTrue(result.isValid());
        
        // Invalid search parameters
        result = ValidationUtil.validateSearchParameters(91.0, 181.0, "invalid@keyword");
        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().contains("Invalid latitude"));
        assertTrue(result.getErrors().contains("Invalid longitude"));
    }
}
