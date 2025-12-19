package com.mcp.qa.ai;

import com.mcp.qa.analyzer.StaticAnalyzer;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.io.IOException;

@Component
public class AIDefectPredictor {

    private final AIClient aiClient;
    private final AIRequestBuilder requestBuilder;
    private final AIResponseParser responseParser;
    private final StaticAnalyzer staticAnalyzer;
    private final ExecutorService executorService;

    public AIDefectPredictor(AIClient aiClient, AIRequestBuilder requestBuilder,
                            AIResponseParser responseParser, StaticAnalyzer staticAnalyzer) {
        this.aiClient = aiClient;
        this.requestBuilder = requestBuilder;
        this.responseParser = responseParser;
        this.staticAnalyzer = staticAnalyzer;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public Map<String, Object> predictDefects(Path repoPath) {
        try {
            System.out.println("Starting defect prediction for: " + repoPath);
            
            List<Map<String, Object>> staticFindings = staticAnalyzer.analyzePath(repoPath);
            System.out.println("Static analysis found " + staticFindings.size() + " findings");
            
            Map<String, List<Map<String, Object>>> findingsByFile = staticFindings.stream()
                .collect(Collectors.groupingBy(f -> (String) f.get("file")));
            
            Map<String, Map<String, Object>> metricsByFile = calculateMetrics(repoPath);
            System.out.println("Calculated metrics for " + metricsByFile.size() + " files");
            
            List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
            
            try (var paths = Files.walk(repoPath)) {
                paths.filter(Files::isRegularFile)
                    .filter(this::isCodeFile)
                    .forEach(file -> {
                        CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
                            String relativePath = repoPath.relativize(file).toString();
                            return predictFileDefects(file, repoPath, 
                                findingsByFile.getOrDefault(relativePath, Collections.emptyList()),
                                metricsByFile.getOrDefault(relativePath, Collections.emptyMap()));
                        }, executorService);
                        futures.add(future);
                    });
            }
            
            System.out.println("Found " + futures.size() + " code files to analyze");
            
            List<Map<String, Object>> allResults = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
            
            List<Map<String, Object>> predictions = allResults.stream()
                .filter(result -> result.containsKey("defectScore"))
                .sorted((a, b) -> {
                    int scoreA = (Integer) a.getOrDefault("defectScore", 0);
                    int scoreB = (Integer) b.getOrDefault("defectScore", 0);
                    return Integer.compare(scoreB, scoreA); 
                })
                .collect(Collectors.toList());
            
            List<Map<String, Object>> errors = allResults.stream()
                .filter(result -> result.containsKey("status") && "error".equals(result.get("status")))
                .collect(Collectors.toList());
            
            if (!errors.isEmpty()) {
                System.err.println("AI processing errors for " + errors.size() + " files:");
                errors.forEach(err -> System.err.println("  - " + err.get("file") + ": " + err.get("error")));
            }
            
            System.out.println("Processed " + allResults.size() + " files, " + predictions.size() + " successful predictions, " + errors.size() + " errors");
            
            int totalFiles = predictions.size();
            int highRiskFiles = (int) predictions.stream()
                .filter(p -> "HIGH".equals(p.get("severity")))
                .count();
            int avgDefectScore = predictions.stream()
                .mapToInt(p -> (Integer) p.getOrDefault("defectScore", 0))
                .sum() / (totalFiles > 0 ? totalFiles : 1);
            
            return Map.of(
                "status", "completed",
                "totalFiles", totalFiles,
                "highRiskFiles", highRiskFiles,
                "averageDefectScore", avgDefectScore,
                "predictions", predictions
            );
            
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "error", e.getMessage()
            );
        }
    }

    private Map<String, Object> predictFileDefects(Path file, Path repoRoot,
                                                   List<Map<String, Object>> staticFindings,
                                                   Map<String, Object> metrics) {
        try {
            String content = Files.readString(file);
            String relativePath = repoRoot.relativize(file).toString();
            
            if (content.length() > 50000) {
                return Map.of("file", relativePath, "status", "skipped");
            }
            
            String prompt = requestBuilder.buildDefectPredictionPrompt(relativePath, content, staticFindings, metrics);
            String systemPrompt = "You are an expert software quality analyst. " +
                                "Predict defect probability based on code patterns, complexity, and static analysis. " +
                                "Return only valid JSON.";
            
            String aiResponse = aiClient.callAI(systemPrompt, prompt);
            Map<String, Object> prediction = responseParser.parseDefectPrediction(aiResponse);
            prediction.put("file", relativePath);
            
            if (!prediction.containsKey("defectScore")) {
                prediction.put("defectScore", 0);
            }
            
            return prediction;
            
        } catch (Exception e) {
            System.err.println("Error processing file " + repoRoot.relativize(file) + ": " + e.getMessage());
            e.printStackTrace();
            return Map.of(
                "file", repoRoot.relativize(file).toString(),
                "status", "error",
                "error", e.getMessage(),
                "defectScore", 0
            );
        }
    }

    private Map<String, Map<String, Object>> calculateMetrics(Path repoPath) {
        Map<String, Map<String, Object>> metrics = new HashMap<>();
        
        try (var paths = Files.walk(repoPath)) {
            paths.filter(Files::isRegularFile)
                .filter(this::isCodeFile)
                .forEach(file -> {
                    try {
                        String content = Files.readString(file);
                        String relativePath = repoPath.relativize(file).toString();
                        
                        int lines = content.split("\n").length;
                        int complexity = countOccurrences(content, "if\\s*\\(") +
                                       countOccurrences(content, "for\\s*\\(") +
                                       countOccurrences(content, "while\\s*\\(") +
                                       countOccurrences(content, "catch\\s*\\(");
                        
                        metrics.put(relativePath, Map.of(
                            "lines", lines,
                            "complexity", complexity,
                            "size", content.length()
                        ));
                    } catch (IOException ignored) {}
                });
        } catch (IOException ignored) {}
        
        return metrics;
    }

    private int countOccurrences(String text, String pattern) {
        return (int) java.util.regex.Pattern.compile(pattern).matcher(text).results().count();
    }

    private boolean isCodeFile(Path file) {
        String fileName = file.toString().toLowerCase();
        return fileName.endsWith(".java") || 
               fileName.endsWith(".js") || 
               fileName.endsWith(".ts") || 
               fileName.endsWith(".py") ||
               fileName.endsWith(".jsx") ||
               fileName.endsWith(".tsx");
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

