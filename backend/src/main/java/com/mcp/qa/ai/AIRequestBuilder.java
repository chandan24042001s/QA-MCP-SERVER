package com.mcp.qa.ai;

import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Component
public class AIRequestBuilder {

    public String buildCodeInsightPrompt(String filePath, String codeContent, 
                                                List<Map<String, Object>> staticFindings) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following code for hidden bugs, logical errors, and architectural issues.\n\n");
        prompt.append("File: ").append(filePath).append("\n\n");
        prompt.append("Code:\n```\n").append(codeContent).append("\n```\n\n");
        
        if (!staticFindings.isEmpty()) {
            prompt.append("Static analysis findings:\n");
            for (Map<String, Object> finding : staticFindings) {
                prompt.append("- ").append(finding.get("type"))
                      .append(" (").append(finding.get("severity")).append("): ")
                      .append(finding.getOrDefault("details", "")).append("\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("Please identify:\n");
        prompt.append("1. Hidden bugs that static analysis might miss\n");
        prompt.append("2. Incorrect logical branching\n");
        prompt.append("3. Misuse of external libraries\n");
        prompt.append("4. Dangerous concurrency patterns\n");
        prompt.append("5. Edge cases where code might fail\n\n");
        prompt.append("Return JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"findings\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"type\": \"bug_type\",\n");
        prompt.append("      \"severity\": \"HIGH|MEDIUM|LOW\",\n");
        prompt.append("      \"line\": line_number,\n");
        prompt.append("      \"evidence\": \"code snippet\",\n");
        prompt.append("      \"reasoning\": \"explanation\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    public String buildDefectPredictionPrompt(String filePath, String codeContent,
                                                     List<Map<String, Object>> staticFindings,
                                                     Map<String, Object> metrics) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Predict the defect probability for this code file.\n\n");
        prompt.append("File: ").append(filePath).append("\n\n");
        prompt.append("Code:\n```\n").append(codeContent).append("\n```\n\n");
        
        if (!staticFindings.isEmpty()) {
            prompt.append("Static analysis findings: ").append(staticFindings.size()).append("\n");
        }
        
        if (metrics != null) {
            prompt.append("Complexity metrics: ").append(metrics).append("\n");
        }
        
        prompt.append("\nCalculate:\n");
        prompt.append("1. Defect probability score (0-100)\n");
        prompt.append("2. Crash probability and severity\n");
        prompt.append("3. Risk reasons\n");
        prompt.append("4. Code hotspots (line numbers)\n\n");
        prompt.append("Return JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"defectScore\": 0-100,\n");
        prompt.append("  \"crashProbability\": 0-100,\n");
        prompt.append("  \"severity\": \"HIGH|MEDIUM|LOW\",\n");
        prompt.append("  \"riskReasons\": [\"reason1\", \"reason2\"],\n");
        prompt.append("  \"hotspots\": [line1, line2]\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    public String buildTestGapPrompt(String filePath, String codeContent,
                                            List<String> existingTests,
                                            List<Map<String, Object>> staticFindings) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze test coverage gaps for this code.\n\n");
        prompt.append("File: ").append(filePath).append("\n\n");
        prompt.append("Code:\n```\n").append(codeContent).append("\n```\n\n");
        
        if (!existingTests.isEmpty()) {
            prompt.append("Existing tests:\n");
            for (String test : existingTests) {
                prompt.append("- ").append(test).append("\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("Identify:\n");
        prompt.append("1. Untested code paths\n");
        prompt.append("2. Missing edge cases\n");
        prompt.append("3. Missing negative test cases\n");
        prompt.append("4. Missing async/concurrency tests\n\n");
        prompt.append("Return JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"gaps\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"type\": \"gap_type\",\n");
        prompt.append("      \"description\": \"description\",\n");
        prompt.append("      \"suggestedTest\": \"test case description\",\n");
        prompt.append("      \"priority\": \"HIGH|MEDIUM|LOW\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    public String buildRefactorPrompt(String filePath, String codeContent,
                                            List<Map<String, Object>> staticFindings,
                                            Map<String, Object> architectureContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Provide refactoring recommendations for this code.\n\n");
        prompt.append("File: ").append(filePath).append("\n\n");
        prompt.append("Code:\n```\n").append(codeContent).append("\n```\n\n");
        
        if (architectureContext != null) {
            prompt.append("Architecture context: ").append(architectureContext).append("\n\n");
        }
        
        prompt.append("Provide:\n");
        prompt.append("1. High-level architecture improvements\n");
        prompt.append("2. Function-level refactor suggestions\n");
        prompt.append("3. Unnecessary layers to remove\n");
        prompt.append("4. Code smells to address\n");
        prompt.append("5. Package restructuring suggestions\n\n");
        prompt.append("Return JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"architecture\": [\"suggestion1\", \"suggestion2\"],\n");
        prompt.append("  \"functions\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"function\": \"function_name\",\n");
        prompt.append("      \"line\": line_number,\n");
        prompt.append("      \"suggestion\": \"refactor suggestion\",\n");
        prompt.append("      \"reason\": \"reasoning\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"codeSmells\": [\"smell1\", \"smell2\"],\n");
        prompt.append("  \"packageRestructure\": [\"suggestion1\"]\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    public String buildMemoryLeakPrompt(String filePath, String codeContent,
                                              List<Map<String, Object>> staticFindings) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Detect potential memory leaks in this code.\n\n");
        prompt.append("File: ").append(filePath).append("\n\n");
        prompt.append("Code:\n```\n").append(codeContent).append("\n```\n\n");
        
        prompt.append("Look for:\n");
        prompt.append("1. Resources not closed (files, streams, connections)\n");
        prompt.append("2. Potential leaks in loops\n");
        prompt.append("3. Unbounded collections\n");
        prompt.append("4. Observer/listener leaks\n");
        prompt.append("5. Asynchronous leak patterns\n\n");
        prompt.append("Return JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"leaks\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"type\": \"leak_type\",\n");
        prompt.append("      \"line\": line_number,\n");
        prompt.append("      \"severity\": \"HIGH|MEDIUM|LOW\",\n");
        prompt.append("      \"evidence\": \"code snippet\",\n");
        prompt.append("      \"fix\": \"suggested fix\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
}

