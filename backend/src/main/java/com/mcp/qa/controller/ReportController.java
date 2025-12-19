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
        return debtService.buildReport(
            (String) req.get("requestId"),
            (String) ((Map)req.get("args")).get("path")
        );
    }
}
