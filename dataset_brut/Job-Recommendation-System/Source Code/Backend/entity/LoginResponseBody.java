// LoginResponseBody.java
// Response body entity for user login API endpoint.
// Contains login result status and user information.
//
// Author: Job Recommendation Team
// Date: 2024

package com.example.jobrec.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * LoginResponseBody represents the JSON response body for user login.
 * Contains authentication status and user details on successful login.
 */
public class LoginResponseBody {
    /** Login status ("OK" for success, error message for failure) */
    public String status;

    /** User ID (returned on successful login) */
    @JsonProperty("user_id")
    public String userId;

    /** User's full name (returned on successful login) */
    public String name;

    /**
     * Default constructor for JSON serialization.
     */
    public LoginResponseBody() {
    }

    /**
     * Constructor for creating login response.
     * @param status Login status
     * @param userId User ID
     * @param name User's full name
     */
    public LoginResponseBody(String status, String userId, String name) {
        this.status = status;
        this.userId = userId;
        this.name = name;
    }
}

