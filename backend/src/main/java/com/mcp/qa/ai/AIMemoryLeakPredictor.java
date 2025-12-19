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
public class AIMemoryLeakPredictor {

    private final AIClient aiClient;
    private final AIRequestBuilder requestBuilder;
    private final AIResponseParser responseParser;
    private final StaticAnalyzer staticAnalyzer;
    private final ExecutorService executorService;

    public AIMemoryLeakPredictor(AIClient aiClient, AIRequestBuilder requestBuilder,
                                 AIResponseParser responseParser, StaticAnalyzer staticAnalyzer) {
        this.aiClient = aiClient;
        this.requestBuilder = requestBuilder;
        this.responseParser = responseParser;
        this.staticAnalyzer = staticAnalyzer;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public Map<String, Object> predictMemoryLeaks(Path repoPath) {
        try {
            List<Map<String, Object>> staticFindings = staticAnalyzer.analyzePath(repoPath);
            
            Map<String, List<Map<String, Object>>> findingsByFile = staticFindings.stream()
                .collect(Collectors.groupingBy(f -> (String) f.get("file")));
            
            List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
            
            try (var paths = Files.walk(repoPath)) {
                paths.filter(Files::isRegularFile)
                    .filter(this::isCodeFile)
                    .forEach(file -> {
                        CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
                            String relativePath = repoPath.relativize(file).toString();
                            return predictFileLeaks(file, repoPath,
                                findingsByFile.getOrDefault(relativePath, Collections.emptyList()));
                        }, executorService);
                        futures.add(future);
                    });
            }
            
            List<Map<String, Object>> leakPredictions = futures.stream()
                .map(CompletableFuture::join)
                .filter(result -> result.containsKey("leaks"))
                .collect(Collectors.toList());
            
            int totalLeaks = leakPredictions.stream()
                .mapToInt(p -> ((List<?>) p.getOrDefault("leaks", Collections.emptyList())).size())
                .sum();
            
            int highSeverityLeaks = leakPredictions.stream()
                .flatMap(p -> ((List<Map<String, Object>>) p.getOrDefault("leaks", Collections.emptyList())).stream())
                .mapToInt(l -> "HIGH".equals(l.get("severity")) ? 1 : 0)
                .sum();
            
            return Map.of(
                "status", "completed",
                "totalFilesAnalyzed", leakPredictions.size(),
                "totalLeaks", totalLeaks,
                "highSeverityLeaks", highSeverityLeaks,
                "predictions", leakPredictions
            );
            
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "error", e.getMessage()
            );
        }
    }

    private Map<String, Object> predictFileLeaks(Path file, Path repoRoot,
                                                List<Map<String, Object>> staticFindings) {
        try {
            String content = Files.readString(file);
            String relativePath = repoRoot.relativize(file).toString();
            
            if (content.length() > 50000) {
                return Map.of("file", relativePath, "status", "skipped");
            }
            
            String prompt = requestBuilder.buildMemoryLeakPrompt(relativePath, content, staticFindings);
            String systemPrompt = "You are an expert in memory management and resource leak detection. " +
                                "Identify potential memory leaks, resource leaks, and unbounded growth patterns. " +
                                "Return only valid JSON.";
            
            String aiResponse = aiClient.callAI(systemPrompt, prompt);
            List<Map<String, Object>> leaks = responseParser.parseMemoryLeaks(aiResponse);
            
            return Map.of(
                "file", relativePath,
                "leaks", leaks,
                "leakCount", leaks.size()
            );
            
        } catch (Exception e) {
            return Map.of(
                "file", repoRoot.relativize(file).toString(),
                "status", "error",
                "error", e.getMessage()
            );
        }
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

