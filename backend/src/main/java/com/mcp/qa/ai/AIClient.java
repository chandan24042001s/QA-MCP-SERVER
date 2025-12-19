package com.mcp.qa.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AIClient {

    private final RestTemplate restTemplate;
    private final List<String> apiKeys;
    private final AtomicInteger currentKeyIndex;
    private final String apiUrl;
    private final String model;

    public AIClient(
            @Value("${ai.api.key:}") String apiKey,
            @Value("${ai.api.key.2:}") String apiKey2,
            @Value("${ai.api.key.3:}") String apiKey3,
            @Value("${ai.api.url:https://api.groq.com/openai/v1/chat/completions}") String apiUrl,
            @Value("${ai.model:llama-3.1-8b-instant}") String model) {
        this.restTemplate = new RestTemplate();
        this.apiKeys = new ArrayList<>();
        this.currentKeyIndex = new AtomicInteger(0);
        this.apiUrl = apiUrl;
        this.model = model;
        
        // Collect all API keys
        addApiKeyIfPresent(apiKey);
        addApiKeyIfPresent(apiKey2);
        addApiKeyIfPresent(apiKey3);
        
        // Also check for comma-separated keys in primary key
        if (apiKey != null && !apiKey.trim().isEmpty() && apiKey.contains(",")) {
            String[] keys = apiKey.split(",");
            for (String key : keys) {
                addApiKeyIfPresent(key.trim());
            }
        }
        
        // Log configuration (without exposing full keys)
        if (this.apiKeys.isEmpty()) {
            System.err.println("WARNING: No AI API keys configured. Set ai.api.key in application.properties");
        } else {
            System.out.println("AI Client initialized with model: " + model + ", " + 
                this.apiKeys.size() + " API key(s) configured");
        }
    }
    
    private void addApiKeyIfPresent(String key) {
        if (key != null && !key.trim().isEmpty()) {
            String cleanKey = key.trim();
            // Remove quotes if present
            if (cleanKey.startsWith("\"") && cleanKey.endsWith("\"")) {
                cleanKey = cleanKey.substring(1, cleanKey.length() - 1);
            }
            if (!cleanKey.isEmpty() && !this.apiKeys.contains(cleanKey)) {
                this.apiKeys.add(cleanKey);
            }
        }
    }
    
    private String getCurrentApiKey() {
        if (apiKeys.isEmpty()) {
            throw new IllegalStateException("No API keys configured");
        }
        int index = currentKeyIndex.get() % apiKeys.size();
        return apiKeys.get(index);
    }
    
    private void rotateToNextKey() {
        if (apiKeys.size() > 1) {
            int nextIndex = (currentKeyIndex.incrementAndGet()) % apiKeys.size();
            System.out.println("Rotating to API key " + (nextIndex + 1) + " of " + apiKeys.size());
        }
    }
    
    private boolean isRateLimitError(Exception e) {
        if (e instanceof HttpClientErrorException) {
            HttpClientErrorException httpEx = (HttpClientErrorException) e;
            int statusCode = httpEx.getStatusCode().value();
            // Rate limit errors: 429 Too Many Requests
            if (statusCode == 429) {
                return true;
            }
            // Also check response body for rate limit messages
            String errorBody = httpEx.getResponseBodyAsString();
            if (errorBody != null && (
                errorBody.contains("rate limit") || 
                errorBody.contains("rate_limit") ||
                errorBody.contains("quota") ||
                errorBody.contains("Too Many Requests"))) {
                return true;
            }
        }
        String message = e.getMessage();
        return message != null && (
            message.contains("429") ||
            message.contains("rate limit") ||
            message.contains("rate_limit") ||
            message.contains("quota") ||
            message.contains("Too Many Requests"));
    }

    public String callAI(String systemPrompt, String userPrompt) {
        if (apiKeys.isEmpty()) {
            throw new IllegalStateException("AI API key not configured. Set ai.api.key in application.properties");
        }

        int maxRetries = apiKeys.size();
        Exception lastException = null;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            String currentKey = getCurrentApiKey();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(currentKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            ));
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 4000);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
                
                // Check for HTTP errors
                if (!response.getStatusCode().is2xxSuccessful()) {
                    Map<String, Object> errorBody = response.getBody();
                    String errorMsg = "HTTP " + response.getStatusCode().value();
                    if (errorBody != null) {
                        errorMsg += ": " + errorBody.toString();
                    }
                    throw new RuntimeException("AI API call failed: " + errorMsg);
                }
                
                Map<String, Object> body = response.getBody();
                if (body != null && body.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                    if (!choices.isEmpty()) {
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        Object content = message.get("content");
                        return content != null ? content.toString() : "";
                    }
                }
                
                // Check for error in response
                if (body != null && body.containsKey("error")) {
                    Map<String, Object> error = (Map<String, Object>) body.get("error");
                    String errorMsg = error.get("message") != null ? error.get("message").toString() : "Unknown error";
                    
                    // Check if it's a rate limit error
                    if (errorMsg.toLowerCase().contains("rate limit") || 
                        errorMsg.toLowerCase().contains("quota") ||
                        errorMsg.toLowerCase().contains("429")) {
                        if (attempt < maxRetries - 1) {
                            System.out.println("Rate limit hit with key " + (currentKeyIndex.get() + 1) + ", trying next key...");
                            rotateToNextKey();
                            lastException = new RuntimeException("AI API rate limit: " + errorMsg);
                            continue;
                        }
                    }
                    
                    throw new RuntimeException("AI API error: " + errorMsg);
                }
                
                throw new RuntimeException("Invalid AI API response format: " + body);
            } catch (HttpClientErrorException e) {
                String errorBody = e.getResponseBodyAsString();
                String errorMsg = "HTTP " + e.getStatusCode().value() + ": " + e.getStatusText();
                if (errorBody != null && !errorBody.isEmpty()) {
                    errorMsg += " - " + errorBody;
                }
                
                // Check if it's a rate limit error and we have more keys to try
                if (isRateLimitError(e) && attempt < maxRetries - 1) {
                    System.out.println("Rate limit hit with key " + (currentKeyIndex.get() + 1) + 
                        " (HTTP " + e.getStatusCode().value() + "), trying next key...");
                    rotateToNextKey();
                    lastException = new RuntimeException("AI API call failed: " + errorMsg, e);
                    continue;
                }
                
                throw new RuntimeException("AI API call failed: " + errorMsg, e);
            } catch (Exception e) {
                // Check if it's a rate limit error and we have more keys to try
                if (isRateLimitError(e) && attempt < maxRetries - 1) {
                    System.out.println("Rate limit error detected, trying next API key...");
                    rotateToNextKey();
                    lastException = e;
                    continue;
                }
                
                throw new RuntimeException("AI API call failed: " + e.getMessage(), e);
            }
        }
        
        // If we've exhausted all keys, throw the last exception
        if (lastException != null) {
            throw new RuntimeException("All API keys exhausted due to rate limits: " + lastException.getMessage(), lastException);
        }
        
        throw new RuntimeException("Failed to call AI API after " + maxRetries + " attempts");
    }

    public String callAIStreaming(String systemPrompt, String userPrompt) {
        return callAI(systemPrompt, userPrompt);
    }
}


