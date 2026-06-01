// ResultResponse.java
// Generic response entity for API operations.
// Used for simple success/error messages in API responses.
//
// Author: Job Recommendation Team
// Date: 2024

package com.example.jobrec.entity;

/**
 * ResultResponse represents a simple API response with a result message.
 * Used for operations that only need to return a status or message.
 */
public class ResultResponse {
    /** Result message (success/error message) */
    public String result;

    /**
     * Default constructor for JSON serialization.
     */
    public ResultResponse() {
    }

    /**
     * Constructor with result message.
     * @param result The result message
     */
    public ResultResponse(String result) {
        this.result = result;
    }
}
