// SerpAPIClient.java
// External service client for SerpAPI Google Jobs search.
// Fetches job listings from Google Jobs using location-based search.
//
// Features:
// - Location-based job search using coordinates
// - Google Jobs integration via SerpAPI
// - Automatic keyword extraction for job listings
// - JSON response parsing to Item objects
//
// API Integration:
// - Uses SerpAPI's Google Jobs engine
// - Converts coordinates to location codes via GeoConverter
// - Extracts keywords using EdenAI for each job
//
// Data Flow:
// 1. Receives search parameters (location, keyword)
// 2. Converts coordinates to Google location code
// 3. Searches Google Jobs via SerpAPI
// 4. Parses results into Item objects
// 5. Extracts keywords for each job using EdenAI
//
// Author: Leo Ji
// Date: 2025

package com.example.jobrec.external;

import com.example.jobrec.entity.Item;
import com.example.jobrec.config.ApplicationConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.ResponseHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * SerpAPIClient provides job search functionality using Google Jobs via SerpAPI.
 * Searches for job listings based on location and keywords, with automatic keyword extraction.
 */
public class SerpAPIClient {
    /** SerpAPI URL template for Google Jobs search */
    private static final String URL_TEMPLATE = "https://serpapi.com/search?engine=google_jobs&q=%s&uule=%s&api_key=%s";

    /** SerpAPI key loaded from configuration */
    private static final String API_KEY = ApplicationConfig.getInstance().getSerpApiKey();

    /**
     * Main method for testing SerpAPI job search functionality.
     * Demonstrates search for software engineer jobs in Silicon Valley area.
     */
    public static void main(String[] args) {
        SerpAPIClient client = new SerpAPIClient();
        List<Item> jobs = client.search(37.334886, -122.008988, "software engineer");
        
        System.out.println("Found " + jobs.size() + " jobs");
        for (Item job : jobs) {
            System.out.println("Job: " + job.getTitle() + " - Keywords: " + job.getKeywords());
            break; // Show only first result for demo
        }
    }

    /** Default search keyword when none provided */
    private static final String DEFAULT_KEYWORD = "engineer";

    /**
     * Searches for job listings based on location and keyword.
     * @param lat Latitude coordinate
     * @param lon Longitude coordinate  
     * @param keyword Search keyword (optional, defaults to "engineer")
     * @return List of job items with extracted keywords
     */
    public List<Item> search(Double lat, Double lon, String keyword) {
        if (keyword == null) {
            keyword = DEFAULT_KEYWORD;
        }

        try {
            keyword = URLEncoder.encode(keyword, "UTF-8"); //transfer input keyword to URL format
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String address = "";

//        System.out.println("Keyword Parsed");

        GeoConverterClient converterClient = new GeoConverterClient();
        String uuleCode = converterClient.convert(lat, lon);

        String url = String.format(URL_TEMPLATE, keyword, uuleCode, API_KEY); //format URL from above

        CloseableHttpClient httpClient = HttpClients.createDefault(); //create a new httpclient object

//        System.out.println("httpClient Created");

        // Create a custom response handler, get response in ideal format
        ResponseHandler<List<Item>> responseHandler = response -> {
            if (response.getStatusLine().getStatusCode() != 200) {
                return Collections.emptyList();
            }

//            System.out.println("200 OK");

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return Collections.emptyList();
            }

//            System.out.println("Entity OK");

            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(entity.getContent());
            JsonNode results = root.get("jobs_results");
            Iterator<JsonNode> result = results.elements();

//            System.out.println("Result OK");

            List<Item> items = new ArrayList<>();

            while (result.hasNext()) {
                JsonNode itemNode = result.next();
                Item item = extract(itemNode);
                System.out.println(item.toString());
                items.add(item);
            }

            extractKeywords(items);

            return items;
        };

//        System.out.println("Response Handler Created");

        try {
            return httpClient.execute(new HttpGet(url), responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println("Failed");

        return Collections.emptyList();
    }

    private static Item extract (JsonNode itemNode) {
        String job_id = itemNode.get("job_id").asText();
        String title = itemNode.get("title").asText();
        String companyName = itemNode.get("company_name").asText();
        String location = itemNode.get("location").asText();
        String via = itemNode.get("via").asText();
        String description = itemNode.get("description").asText();
        List<String> highlights = new ArrayList<String>();
        String url = "";
        Set<String> keywords = new HashSet<>();

        //Store all job highlights to highlights
        JsonNode highlights_node = itemNode.get("job_highlights");
        Iterator<JsonNode> highlight = highlights_node.elements();
        while (highlight.hasNext()) {
            Iterator<JsonNode> item = highlight.next().get("items").elements();
            while (item.hasNext()) {
                highlights.add(item.next().asText());
            }
        }

        //Get a link for application
        Iterator<JsonNode> url_it = itemNode.get("related_links").elements();
        if (url_it.hasNext()) {
            url = url_it.next().get("link").asText();
        }

        //Store extension(keywords) to keywords
        Iterator<JsonNode> extension_it = itemNode.get("extensions").elements();
        while (extension_it.hasNext()) {
            keywords.add(extension_it.next().asText());
        }

        return new Item(job_id, title, companyName, location, via, description, highlights, url, keywords, false);
    }

    private static void extractKeywords(List<Item> items) {
        EdenAI client = new EdenAI();
        for (Item item: items) {
            String article = item.getDescription() + ". " + String.join(". ", item.getJobHighlights());
            Set<String> keywords = new HashSet<>();
            keywords.addAll(client.extract(article, 3));
            keywords.addAll(item.getKeywords());
            item.setKeywords(keywords);
        }
    }
}
