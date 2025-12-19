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

@Component
public class AIClient {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public AIClient(
            @Value("${ai.api.key:}") String apiKey,
            @Value("${ai.api.url:https://api.groq.com/openai/v1/chat/completions}") String apiUrl,
            @Value("${ai.model:llama-3.1-8b-instant}") String model) {
        this.restTemplate = new RestTemplate();
        
        // Clean API key - remove quotes if present
        String cleanKey = apiKey != null ? apiKey.trim() : "";
        if (cleanKey.startsWith("\"") && cleanKey.endsWith("\"")) {
            cleanKey = cleanKey.substring(1, cleanKey.length() - 1);
        }
        this.apiKey = cleanKey;
        this.apiUrl = apiUrl;
        this.model = model;
        
        // Log configuration (without exposing full key)
        if (this.apiKey.isEmpty()) {
            System.err.println("WARNING: AI API key is not configured. Set ai.api.key in application.properties");
        } else {
            System.out.println("AI Client initialized with model: " + model + ", API key: " + 
                (this.apiKey.length() > 10 ? this.apiKey.substring(0, 10) + "..." : "***"));
        }
    }

    public String callAI(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("AI API key not configured. Set ai.api.key in application.properties");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

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
                throw new RuntimeException("AI API error: " + errorMsg);
            }
            
            throw new RuntimeException("Invalid AI API response format: " + body);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            String errorMsg = "HTTP " + e.getStatusCode().value() + ": " + e.getStatusText();
            if (errorBody != null && !errorBody.isEmpty()) {
                errorMsg += " - " + errorBody;
            }
            throw new RuntimeException("AI API call failed: " + errorMsg, e);
        } catch (Exception e) {
            throw new RuntimeException("AI API call failed: " + e.getMessage(), e);
        }
    }

    public String callAIStreaming(String systemPrompt, String userPrompt) {
        return callAI(systemPrompt, userPrompt);
    }
}


