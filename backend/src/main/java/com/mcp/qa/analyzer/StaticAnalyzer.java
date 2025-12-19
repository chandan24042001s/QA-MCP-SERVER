package com.mcp.qa.analyzer;

import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.util.*;
import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class StaticAnalyzer {

    private static final int MAX_FILE_LINES = 500;
    private static final int MAX_METHOD_LINES = 50;
    private static final int MAX_CLASS_LINES = 1000;

    public List<Map<String,Object>> analyzePath(Path root) {
        List<Map<String,Object>> results = new ArrayList<>();
        Map<String, List<Path>> sourceFiles = new HashMap<>();
        Map<String, List<Path>> testFiles = new HashMap<>();

        try {
            // Collect all source and test files
            Files.walk(root)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    String fileName = file.toString();
                    if (fileName.endsWith(".java") || fileName.endsWith(".js") || 
                        fileName.endsWith(".ts") || fileName.endsWith(".py")) {
                        
                        String baseName = getBaseFileName(fileName);
                        if (isTestFile(fileName)) {
                            testFiles.computeIfAbsent(baseName, k -> new ArrayList<>()).add(file);
                        } else {
                            sourceFiles.computeIfAbsent(baseName, k -> new ArrayList<>()).add(file);
                        }
                    }
                });

            // Check for missing tests
            for (String baseName : sourceFiles.keySet()) {
                if (!testFiles.containsKey(baseName) && !isConfigOrDataFile(baseName)) {
                    results.add(Map.of(
                        "file", sourceFiles.get(baseName).get(0).toString(),
                        "type", "MissingTest",
                        "severity", "MEDIUM"
                    ));
                }
            }

            // Analyze each source file
            for (List<Path> files : sourceFiles.values()) {
                for (Path file : files) {
                    try {
                        String content = Files.readString(file);
                        String relativePath = root.relativize(file).toString();
                        
                        analyzeFileContent(file, content, relativePath, results);
                    } catch(IOException ignored) {}
                }
            }

        } catch(IOException ignored) {}

        return results;
    }

    private void analyzeFileContent(Path file, String content, String relativePath, List<Map<String,Object>> results) {
        String[] lines = content.split("\n");
        int lineCount = lines.length;
        
        // Check file length
        if (lineCount > MAX_FILE_LINES) {
            results.add(Map.of(
                "file", relativePath,
                "type", "LongFile",
                "severity", "MEDIUM",
                "details", "File has " + lineCount + " lines (max recommended: " + MAX_FILE_LINES + ")"
            ));
        }

        // Check for code smells and issues
        checkCodeSmells(content, relativePath, results);
        checkSecurityIssues(content, relativePath, results);
        checkBestPractices(content, relativePath, results);
        checkComplexity(content, relativePath, results);
    }

    private void checkCodeSmells(String content, String relativePath, List<Map<String,Object>> results) {
        String[] lines = content.split("\n");
        
        // Check for long methods (heuristic: methods with many lines)
        int methodStartLine = -1;
        int braceCount = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.matches(".*(public|private|protected|static).*\\(.*\\).*\\{?$")) {
                methodStartLine = i;
                braceCount = countChar(line, '{') - countChar(line, '}');
            } else if (methodStartLine >= 0) {
                braceCount += countChar(line, '{') - countChar(line, '}');
                if (braceCount <= 0 && i - methodStartLine > MAX_METHOD_LINES) {
                    results.add(Map.of(
                        "file", relativePath,
                        "type", "LongMethod",
                        "severity", "MEDIUM",
                        "details", "Method starting at line " + (methodStartLine + 1) + " has " + (i - methodStartLine) + " lines"
                    ));
                    methodStartLine = -1;
                }
            }
        }

        // Check for empty catch blocks
        if (Pattern.compile("catch\\s*\\([^)]+\\)\\s*\\{\\s*\\}").matcher(content).find()) {
            results.add(Map.of(
                "file", relativePath,
                "type", "EmptyCatchBlock",
                "severity", "HIGH",
                "details", "Empty catch blocks hide errors"
            ));
        }

        // Check for print statements (should use logging)
        if (Pattern.compile("System\\.(out|err)\\.print").matcher(content).find()) {
            results.add(Map.of(
                "file", relativePath,
                "type", "PrintStatement",
                "severity", "LOW",
                "details", "Use proper logging instead of System.out/err"
            ));
        }
    }

    private void checkSecurityIssues(String content, String relativePath, List<Map<String,Object>> results) {
        // Check for hardcoded passwords/secrets
        if (Pattern.compile("(?i)(password|secret|api[_-]?key|token)\\s*[=:]\\s*[\"'][^\"']+[\"']").matcher(content).find()) {
            results.add(Map.of(
                "file", relativePath,
                "type", "HardcodedSecret",
                "severity", "HIGH",
                "details", "Potential hardcoded credentials detected"
            ));
        }

        // Check for SQL injection risks (basic check)
        if (Pattern.compile("(?i)(SELECT|INSERT|UPDATE|DELETE).*\\+.*\\$").matcher(content).find() ||
            Pattern.compile("(?i)Statement\\.executeQuery.*\\+").matcher(content).find()) {
            results.add(Map.of(
                "file", relativePath,
                "type", "SQLInjectionRisk",
                "severity", "HIGH",
                "details", "Potential SQL injection vulnerability - use parameterized queries"
            ));
        }
    }

    private void checkBestPractices(String content, String relativePath, List<Map<String,Object>> results) {
        // Check for System.exit (bad practice in web apps)
        if (content.contains("System.exit")) {
            results.add(Map.of(
                "file", relativePath,
                "type", "SystemExitUsage",
                "severity", "HIGH",
                "details", "System.exit() should not be used in application code"
            ));
        }

        // Check for TODO/FIXME comments (technical debt markers)
        long todoCount = Pattern.compile("(?i)(TODO|FIXME|XXX|HACK)").matcher(content).results().count();
        if (todoCount > 0) {
            results.add(Map.of(
                "file", relativePath,
                "type", "TechnicalDebtMarker",
                "severity", "LOW",
                "details", "Found " + todoCount + " TODO/FIXME comments"
            ));
        }

        // Check for commented out code
        long commentedBlocks = Pattern.compile("(?m)^\\s*//.*(public|private|class|function|def|const|let|var)").matcher(content).results().count();
        if (commentedBlocks > 3) {
            results.add(Map.of(
                "file", relativePath,
                "type", "CommentedCode",
                "severity", "LOW",
                "details", "Large amount of commented code detected"
            ));
        }
    }

    private void checkComplexity(String content, String relativePath, List<Map<String,Object>> results) {
        // Check cyclomatic complexity indicators
        int ifCount = countOccurrences(content, "if\\s*\\(");
        int forCount = countOccurrences(content, "for\\s*\\(");
        int whileCount = countOccurrences(content, "while\\s*\\(");
        int switchCount = countOccurrences(content, "switch\\s*\\(");
        int catchCount = countOccurrences(content, "catch\\s*\\(");
        
        int complexity = ifCount + forCount + whileCount + switchCount + catchCount;
        
        if (complexity > 20) {
            results.add(Map.of(
                "file", relativePath,
                "type", "HighComplexity",
                "severity", "MEDIUM",
                "details", "High cyclomatic complexity: " + complexity + " control flow statements"
            ));
        }

        // Check for deep nesting
        int maxNesting = calculateMaxNesting(content);
        if (maxNesting > 4) {
            results.add(Map.of(
                "file", relativePath,
                "type", "DeepNesting",
                "severity", "MEDIUM",
                "details", "Code has nesting depth of " + maxNesting + " levels"
            ));
        }
    }

    private int countOccurrences(String text, String pattern) {
        return (int) Pattern.compile(pattern).matcher(text).results().count();
    }

    private int countChar(String str, char c) {
        return (int) str.chars().filter(ch -> ch == c).count();
    }

    private int calculateMaxNesting(String content) {
        int maxDepth = 0;
        int currentDepth = 0;
        for (char c : content.toCharArray()) {
            if (c == '{') {
                currentDepth++;
                maxDepth = Math.max(maxDepth, currentDepth);
            } else if (c == '}') {
                currentDepth--;
            }
        }
        return maxDepth;
    }

    private String getBaseFileName(String filePath) {
        String fileName = Paths.get(filePath).getFileName().toString();
        // Remove extension and test suffixes
        fileName = fileName.replaceAll("(Test|Spec|Tests|Specs)\\.(java|js|ts|py)$", "");
        fileName = fileName.replaceAll("\\.(java|js|ts|py)$", "");
        return fileName.toLowerCase();
    }

    private boolean isTestFile(String filePath) {
        String lower = filePath.toLowerCase();
        return lower.contains("test") || 
               lower.contains("spec") ||
               filePath.endsWith("Test.java") ||
               filePath.endsWith("Tests.java") ||
               filePath.endsWith(".test.js") ||
               filePath.endsWith(".test.ts") ||
               filePath.endsWith("_test.py");
    }

    private boolean isConfigOrDataFile(String baseName) {
        return baseName.contains("config") || 
               baseName.contains("application") ||
               baseName.contains("main") ||
               baseName.contains("app") ||
               baseName.equals("index");
    }
}
