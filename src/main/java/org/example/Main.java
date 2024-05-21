package org.example;

import org.example.controllers.Executor;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        Properties prop = new Properties();

        try (InputStream input = new FileInputStream("src/main/java/org/example/tool/configuration.properties")) {
            prop.load(input);
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.INFO, String.format("File not found, %s", e));
        }

        String projectName = prop.getProperty("PROJECT_NAME");

        try {
            Executor.dataExtraction(projectName);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, String.format("Error during program execution flow %s", e));
        }
    }
}