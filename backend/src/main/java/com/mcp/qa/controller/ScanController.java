package com.mcp.qa.controller;

import com.mcp.qa.service.ScanService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/call/scan")
public class ScanController {

    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping(path="/repository", consumes=MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> scanRepository(@RequestBody Map<String,Object> req) {
        return scanService.scanRepository(
            (String) req.get("requestId"),
            (String) ((Map)req.get("args")).get("repoUrl"),
            (String) ((Map)req.get("args")).get("branch")
        );
    }

    @PostMapping(path="/files", consumes=MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> scanFiles(@RequestBody Map<String,Object> req) {
        return scanService.scanFiles(
            (String) req.get("requestId"),
            (String) ((Map)req.get("args")).get("path")
        );
    }
}
