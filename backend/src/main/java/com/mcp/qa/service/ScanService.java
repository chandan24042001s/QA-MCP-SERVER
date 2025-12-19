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
public class ScanService {

    private final StaticAnalyzer analyzer;
    private final TechDebtCalculator calculator;

    public ScanService(StaticAnalyzer analyzer, TechDebtCalculator calculator) {
        this.analyzer = analyzer;
        this.calculator = calculator;
    }

    public Map<String,Object> scanRepository(String reqId, String repoUrl, String branch) {
        Path localPath = null;
        try {
            localPath = Files.createTempDirectory("repo-");
            if (branch != null && !branch.isEmpty()) {
                GitUtils.cloneRepo(repoUrl, localPath, branch);
            } else {
                GitUtils.cloneRepo(repoUrl, localPath);
            }

            long filesScanned = countFilesScanned(localPath);
            
            var findings = analyzer.analyzePath(localPath);
            
            var debtInfo = calculator.calculateDebt(findings);
            int techDebtScore = (Integer) debtInfo.get("score");
            String riskLevel = (String) debtInfo.get("risk");

            return Map.of(
                    "status", "completed",
                    "filesScanned", filesScanned,
                    "techDebtScore", techDebtScore,
                    "riskLevel", riskLevel,
                    "totalFindings", debtInfo.get("totalFindings"),
                    "highSeverityIssues", debtInfo.get("highSeverity"),
                    "mediumSeverityIssues", debtInfo.get("mediumSeverity"),
                    "lowSeverityIssues", debtInfo.get("lowSeverity")
            );

        } catch(Exception e) {
            e.printStackTrace();
            if (localPath != null) {
                try {
                    Files.walk(localPath)
                        .sorted((a, b) -> -a.compareTo(b))
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (Exception ignored) {}
                        });
                } catch (Exception ignored) {}
            }
            return Map.of("status", "error", "error", e.getMessage() != null ? e.getMessage() : "Unknown error");
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

    public Map<String,Object> scanFiles(String reqId, String path) {
        try {
            Path localPath = Path.of(path);
            
            long filesScanned = countFilesScanned(localPath);
            
            var findings = analyzer.analyzePath(localPath);
            
            var debtInfo = calculator.calculateDebt(findings);
            int techDebtScore = (Integer) debtInfo.get("score");
            String riskLevel = (String) debtInfo.get("risk");

            return Map.of(
                    "status", "completed",
                    "filesScanned", filesScanned,
                    "techDebtScore", techDebtScore,
                    "riskLevel", riskLevel,
                    "totalFindings", debtInfo.get("totalFindings"),
                    "highSeverityIssues", debtInfo.get("highSeverity"),
                    "mediumSeverityIssues", debtInfo.get("mediumSeverity"),
                    "lowSeverityIssues", debtInfo.get("lowSeverity")
            );
        } catch(Exception e) {
            return Map.of("status", "error", "error", e.getMessage());
        }
    }
}
