package com.demo.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.demo.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

/**
 * Utility class for authentication-related operations.
 * Provides methods to extract user information from Spring Security context.
 */

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Component
public class AuthUtils {

    private AuthUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Gets the current authenticated user's ID from the security context.
     * 
     * @return the user ID
     * @throws RuntimeException if user is not authenticated
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Long) {
            return (Long) authentication.getDetails();
        }
        throw new UnauthorizedException("User not authenticated");
    }

    /**
     * Gets the current authenticated user's username from the security context.
     * 
     * @return the username
     * @throws RuntimeException if user is not authenticated
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName();
        }
        throw new UnauthorizedException("User not authenticated");
    }

    /**
     * Checks if the current authenticated user has admin role.
     * 
     * @return true if user has admin role, false otherwise
     */
    public static boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }

    /**
     * Checks if the current user is authenticated.
     * 
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Gets the current authentication object.
     * 
     * @return the authentication object, or null if not authenticated
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
