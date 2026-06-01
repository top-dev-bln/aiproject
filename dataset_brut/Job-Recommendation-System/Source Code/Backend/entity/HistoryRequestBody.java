// HistoryRequestBody.java
// Request body entity for favorite items management API.
// Used for adding/removing items from user's favorites list.
//
// Author: Job Recommendation Team
// Date: 2024

package com.example.jobrec.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * HistoryRequestBody represents the JSON request body for favorite operations.
 * Contains user ID and the item to be added/removed from favorites.
 */
public class HistoryRequestBody {
    /** User ID performing the favorite operation */
    @JsonProperty("user_id")
    public String userId;

    /** Job item to be favorited/unfavorited */
    public Item favorite;
}

