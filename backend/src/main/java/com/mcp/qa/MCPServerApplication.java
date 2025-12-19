package com.mcp.qa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@SpringBootApplication
@RestController
public class MCPServerApplication {

    @GetMapping("/.well-known/mcp/manifest")
    public Map<String,Object> manifest() {
        return Map.of(
               "name", "qa-mcp-server",
               "version", "0.1",
               "description", "A QA analysis MCP server"
        );
    }

    public static void main(String[] args) {
        SpringApplication.run(MCPServerApplication.class, args);
    }
}
