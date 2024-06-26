package org.example.tool;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RepoFactory {
    private static Repository repository;
    private static Git git;

    private RepoFactory() {}

    static {
        Properties prop = new Properties();

        try (InputStream input = new FileInputStream("src/main/java/org/example/tool/configuration.properties")) {
            prop.load(input);
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        }

        String projectName = prop.getProperty("PROJECT_NAME");
        String repo = prop.getProperty("REPOSITORY_PATH");

        Path tempDir = Paths.get("src/main/resources/repo/" + projectName);
        if (!Files.exists(tempDir)) {
            try {
                Files.createDirectories(tempDir);
            } catch (IOException e) {
                Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
            }

            try {
                git = Git.cloneRepository()
                        .setURI(repo)
                        .setDirectory(tempDir.toFile())
                        .call();

                repository = git.getRepository();
                Logger.getAnonymousLogger().log(Level.INFO, "Repository cloned!");
            } catch (GitAPIException e) {
                Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
            }
        } else {
            try {
                git = Git.open(tempDir.toFile());
                repository = git.getRepository();
            } catch (IOException e) {
                Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
            }
        }
    }

    public static Repository getRepo() {
        return repository;
    }

    public static Git getGit() {
        return git;
    }
}
