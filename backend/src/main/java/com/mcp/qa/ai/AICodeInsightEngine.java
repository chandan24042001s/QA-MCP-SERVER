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
public class AICodeInsightEngine {

    private final AIClient aiClient;
    private final AIRequestBuilder requestBuilder;
    private final AIResponseParser responseParser;
    private final StaticAnalyzer staticAnalyzer;
    private final ExecutorService executorService;

    public AICodeInsightEngine(AIClient aiClient, AIRequestBuilder requestBuilder,
                              AIResponseParser responseParser, StaticAnalyzer staticAnalyzer) {
        this.aiClient = aiClient;
        this.requestBuilder = requestBuilder;
        this.responseParser = responseParser;
        this.staticAnalyzer = staticAnalyzer;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public Map<String, Object> analyzeRepository(Path repoPath) {
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
                            return analyzeFile(file, repoPath, findingsByFile.getOrDefault(
                                repoPath.relativize(file).toString(), Collections.emptyList()));
                        }, executorService);
                        futures.add(future);
                    });
            }
            
            List<Map<String, Object>> allInsights = futures.stream()
                .map(CompletableFuture::join)
                .filter(result -> !result.isEmpty())
                .collect(Collectors.toList());
            
            return Map.of(
                "status", "completed",
                "totalFilesAnalyzed", allInsights.size(),
                "insights", allInsights
            );
            
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "error", e.getMessage()
            );
        }
    }

    private Map<String, Object> analyzeFile(Path file, Path repoRoot, 
                                           List<Map<String, Object>> staticFindings) {
        try {
            String content = Files.readString(file);
            String relativePath = repoRoot.relativize(file).toString();
            
            if (content.length() > 50000) {
                return Map.of("file", relativePath, "status", "skipped", "reason", "file too large");
            }
            
            String prompt = requestBuilder.buildCodeInsightPrompt(relativePath, content, staticFindings);
            String systemPrompt = "You are an expert code reviewer specializing in finding hidden bugs, " +
                                "logical errors, and architectural flaws. Return only valid JSON.";
            
            String aiResponse = aiClient.callAI(systemPrompt, prompt);
            List<Map<String, Object>> findings = responseParser.parseCodeInsights(aiResponse);
            
            return Map.of(
                "file", relativePath,
                "findings", findings,
                "count", findings.size()
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

