// RegisterRequestBody.java
// Request body entity for user registration API endpoint.
// Contains all required information for creating a new user account.
//
// Author: Job Recommendation Team
// Date: 2024

package com.example.jobrec.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RegisterRequestBody represents the JSON request body for user registration.
 * Contains all required fields for creating a new user account.
 */
public class RegisterRequestBody {
    /** Unique user ID for the new account */
    @JsonProperty("user_id")
    public String userId;
    
    /** Hashed password for the new account */
    @JsonProperty("password")
    public String password;

    /** User's first name */
    @JsonProperty("first_name")
    public String firstName;

    /** User's last name */
    @JsonProperty("last_name")
    public String lastName;
}

