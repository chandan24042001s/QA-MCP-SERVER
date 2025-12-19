package com.mcp.qa.controller;

import com.mcp.qa.service.TestExecutionService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/call/test")
public class TestController {

    private final TestExecutionService testService;

    public TestController(TestExecutionService testService) {
        this.testService = testService;
    }

    @PostMapping("/run")
    public Map<String,Object> runTests(@RequestBody Map<String,Object> req) {
        return testService.executeTests(
            (String) req.get("requestId"),
            (String) ((Map)req.get("args")).get("path")
        );
    }
}
