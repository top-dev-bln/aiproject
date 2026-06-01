package com.example.jobrec.servlet;

import com.example.jobrec.external.SerpAPIClient;
import com.example.jobrec.db.MySQLConnection;
import com.example.jobrec.db.RedisConnection;
import com.example.jobrec.entity.Item;
import com.example.jobrec.entity.ResultResponse;
import com.example.jobrec.util.Logger;
import com.example.jobrec.util.ValidationUtil;
import com.example.jobrec.exception.JobRecException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * SearchServlet handles job search requests with location-based filtering.
 * Provides caching, validation, and error handling for search operations.
 */
@WebServlet(name = "SearchServlet", urlPatterns = {"/search"})
public class SearchServlet extends HttpServlet {
    
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CHARSET_UTF8 = "UTF-8";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // POST method not supported for search
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        writeErrorResponse(response, "POST method not supported for search operations");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        Logger.info(SearchServlet.class, "Processing search request");
        
        // Set response properties
        response.setContentType(CONTENT_TYPE_JSON);
        response.setCharacterEncoding(CHARSET_UTF8);
        
        try {
            // Validate session
            HttpSession session = request.getSession(false);
            if (session == null) {
                Logger.warn(SearchServlet.class, "Invalid session for search request");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                writeErrorResponse(response, "Session Invalid");
                return;
            }
            
            // Extract and validate parameters
            String userId = extractUserId(request);
            double latitude = extractLatitude(request);
            double longitude = extractLongitude(request);
            String keyword = extractKeyword(request);
            
            // Validate input parameters
            ValidationUtil.ValidationResult validation = 
                ValidationUtil.validateSearchParameters(latitude, longitude, keyword);
            
            if (validation.hasErrors()) {
                Logger.warn(SearchServlet.class, "Invalid search parameters: " + validation.getErrors());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeErrorResponse(response, validation.getErrors());
                return;
            }
            
            // Sanitize inputs
            userId = ValidationUtil.validateAndSanitizeUserId(userId);
            keyword = ValidationUtil.validateAndSanitizeKeyword(keyword);
            
            if (userId == null) {
                Logger.warn(SearchServlet.class, "Invalid user ID format");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeErrorResponse(response, "Invalid user ID format");
                return;
            }
            
            // Perform search with caching
            List<Item> items = performSearch(latitude, longitude, keyword, userId);
            
            // Write successful response
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getWriter(), items);
            
            Logger.logExecutionTime(SearchServlet.class, "doGet", startTime);
            Logger.info(SearchServlet.class, "Search completed successfully, returned " + items.size() + " items");
            
        } catch (JobRecException e) {
            Logger.error(SearchServlet.class, "Search operation failed: " + e.getTechnicalMessage(), e);
            handleJobRecException(response, e);
        } catch (Exception e) {
            Logger.error(SearchServlet.class, "Unexpected error during search", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeErrorResponse(response, "Internal server error occurred");
        }
    }
    
    /**
     * Performs the actual search with caching support.
     */
    private List<Item> performSearch(double latitude, double longitude, String keyword, String userId) 
            throws JobRecException {
        
        RedisConnection redis = null;
        MySQLConnection dbConnection = null;
        
        try {
            // Get user's favorite items
            dbConnection = new MySQLConnection();
            Set<String> favoritedItemIds = dbConnection.getFavoriteItemIds(userId);
            
            // Try cache first
            redis = new RedisConnection();
            String cachedResult = redis.getSearchResult(latitude, longitude, keyword);
            
            List<Item> items;
            if (cachedResult != null) {
                Logger.debug(SearchServlet.class, "Using cached search results");
                ObjectMapper mapper = new ObjectMapper();
                items = Arrays.asList(mapper.readValue(cachedResult, Item[].class));
            } else {
                Logger.debug(SearchServlet.class, "Fetching fresh search results from API");
                SerpAPIClient client = new SerpAPIClient();
                items = client.search(latitude, longitude, keyword);
                
                // Cache the results
                if (items != null && !items.isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    redis.setSearchResult(latitude, longitude, keyword, mapper.writeValueAsString(items));
                }
            }
            
            // Mark favorite items
            if (items != null) {
                for (Item item : items) {
                    item.setFavorite(favoritedItemIds.contains(item.getId()));
                }
            }
            
            return items;
            
        } catch (Exception e) {
            throw JobRecException.internal("Search operation failed", e);
        } finally {
            if (redis != null) {
                redis.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }
    
    /**
     * Extracts and validates user ID from request.
     */
    private String extractUserId(HttpServletRequest request) throws JobRecException {
        String userId = request.getParameter("user_id");
        if (!ValidationUtil.isNotEmpty(userId)) {
            throw JobRecException.validation("User ID is required");
        }
        return userId;
    }
    
    /**
     * Extracts and validates latitude from request.
     */
    private double extractLatitude(HttpServletRequest request) throws JobRecException {
        String latStr = request.getParameter("lat");
        if (!ValidationUtil.isNotEmpty(latStr)) {
            throw JobRecException.validation("Latitude is required");
        }
        
        try {
            return Double.parseDouble(latStr);
        } catch (NumberFormatException e) {
            throw JobRecException.validation("Invalid latitude format");
        }
    }
    
    /**
     * Extracts and validates longitude from request.
     */
    private double extractLongitude(HttpServletRequest request) throws JobRecException {
        String lonStr = request.getParameter("lon");
        if (!ValidationUtil.isNotEmpty(lonStr)) {
            throw JobRecException.validation("Longitude is required");
        }
        
        try {
            return Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
            throw JobRecException.validation("Invalid longitude format");
        }
    }
    
    /**
     * Extracts keyword from request (optional parameter).
     */
    private String extractKeyword(HttpServletRequest request) {
        return request.getParameter("keyword");
    }
    
    /**
     * Handles JobRecException by setting appropriate HTTP status and response.
     */
    private void handleJobRecException(HttpServletResponse response, JobRecException e) throws IOException {
        switch (e.getErrorType()) {
            case VALIDATION:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                break;
            case AUTHENTICATION:
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                break;
            case AUTHORIZATION:
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                break;
            case API:
                response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                break;
        }
        writeErrorResponse(response, e.getUserMessage());
    }
    
    /**
     * Writes error response in JSON format.
     */
    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), new ResultResponse(message));
    }
}

//    Bubbling and Capturing