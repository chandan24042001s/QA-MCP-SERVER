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
public class AIRefactorAdvisor {

    private final AIClient aiClient;
    private final AIRequestBuilder requestBuilder;
    private final AIResponseParser responseParser;
    private final StaticAnalyzer staticAnalyzer;
    private final ExecutorService executorService;

    public AIRefactorAdvisor(AIClient aiClient, AIRequestBuilder requestBuilder,
                            AIResponseParser responseParser, StaticAnalyzer staticAnalyzer) {
        this.aiClient = aiClient;
        this.requestBuilder = requestBuilder;
        this.responseParser = responseParser;
        this.staticAnalyzer = staticAnalyzer;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public Map<String, Object> analyzeRefactoring(Path repoPath) {
        try {
            List<Map<String, Object>> staticFindings = staticAnalyzer.analyzePath(repoPath);
            
            Map<String, List<Map<String, Object>>> findingsByFile = staticFindings.stream()
                .collect(Collectors.groupingBy(f -> (String) f.get("file")));
            
            Map<String, Object> architectureContext = analyzeArchitecture(repoPath);
            
            List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
            
            try (var paths = Files.walk(repoPath)) {
                paths.filter(Files::isRegularFile)
                    .filter(this::isCodeFile)
                    .forEach(file -> {
                        CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
                            String relativePath = repoPath.relativize(file).toString();
                            return analyzeFileRefactoring(file, repoPath,
                                findingsByFile.getOrDefault(relativePath, Collections.emptyList()),
                                architectureContext);
                        }, executorService);
                        futures.add(future);
                    });
            }
            
            List<Map<String, Object>> recommendations = futures.stream()
                .map(CompletableFuture::join)
                .filter(result -> result.containsKey("recommendations"))
                .collect(Collectors.toList());
            
            List<String> allArchRecommendations = recommendations.stream()
                .flatMap(r -> {
                    Map<String, Object> recs = (Map<String, Object>) r.get("recommendations");
                    List<String> arch = (List<String>) recs.getOrDefault("architecture", Collections.emptyList());
                    return arch.stream();
                })
                .distinct()
                .collect(Collectors.toList());
            
            return Map.of(
                "status", "completed",
                "totalFilesAnalyzed", recommendations.size(),
                "architectureRecommendations", allArchRecommendations,
                "fileRecommendations", recommendations
            );
            
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "error", e.getMessage()
            );
        }
    }

    private Map<String, Object> analyzeFileRefactoring(Path file, Path repoRoot,
                                                       List<Map<String, Object>> staticFindings,
                                                       Map<String, Object> architectureContext) {
        try {
            String content = Files.readString(file);
            String relativePath = repoRoot.relativize(file).toString();
            
            if (content.length() > 50000) {
                return Map.of("file", relativePath, "status", "skipped");
            }
            
            String prompt = requestBuilder.buildRefactorPrompt(relativePath, content, staticFindings, architectureContext);
            String systemPrompt = "You are an expert software architect and refactoring specialist. " +
                                "Provide actionable refactoring recommendations at both architecture and function levels. " +
                                "Return only valid JSON.";
            
            String aiResponse = aiClient.callAI(systemPrompt, prompt);
            Map<String, Object> recommendations = responseParser.parseRefactorRecommendations(aiResponse);
            
            return Map.of(
                "file", relativePath,
                "recommendations", recommendations
            );
            
        } catch (Exception e) {
            return Map.of(
                "file", repoRoot.relativize(file).toString(),
                "status", "error",
                "error", e.getMessage()
            );
        }
    }

    private Map<String, Object> analyzeArchitecture(Path repoPath) {
        Map<String, Integer> packageCounts = new HashMap<>();
        List<String> topLevelPackages = new ArrayList<>();
        
        try (var paths = Files.walk(repoPath)) {
            paths.filter(Files::isRegularFile)
                .filter(this::isCodeFile)
                .forEach(file -> {
                    String relativePath = repoPath.relativize(file).toString();
                    String packageName = extractPackage(relativePath);
                    if (packageName != null && !packageName.isEmpty()) {
                        packageCounts.put(packageName, packageCounts.getOrDefault(packageName, 0) + 1);
                        String topLevel = packageName.split("\\.")[0];
                        if (!topLevelPackages.contains(topLevel)) {
                            topLevelPackages.add(topLevel);
                        }
                    }
                });
        } catch (IOException ignored) {}
        
        return Map.of(
            "packageCount", packageCounts.size(),
            "topLevelPackages", topLevelPackages,
            "packageDistribution", packageCounts
        );
    }

    private String extractPackage(String filePath) {
        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        if (parent != null) {
            return parent.toString().replace(FileSystems.getDefault().getSeparator(), ".");
        }
        return "";
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

