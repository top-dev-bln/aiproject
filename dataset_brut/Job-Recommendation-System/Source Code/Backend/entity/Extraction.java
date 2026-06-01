// Extraction.java
// Entity for keyword extraction results from EdenAI API.
// Represents individual extracted keywords with metadata.
//
// Author: Job Recommendation Team
// Date: 2024

package com.example.jobrec.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Extraction represents a single keyword extraction result from EdenAI.
 * Contains keyword information and relevance metrics.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Extraction {
    /** Category/type of the extracted keyword */
    @JsonProperty("tag_name")
    public String tagName;

    /** The actual extracted keyword value */
    @JsonProperty("parsed_value")
    public String parsedValue;

    /** Frequency count of the keyword */
    public int count;

    /** Relevance score of the keyword */
    public String relevance;
}

