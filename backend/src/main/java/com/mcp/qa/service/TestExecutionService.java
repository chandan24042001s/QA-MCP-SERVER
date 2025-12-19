package com.mcp.qa.service;

import com.mcp.qa.utils.CommandRunner;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.nio.file.Path;

@Service
public class TestExecutionService {

    public Map<String,Object> executeTests(String reqId, String path) {
        try {
            if (!Path.of(path).resolve("pom.xml").toFile().exists()) {
                return Map.of("requestId", reqId, "success", true, "skipped", true);
            }

            var res = CommandRunner.run(new String[]{"mvn","-B","test"}, 120);
            return Map.of("requestId", reqId, "success", res.success(), "output", res.output());
        } catch(Exception e) {
            return Map.of("requestId", reqId, "success", false, "error", e.getMessage());
        }
    }
}
