package com.mcp.qa.service;

import com.mcp.qa.ai.AIMemoryLeakPredictor;
import com.mcp.qa.utils.GitUtils;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.util.Map;

@Service
public class AIMemoryLeakService {

    private final AIMemoryLeakPredictor memoryLeakPredictor;

    public AIMemoryLeakService(AIMemoryLeakPredictor memoryLeakPredictor) {
        this.memoryLeakPredictor = memoryLeakPredictor;
    }

    public Map<String, Object> predictMemoryLeaks(String repoUrl, String branch) {
        Path localPath = null;
        try {
            localPath = Files.createTempDirectory("repo-ai-");
            GitUtils.cloneRepo(repoUrl, localPath, branch);
            
            Map<String, Object> result = memoryLeakPredictor.predictMemoryLeaks(localPath);
            
            deleteDirectory(localPath);
            
            return result;
        } catch (Exception e) {
            if (localPath != null) {
                deleteDirectory(localPath);
            }
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    public Map<String, Object> predictMemoryLeaksForPath(String path) {
        try {
            Path localPath = Path.of(path);
            return memoryLeakPredictor.predictMemoryLeaks(localPath);
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

