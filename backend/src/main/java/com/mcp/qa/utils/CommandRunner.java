package com.mcp.qa.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;

public class CommandRunner {

    public record Result(boolean success, String output) {}

    public static Result run(String[] cmd, int timeout) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process p = pb.start();
        StringBuilder sb = new StringBuilder();

        Instant end = Instant.now().plusSeconds(timeout);

        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while (Instant.now().isBefore(end) && (line = r.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }

        boolean ok = p.waitFor() == 0;
        return new Result(ok, sb.toString());
    }
}
