// EdenAI.java
// External service client for EdenAI keyword extraction API.
// Extracts relevant keywords from job descriptions to improve search and recommendations.
//
// Features:
// - Keyword extraction from text content
// - Configurable number of keywords to extract
// - Importance-based keyword ranking
// - JSON request/response handling
//
// API Integration:
// - Uses EdenAI's text analysis service
// - Supports multiple NLP providers (IBM Watson)
// - Returns ranked keywords with importance scores
//
// Usage:
// - Called by SerpAPIClient to extract keywords from job descriptions
// - Used in recommendation algorithm for keyword-based matching
//
// Author: Job Recommendation Team
// Date: 2024

package com.example.jobrec.external;

import com.example.jobrec.entity.ExtractRequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.io.IOException;

/**
 * EdenAI provides keyword extraction functionality using EdenAI's NLP services.
 * Analyzes job descriptions and extracts the most relevant keywords for search and recommendations.
 */
public class EdenAI {
    /** API authentication token (replace with your actual API key) */
    private static final String EDENAI_TOKEN = "Bearer " + "YOUR_API_KEY";

    /** EdenAI keyword extraction endpoint */
    private static final String EXTRACT_URL = "https://api.edenai.run/v2/text/keyword_extraction";

    /**
     * Main method for testing EdenAI keyword extraction.
     * Demonstrates extraction of keywords from sample text.
     */
    public static void main(String[] args) {
        String sampleText = "Artificial Intelligence (AI) is revolutionizing various industries, from healthcare to finance. "
                + "AI-powered systems are capable of performing tasks that traditionally required human intelligence, "
                + "such as visual perception, speech recognition, decision-making, and language translation.";

        EdenAI client = new EdenAI();
        Set<String> keywordSet = client.extract(sampleText, 3);
        System.out.println("Extracted keywords: " + keywordSet);
    }

    /**
     * Extracts keywords from text using EdenAI's keyword extraction API.
     * @param article Text content to analyze
     * @param keywords_num Maximum number of keywords to extract
     * @return Set of extracted keywords, ranked by importance
     */
    public Set<String> extract(String article, int keywords_num) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ObjectMapper mapper = new ObjectMapper();

        // Create POST request to EdenAI API
        HttpPost request = new HttpPost(EXTRACT_URL);
        request.setHeader("Content-type", "application/json");
        request.setHeader("Authorization", EDENAI_TOKEN);
        request.setHeader("accept", "application/json");
        
        ExtractRequestBody body = new ExtractRequestBody(article);

        // Convert request body to JSON
        String jsonBody;
        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing request body: " + e.getMessage());
            return Collections.emptySet();
        }

        // Set request body
        try {
            request.setEntity(new StringEntity(jsonBody));
        } catch (UnsupportedEncodingException e) {
            System.err.println("Error setting request entity: " + e.getMessage());
            return Collections.emptySet();
        }

        // Create response handler to process API response
        ResponseHandler<Set<String>> responseHandler = response -> {
            if (response.getStatusLine().getStatusCode() != 200) {
                System.err.println("EdenAI API returned status: " + response.getStatusLine().getStatusCode());
                return Collections.emptySet();
            }
            
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                System.err.println("Empty response from EdenAI API");
                return Collections.emptySet();
            }
            // Parse JSON response from EdenAI
            JsonNode root = mapper.readTree(entity.getContent());
            JsonNode ibm = root.get("ibm");
            
            if (ibm == null) {
                System.err.println("No IBM results in EdenAI response");
                return Collections.emptySet();
            }

            JsonNode ibmItems = ibm.get("items");
            if (ibmItems == null) {
                System.err.println("No items in IBM results");
                return Collections.emptySet();
            }

            // Sort keywords by importance score (TreeMap maintains order)
            TreeMap<Double, ArrayList<String>> keywords = new TreeMap<>();
            Iterator<JsonNode> itemsIterator = ibmItems.elements();
            
            while (itemsIterator.hasNext()) {
                JsonNode itemNode = itemsIterator.next();
                String keyword = itemNode.get("keyword").asText();
                double importance = itemNode.get("importance").asDouble();
                
                ArrayList<String> wordsList = keywords.getOrDefault(importance, new ArrayList<>());
                wordsList.add(keyword);
                keywords.put(importance, wordsList);
            }

            // Extract top keywords based on importance (highest first)
            Set<String> refinedSet = new HashSet<>();
            
            while (refinedSet.size() < keywords_num && !keywords.isEmpty()) {
                // Get highest importance keywords first (pollLastEntry gets highest)
                ArrayList<String> wordsList = keywords.pollLastEntry().getValue();
                while (!wordsList.isEmpty() && refinedSet.size() < keywords_num) {
                    refinedSet.add(wordsList.remove(0));
                }
            }

            return refinedSet;
        };

        // Execute HTTP request and return results
        try {
            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            System.err.println("Error executing EdenAI request: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                System.err.println("Error closing HTTP client: " + e.getMessage());
            }
        }

        return Collections.emptySet();
    }
}
