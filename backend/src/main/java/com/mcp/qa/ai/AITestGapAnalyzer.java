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
import java.util.regex.Pattern;

@Component
public class AITestGapAnalyzer {

    private final AIClient aiClient;
    private final AIRequestBuilder requestBuilder;
    private final AIResponseParser responseParser;
    private final StaticAnalyzer staticAnalyzer;
    private final ExecutorService executorService;

    public AITestGapAnalyzer(AIClient aiClient, AIRequestBuilder requestBuilder,
                             AIResponseParser responseParser, StaticAnalyzer staticAnalyzer) {
        this.aiClient = aiClient;
        this.requestBuilder = requestBuilder;
        this.responseParser = responseParser;
        this.staticAnalyzer = staticAnalyzer;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public Map<String, Object> analyzeTestGaps(Path repoPath) {
        try {
            List<Map<String, Object>> staticFindings = staticAnalyzer.analyzePath(repoPath);
            
            Map<String, Path> sourceFiles = new HashMap<>();
            Map<String, List<String>> testFilesBySource = new HashMap<>();
            
            try (var paths = Files.walk(repoPath)) {
                paths.filter(Files::isRegularFile)
                    .filter(this::isCodeFile)
                    .forEach(file -> {
                        String relativePath = repoPath.relativize(file).toString();
                        if (isTestFile(file)) {
                            String sourceBase = getSourceBaseName(relativePath);
                            testFilesBySource.computeIfAbsent(sourceBase, k -> new ArrayList<>())
                                .add(relativePath);
                        } else {
                            String baseName = getBaseFileName(relativePath);
                            sourceFiles.put(baseName, file);
                        }
                    });
            }
            
            List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
            
            for (Map.Entry<String, Path> entry : sourceFiles.entrySet()) {
                String baseName = entry.getKey();
                Path sourceFile = entry.getValue();
                List<String> testFiles = testFilesBySource.getOrDefault(baseName, Collections.emptyList());
                
                CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
                    String relativePath = repoPath.relativize(sourceFile).toString();
                    List<Map<String, Object>> fileFindings = staticFindings.stream()
                        .filter(f -> relativePath.equals(f.get("file")))
                        .collect(Collectors.toList());
                    
                    return analyzeFileTestGaps(sourceFile, repoPath, testFiles, fileFindings);
                }, executorService);
                futures.add(future);
            }
            
            List<Map<String, Object>> gapAnalyses = futures.stream()
                .map(CompletableFuture::join)
                .filter(result -> result.containsKey("gaps"))
                .collect(Collectors.toList());
            
            int totalGaps = gapAnalyses.stream()
                .mapToInt(g -> ((List<?>) g.getOrDefault("gaps", Collections.emptyList())).size())
                .sum();
            
            return Map.of(
                "status", "completed",
                "totalFilesAnalyzed", gapAnalyses.size(),
                "totalGaps", totalGaps,
                "analyses", gapAnalyses
            );
            
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "error", e.getMessage()
            );
        }
    }

    private Map<String, Object> analyzeFileTestGaps(Path file, Path repoRoot,
                                                    List<String> existingTests,
                                                    List<Map<String, Object>> staticFindings) {
        try {
            String content = Files.readString(file);
            String relativePath = repoRoot.relativize(file).toString();
            
            if (content.length() > 50000) {
                return Map.of("file", relativePath, "status", "skipped");
            }
            
            String prompt = requestBuilder.buildTestGapPrompt(relativePath, content, existingTests, staticFindings);
            String systemPrompt = "You are an expert QA engineer specializing in test coverage analysis. " +
                                "Identify missing test cases, edge cases, and negative scenarios. " +
                                "Return only valid JSON.";
            
            String aiResponse = aiClient.callAI(systemPrompt, prompt);
            List<Map<String, Object>> gaps = responseParser.parseTestGaps(aiResponse);
            
            return Map.of(
                "file", relativePath,
                "gaps", gaps,
                "gapCount", gaps.size()
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

    private boolean isTestFile(Path file) {
        String fileName = file.toString().toLowerCase();
        return fileName.contains("test") || 
               fileName.contains("spec") ||
               fileName.endsWith("test.java") ||
               fileName.endsWith("tests.java") ||
               fileName.endsWith(".test.js") ||
               fileName.endsWith(".test.ts") ||
               fileName.endsWith("_test.py");
    }

    private String getBaseFileName(String filePath) {
        String fileName = Paths.get(filePath).getFileName().toString();
        fileName = fileName.replaceAll("\\.(java|js|ts|py|jsx|tsx)$", "");
        return fileName.toLowerCase();
    }

    private String getSourceBaseName(String testFilePath) {
        String base = getBaseFileName(testFilePath);
        base = base.replaceAll("(test|spec|tests|specs)$", "");
        return base;
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

