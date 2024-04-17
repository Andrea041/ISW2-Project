package org.example;

import org.example.controllers.Executor;

import java.io.IOException;

public class Main {
    //private static final String PROJECT_NAME = "OPENJPA";
    private static final String PROJECT_NAME = "BOOKKEEPER";

    public static void main(String[] args) throws IOException {
        Executor.dataExtraction(PROJECT_NAME);
    }
}