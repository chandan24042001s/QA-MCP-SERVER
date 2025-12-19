package com.mcp.qa.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;

import java.nio.file.Path;

public class GitUtils {

    public static void cloneRepo(String url, Path path) throws Exception {
        cloneRepo(url, path, null);
    }

    public static void cloneRepo(String url, Path path, String branch) throws Exception {
        Git git = Git.cloneRepository()
            .setURI(url)
            .setDirectory(path.toFile())
            .call();
        
        if (branch != null && !branch.isEmpty() && !branch.equals("main") && !branch.equals("master")) {
            try {
                git.checkout()
                    .setName("refs/remotes/origin/" + branch)
                    .call();
            } catch (Exception e) {
                try {
                    git.checkout()
                        .setName(branch)
                        .call();
                } catch (Exception e2) {
                    System.err.println("Warning: Could not checkout branch " + branch + ", using default branch");
                }
            }
        }
        
        git.close();
    }
}
