package com.mcp.qa.techdebt;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class TechDebtCalculator {

    public Map<String,Object> calculateDebt(List<Map<String,Object>> findings) {
        int score = 0;
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;

        for (Map<String,Object> finding : findings) {
            String severity = (String) finding.getOrDefault("severity", "LOW");
            switch (severity) {
                case "HIGH":
                    score += 10;
                    highCount++;
                    break;
                case "MEDIUM":
                    score += 5;
                    mediumCount++;
                    break;
                case "LOW":
                default:
                    score += 2;
                    lowCount++;
                    break;
            }
        }

        if (findings.size() > 0) {
            score += findings.size();
        }

        String risk =
            score > 100 ? "HIGH" :
            score > 50 ? "MEDIUM" :
            score > 20 ? "LOW" :
            "MINIMAL";

        return Map.of(
            "totalFindings", findings.size(),
            "score", score,
            "risk", risk,
            "highSeverity", highCount,
            "mediumSeverity", mediumCount,
            "lowSeverity", lowCount
        );
    }
}
