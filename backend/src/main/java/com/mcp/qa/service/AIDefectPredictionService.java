package com.mcp.qa.service;

import com.mcp.qa.ai.AIDefectPredictor;
import com.mcp.qa.utils.GitUtils;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.util.Map;

@Service
public class AIDefectPredictionService {

    private final AIDefectPredictor defectPredictor;

    public AIDefectPredictionService(AIDefectPredictor defectPredictor) {
        this.defectPredictor = defectPredictor;
    }

    public Map<String, Object> predictDefects(String repoUrl, String branch) {
        Path localPath = null;
        try {
            localPath = Files.createTempDirectory("repo-ai-");
            System.out.println("Cloning repository: " + repoUrl + " (branch: " + branch + ") to " + localPath);
            GitUtils.cloneRepo(repoUrl, localPath, branch);
            
            System.out.println("Repository cloned successfully. Analyzing...");
            Map<String, Object> result = defectPredictor.predictDefects(localPath);
            
            deleteDirectory(localPath);
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            if (localPath != null) {
                deleteDirectory(localPath);
            }
            return Map.of("status", "error", "error", e.getMessage(), "stackTrace", e.getClass().getName());
        }
    }

    public Map<String, Object> predictDefectsForPath(String path) {
        try {
            Path localPath = Path.of(path);
            return defectPredictor.predictDefects(localPath);
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

