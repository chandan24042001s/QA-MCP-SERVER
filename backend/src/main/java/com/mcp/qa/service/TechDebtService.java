package com.mcp.qa.service;

import com.mcp.qa.analyzer.StaticAnalyzer;
import com.mcp.qa.techdebt.TechDebtCalculator;
import com.mcp.qa.utils.GitUtils;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import java.io.IOException;

@Service
public class TechDebtService {

    private final StaticAnalyzer analyzer;
    private final TechDebtCalculator calculator;

    public TechDebtService(StaticAnalyzer analyzer, TechDebtCalculator calculator) {
        this.analyzer = analyzer; 
        this.calculator = calculator;
    }

    public Map<String,Object> buildReport(String reqId, String path) {
        try {
            Path localPath = Path.of(path);
            
            // Check if path is a GitHub URL
            if (path != null && (path.startsWith("http://") || path.startsWith("https://") || path.contains("github.com"))) {
                return buildReportFromRepository(reqId, path, "main");
            }
            
            List<Map<String,Object>> findings = analyzer.analyzePath(localPath);
            Map<String,Object> debt = calculator.calculateDebt(findings);
            
            long filesScanned = countFilesScanned(localPath);

            Map<String, Object> result = new HashMap<>();
            result.put("requestId", reqId);
            result.put("status", "completed");
            result.put("success", true);
            result.put("filesScanned", filesScanned);
            result.put("findings", findings);
            result.put("techDebt", debt);
            result.put("techDebtScore", debt.get("score"));
            result.put("riskLevel", debt.get("risk"));
            result.put("totalFindings", debt.get("totalFindings"));
            result.put("highSeverityIssues", debt.get("highSeverity"));
            result.put("mediumSeverityIssues", debt.get("mediumSeverity"));
            result.put("lowSeverityIssues", debt.get("lowSeverity"));
            return result;
        } catch (Exception e) {
            return Map.of(
                "requestId", reqId,
                "status", "error",
                "success", false,
                "error", e.getMessage()
            );
        }
    }
    
    public Map<String,Object> buildReportFromRepository(String reqId, String repoUrl, String branch) {
        Path localPath = null;
        try {
            if (repoUrl == null || repoUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("Repository URL cannot be empty");
            }
            
            localPath = Files.createTempDirectory("repo-debt-");
            System.out.println("Cloning repository: " + repoUrl + " (branch: " + branch + ") to " + localPath);
            
            if (branch != null && !branch.trim().isEmpty()) {
                GitUtils.cloneRepo(repoUrl, localPath, branch);
            } else {
                GitUtils.cloneRepo(repoUrl, localPath);
            }
            
            System.out.println("Repository cloned successfully. Analyzing tech debt...");
            
            long filesScanned = countFilesScanned(localPath);
            List<Map<String,Object>> findings = analyzer.analyzePath(localPath);
            Map<String,Object> debt = calculator.calculateDebt(findings);
            
            deleteDirectory(localPath);
            localPath = null; // Mark as cleaned up
            
            Map<String, Object> result = new HashMap<>();
            result.put("requestId", reqId != null ? reqId : "unknown");
            result.put("status", "completed");
            result.put("success", true);
            result.put("repositoryUrl", repoUrl);
            result.put("branch", branch != null && !branch.trim().isEmpty() ? branch : "main");
            result.put("filesScanned", filesScanned);
            result.put("findings", findings);
            result.put("techDebt", debt);
            result.put("techDebtScore", debt.get("score"));
            result.put("riskLevel", debt.get("risk"));
            result.put("totalFindings", debt.get("totalFindings"));
            result.put("highSeverityIssues", debt.get("highSeverity"));
            result.put("mediumSeverityIssues", debt.get("mediumSeverity"));
            result.put("lowSeverityIssues", debt.get("lowSeverity"));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "Failed to generate tech debt report: " + e.getClass().getSimpleName();
            }
            
            if (localPath != null) {
                try {
                    deleteDirectory(localPath);
                } catch (Exception cleanupEx) {
                    System.err.println("Failed to cleanup temp directory: " + cleanupEx.getMessage());
                }
            }
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("requestId", reqId != null ? reqId : "unknown");
            errorResult.put("status", "error");
            errorResult.put("success", false);
            errorResult.put("error", errorMessage);
            errorResult.put("errorType", e.getClass().getSimpleName());
            return errorResult;
        }
    }
    
    private long countFilesScanned(Path root) {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(f -> {
                    String fileName = f.toString().toLowerCase();
                    return fileName.endsWith(".java") || 
                           fileName.endsWith(".js") || 
                           fileName.endsWith(".ts") || 
                           fileName.endsWith(".py") || 
                           fileName.endsWith(".go") || 
                           fileName.endsWith(".rs") ||
                           fileName.endsWith(".cpp") || 
                           fileName.endsWith(".c") ||
                           fileName.endsWith(".cs");
                })
                .count();
        } catch (IOException e) {
            return 0;
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
