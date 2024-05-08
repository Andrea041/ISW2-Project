package org.example;

import org.example.controllers.Executor;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        Properties prop = new Properties();

        try (InputStream input = new FileInputStream("src/main/java/org/example/tool/configuration.properties")) {
            prop.load(input);
        } catch (FileNotFoundException e) {
            Logger.getAnonymousLogger().log(Level.INFO, String.format("File not found, %s", e));
        }

        String projectName = prop.getProperty("PROJECT_NAME");
        String repo = prop.getProperty("REPOSITORY_PATH");

        Executor.dataExtraction(projectName);
    }
}