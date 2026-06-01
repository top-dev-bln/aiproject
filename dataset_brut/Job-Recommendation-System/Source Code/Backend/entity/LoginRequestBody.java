// LoginRequestBody.java
// Request body entity for user login API endpoint.
// Contains user credentials for authentication.
//
// Author: Job Recommendation Team
// Date: 2024

package com.example.jobrec.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * LoginRequestBody represents the JSON request body for user login.
 * Contains user ID and hashed password for authentication.
 */
public class LoginRequestBody {
    /** User ID for login */
    @JsonProperty("user_id")
    public String userId;

    /** Hashed password for authentication */
    public String password;
}

