package com.mcp.qa.controller;

import com.mcp.qa.service.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/call/ai")
public class AIController {

    private final AICodeInsightService codeInsightService;
    private final AIDefectPredictionService defectPredictionService;
    private final AITestGapService testGapService;
    private final AIRefactorService refactorService;
    private final AIMemoryLeakService memoryLeakService;

    public AIController(
            AICodeInsightService codeInsightService,
            AIDefectPredictionService defectPredictionService,
            AITestGapService testGapService,
            AIRefactorService refactorService,
            AIMemoryLeakService memoryLeakService) {
        this.codeInsightService = codeInsightService;
        this.defectPredictionService = defectPredictionService;
        this.testGapService = testGapService;
        this.refactorService = refactorService;
        this.memoryLeakService = memoryLeakService;
    }

    @PostMapping(path = "/code_insights", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> codeInsights(@RequestBody Map<String, Object> req) {
        try {
            Map<String, Object> args = (Map<String, Object>) req.get("args");
            if (args == null) {
                return Map.of("status", "error", "error", "args parameter is required");
            }
            
            String repoUrl = (String) args.get("repoUrl");
            String branch = (String) args.getOrDefault("branch", "main");
            
            if (repoUrl != null && !repoUrl.isEmpty()) {
                return codeInsightService.analyzeRepository(repoUrl, branch);
            }
            
            String path = (String) args.get("path");
            if (path != null && !path.isEmpty()) {
                return codeInsightService.analyzePath(path);
            }
            
            return Map.of("status", "error", "error", "repoUrl or path required in args");
        } catch (Exception e) {
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    @PostMapping(path = "/defect_prediction", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> defectPrediction(@RequestBody Map<String, Object> req) {
        try {
            Map<String, Object> args = (Map<String, Object>) req.get("args");
            if (args == null) {
                return Map.of("status", "error", "error", "args parameter is required");
            }
            
            String repoUrl = (String) args.get("repoUrl");
            String branch = (String) args.getOrDefault("branch", "main");
            
            if (repoUrl != null && !repoUrl.isEmpty()) {
                return defectPredictionService.predictDefects(repoUrl, branch);
            }
            
            String path = (String) args.get("path");
            if (path != null && !path.isEmpty()) {
                return defectPredictionService.predictDefectsForPath(path);
            }
            
            return Map.of("status", "error", "error", "repoUrl or path required in args");
        } catch (Exception e) {
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    @PostMapping(path = "/test_gap_analysis", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> testGapAnalysis(@RequestBody Map<String, Object> req) {
        try {
            Map<String, Object> args = (Map<String, Object>) req.get("args");
            if (args == null) {
                return Map.of("status", "error", "error", "args parameter is required");
            }
            
            String repoUrl = (String) args.get("repoUrl");
            String branch = (String) args.getOrDefault("branch", "main");
            
            if (repoUrl != null && !repoUrl.isEmpty()) {
                return testGapService.analyzeTestGaps(repoUrl, branch);
            }
            
            String path = (String) args.get("path");
            if (path != null && !path.isEmpty()) {
                return testGapService.analyzeTestGapsForPath(path);
            }
            
            return Map.of("status", "error", "error", "repoUrl or path required in args");
        } catch (Exception e) {
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    @PostMapping(path = "/refactor_advisor", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> refactorAdvisor(@RequestBody Map<String, Object> req) {
        try {
            Map<String, Object> args = (Map<String, Object>) req.get("args");
            if (args == null) {
                return Map.of("status", "error", "error", "args parameter is required");
            }
            
            String repoUrl = (String) args.get("repoUrl");
            String branch = (String) args.getOrDefault("branch", "main");
            
            if (repoUrl != null && !repoUrl.isEmpty()) {
                return refactorService.analyzeRefactoring(repoUrl, branch);
            }
            
            String path = (String) args.get("path");
            if (path != null && !path.isEmpty()) {
                return refactorService.analyzeRefactoringForPath(path);
            }
            
            return Map.of("status", "error", "error", "repoUrl or path required in args");
        } catch (Exception e) {
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    @PostMapping(path = "/memory_leak_prediction", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> memoryLeakPrediction(@RequestBody Map<String, Object> req) {
        try {
            Map<String, Object> args = (Map<String, Object>) req.get("args");
            if (args == null) {
                return Map.of("status", "error", "error", "args parameter is required");
            }
            
            String repoUrl = (String) args.get("repoUrl");
            String branch = (String) args.getOrDefault("branch", "main");
            
            if (repoUrl != null && !repoUrl.isEmpty()) {
                return memoryLeakService.predictMemoryLeaks(repoUrl, branch);
            }
            
            String path = (String) args.get("path");
            if (path != null && !path.isEmpty()) {
                return memoryLeakService.predictMemoryLeaksForPath(path);
            }
            
            return Map.of("status", "error", "error", "repoUrl or path required in args");
        } catch (Exception e) {
            return Map.of("status", "error", "error", e.getMessage());
        }
    }
}


