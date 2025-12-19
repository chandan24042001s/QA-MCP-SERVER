package com.mcp.qa.controller;

import com.mcp.qa.service.TechDebtService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/call/report")
public class ReportController {

    private final TechDebtService debtService;

    public ReportController(TechDebtService debtService) {
        this.debtService = debtService;
    }

    @PostMapping("/tech-debt")
    public Map<String,Object> techDebt(@RequestBody Map<String,Object> req) {
        try {
            Map<String, Object> args = (Map<String, Object>) req.get("args");
            if (args == null) {
                return Map.of("status", "error", "error", "args parameter is required");
            }
            
            String requestId = (String) req.get("requestId");
            if (requestId == null) {
                requestId = "req-" + System.currentTimeMillis();
            }
            
            String repoUrl = (String) args.get("repoUrl");
            String branch = (String) args.get("branch");
            if (branch == null || branch.isEmpty()) {
                branch = "main";
            }
            String path = (String) args.get("path");
            
            // If repoUrl is provided, use repository method
            if (repoUrl != null && !repoUrl.isEmpty()) {
                return debtService.buildReportFromRepository(requestId, repoUrl, branch);
            }
            
            // Otherwise use path method
            if (path != null && !path.isEmpty()) {
                return debtService.buildReport(requestId, path);
            }
            
            return Map.of("status", "error", "error", "repoUrl or path required in args");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                "status", "error", 
                "error", e.getMessage() != null ? e.getMessage() : "Internal Server Error",
                "errorType", e.getClass().getSimpleName()
            );
        }
    }
}
