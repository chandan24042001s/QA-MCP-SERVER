package com.mcp.qa.service;

import com.mcp.qa.analyzer.StaticAnalyzer;
import com.mcp.qa.techdebt.TechDebtCalculator;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.*;

@Service
public class TechDebtService {

    private final StaticAnalyzer analyzer;
    private final TechDebtCalculator calculator;

    public TechDebtService(StaticAnalyzer analyzer, TechDebtCalculator calculator) {
        this.analyzer = analyzer; 
        this.calculator = calculator;
    }

    public Map<String,Object> buildReport(String reqId, String path) {
        List<Map<String,Object>> findings = analyzer.analyzePath(Path.of(path));
        Map<String,Object> debt = calculator.calculateDebt(findings);

        return Map.of(
            "requestId", reqId,
            "success", true,
            "findings", findings,
            "techDebt", debt
        );
    }
}
