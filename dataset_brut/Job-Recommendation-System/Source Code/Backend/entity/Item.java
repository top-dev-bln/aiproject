// Item.java
// Core entity class representing a job listing in the recommendation system.
// This class holds all job-related information including title, company, location,
// description, keywords, and user's favorite status.
//
// Features:
// - JSON serialization/deserialization support
// - Flexible field mapping with @JsonProperty annotations
// - Support for keywords and job highlights
// - Favorite status tracking per user
// - Proper equals/hashCode implementation for collections
//
// Used by:
// - Search results from external APIs (SerpAPI)
// - Database storage and retrieval
// - User favorites management
// - Recommendation algorithms
//
// Author: Leo Ji
// Date: 2024

package com.example.jobrec.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Item represents a job listing with all associated information.
 * Supports JSON serialization for API communication and database storage.
 * 
 * Key fields:
 * - Basic info: id, title, company, location, url
 * - Content: description, job highlights
 * - Metadata: keywords, favorite status
 */
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown fields during deserialization
@JsonInclude(JsonInclude.Include.NON_NULL) // Skip null fields during serialization
public class Item {
    /** Unique job identifier */
    private String id;
    
    /** Job title */
    private String title;
    
    /** Company name */
    private String company_name;

    /** Job location (city, state, country) */
    private String location;
    
    /** Source platform (e.g., "via LinkedIn") */
    private String via;
    
    /** Detailed job description */
    private String description;
    
    /** List of job highlights/benefits */
    private List<String> job_highlights;
    
    /** Application URL */
    private String url;
    
    /** Set of extracted keywords */
    private Set<String> keywords;
    
    /** Whether this job is favorited by current user */
    private boolean favorite;


    /**
     * Default constructor for JSON deserialization.
     */
    public Item() {
    }

    /**
     * Full constructor for creating Item instances.
     * @param id Unique job identifier
     * @param title Job title
     * @param company_name Company name
     * @param location Job location
     * @param via Source platform
     * @param description Job description
     * @param job_highlights List of job highlights
     * @param url Application URL
     * @param keywords Set of keywords
     * @param favorite Whether favorited by user
     */
    public Item(String id, String title, String company_name, String location, String via, String description, List<String> job_highlights, String url, Set<String> keywords, boolean favorite) {
        this.id = id;
        this.title = title;
        this.company_name = company_name;
        this.location = location;
        this.via = via;
        this.description = description;
        this.job_highlights = job_highlights;
        this.url = url;
        this.keywords = keywords;
        this.favorite = favorite;
    }


    /**
     * Gets the unique job identifier.
     * @return job ID
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    /**
     * Gets the job title.
     * @return job title
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }
    /**
     * Gets the company name.
     * @return company name
     */
    @JsonProperty("company_name")
    public String getCompanyName() {
        return company_name;
    }
    /**
     * Gets the job location.
     * @return job location
     */
    @JsonProperty("location")
    public String getLocation() {
        return location;
    }
    /**
     * Gets the source platform.
     * @return source platform (e.g., "via LinkedIn")
     */
    @JsonProperty("via")
    public String getVia() {
        return via;
    }
    /**
     * Gets the job description.
     * @return job description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }
    /**
     * Gets the job highlights/benefits.
     * @return list of job highlights
     */
    @JsonProperty("job_highlights")
    public List<String> getJobHighlights() {
        return job_highlights;
    }
    /**
     * Gets the application URL.
     * @return application URL
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     * Gets the extracted keywords.
     * @return set of keywords
     */
    public Set<String> getKeywords() {
        return keywords;
    }

    /**
     * Sets the keywords for this job.
     * @param keywords set of keywords to associate with this job
     */
    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }
    /**
     * Gets the favorite status for current user.
     * @return true if favorited by current user
     */
    public boolean getFavorite() {
        return favorite;
    }

    /**
     * Sets the favorite status for current user.
     * @param favorite true to mark as favorite, false otherwise
     */
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return favorite == item.favorite &&
                Objects.equals(id, item.id) &&
                Objects.equals(title, item.title) &&
                Objects.equals(company_name, item.company_name) &&
                Objects.equals(location, item.location) &&
                Objects.equals(via, item.via) &&
                Objects.equals(description, item.description) &&
                Objects.equals(job_highlights, item.job_highlights) &&
                Objects.equals(url, item.url) &&
                Objects.equals(keywords, item.keywords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, company_name, location, via, description, job_highlights, url, keywords, favorite);
    }

    /**
     * Returns string representation of the Item.
     * @return formatted string with all item details
     */
    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", company_name='" + company_name + '\'' +
                ", location='" + location + '\'' +
                ", via='" + via + '\'' +
                ", description='" + description + '\'' +
                ", job_highlights='" + job_highlights + '\'' +
                ", url='" + url + '\'' +
                ", keywords=" + keywords +
                ", favorite=" + favorite +
                '}';
    }
}
