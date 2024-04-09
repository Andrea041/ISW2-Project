package org.example;

import org.example.controller.Metrics;

import java.io.IOException;

public class Main {
    private static final String PROJECT_NAME = "BOOKKEEPER";

    public static void main(String[] args) throws IOException {
        Metrics.dataExtraction(PROJECT_NAME);
    }
}