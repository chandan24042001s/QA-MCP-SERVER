package com.mcp.qa.service;

import com.mcp.qa.ai.AITestGapAnalyzer;
import com.mcp.qa.utils.GitUtils;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.util.Map;

@Service
public class AITestGapService {

    private final AITestGapAnalyzer testGapAnalyzer;

    public AITestGapService(AITestGapAnalyzer testGapAnalyzer) {
        this.testGapAnalyzer = testGapAnalyzer;
    }

    public Map<String, Object> analyzeTestGaps(String repoUrl, String branch) {
        Path localPath = null;
        try {
            localPath = Files.createTempDirectory("repo-ai-");
            GitUtils.cloneRepo(repoUrl, localPath, branch);
            
            Map<String, Object> result = testGapAnalyzer.analyzeTestGaps(localPath);
            
            deleteDirectory(localPath);
            
            return result;
        } catch (Exception e) {
            if (localPath != null) {
                deleteDirectory(localPath);
            }
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    public Map<String, Object> analyzeTestGapsForPath(String path) {
        try {
            Path localPath = Path.of(path);
            return testGapAnalyzer.analyzeTestGaps(localPath);
        } catch (Exception e) {
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    private void deleteDirectory(Path path) {
        try {
            Files.walk(path)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (Exception ignored) {}
                });
        } catch (Exception ignored) {}
    }
}

