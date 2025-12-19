package com.mcp.qa.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class AIResponseParser {

    private final ObjectMapper objectMapper;

    public AIResponseParser() {
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> parseResponse(String aiResponse) {
        try {
            String jsonContent = extractJsonFromMarkdown(aiResponse);
            
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            return objectMapper.convertValue(rootNode, Map.class);
        } catch (Exception e) {
            return Map.of(
                "rawResponse", aiResponse,
                "parseError", e.getMessage()
            );
        }
    }

    private String extractJsonFromMarkdown(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        return cleaned.trim();
    }

    public List<Map<String, Object>> parseCodeInsights(String aiResponse) {
        Map<String, Object> parsed = parseResponse(aiResponse);
        Object findingsObj = parsed.get("findings");
        
        if (findingsObj instanceof List) {
            return (List<Map<String, Object>>) findingsObj;
        }
        
        return Collections.emptyList();
    }

    public Map<String, Object> parseDefectPrediction(String aiResponse) {
        try {
            Map<String, Object> parsed = parseResponse(aiResponse);
            
            if (parsed.containsKey("parseError")) {
                System.err.println("Failed to parse AI response: " + parsed.get("parseError"));
                System.err.println("Raw response: " + parsed.get("rawResponse"));
                return createDefaultDefectPrediction();
            }
            
            Map<String, Object> result = new HashMap<>();
            
            Object defectScoreObj = parsed.get("defectScore");
            int defectScore = 0;
            if (defectScoreObj != null) {
                if (defectScoreObj instanceof Number) {
                    defectScore = ((Number) defectScoreObj).intValue();
                } else if (defectScoreObj instanceof String) {
                    try {
                        defectScore = Integer.parseInt((String) defectScoreObj);
                    } catch (NumberFormatException e) {
                        defectScore = 0;
                    }
                }
            }
            result.put("defectScore", Math.max(0, Math.min(100, defectScore))); // Clamp 0-100
            
            Object crashProbObj = parsed.get("crashProbability");
            int crashProbability = 0;
            if (crashProbObj != null) {
                if (crashProbObj instanceof Number) {
                    crashProbability = ((Number) crashProbObj).intValue();
                } else if (crashProbObj instanceof String) {
                    try {
                        crashProbability = Integer.parseInt((String) crashProbObj);
                    } catch (NumberFormatException e) {
                        crashProbability = 0;
                    }
                }
            }
            result.put("crashProbability", Math.max(0, Math.min(100, crashProbability)));
            
            String severity = String.valueOf(parsed.getOrDefault("severity", "LOW"));
            if (!severity.equals("HIGH") && !severity.equals("MEDIUM") && !severity.equals("LOW")) {
                severity = defectScore > 70 ? "HIGH" : defectScore > 40 ? "MEDIUM" : "LOW";
            }
            result.put("severity", severity);
            
            Object riskReasonsObj = parsed.get("riskReasons");
            List<String> riskReasons = Collections.emptyList();
            if (riskReasonsObj instanceof List) {
                riskReasons = (List<String>) riskReasonsObj;
            }
            result.put("riskReasons", riskReasons);
            
            Object hotspotsObj = parsed.get("hotspots");
            List<Integer> hotspots = Collections.emptyList();
            if (hotspotsObj instanceof List) {
                hotspots = (List<Integer>) hotspotsObj;
            }
            result.put("hotspots", hotspots);
            
            return result;
        } catch (Exception e) {
            System.err.println("Error parsing defect prediction: " + e.getMessage());
            e.printStackTrace();
            return createDefaultDefectPrediction();
        }
    }
    
    private Map<String, Object> createDefaultDefectPrediction() {
        return Map.of(
            "defectScore", 0,
            "crashProbability", 0,
            "severity", "LOW",
            "riskReasons", Collections.emptyList(),
            "hotspots", Collections.emptyList()
        );
    }

    public List<Map<String, Object>> parseTestGaps(String aiResponse) {
        Map<String, Object> parsed = parseResponse(aiResponse);
        Object gapsObj = parsed.get("gaps");
        
        if (gapsObj instanceof List) {
            return (List<Map<String, Object>>) gapsObj;
        }
        
        return Collections.emptyList();
    }

    public Map<String, Object> parseRefactorRecommendations(String aiResponse) {
        Map<String, Object> parsed = parseResponse(aiResponse);
        
        Map<String, Object> result = new HashMap<>();
        result.put("architecture", parsed.getOrDefault("architecture", Collections.emptyList()));
        result.put("functions", parsed.getOrDefault("functions", Collections.emptyList()));
        result.put("codeSmells", parsed.getOrDefault("codeSmells", Collections.emptyList()));
        result.put("packageRestructure", parsed.getOrDefault("packageRestructure", Collections.emptyList()));
        
        return result;
    }

    public List<Map<String, Object>> parseMemoryLeaks(String aiResponse) {
        Map<String, Object> parsed = parseResponse(aiResponse);
        Object leaksObj = parsed.get("leaks");
        
        if (leaksObj instanceof List) {
            return (List<Map<String, Object>>) leaksObj;
        }
        
        return Collections.emptyList();
    }
}

