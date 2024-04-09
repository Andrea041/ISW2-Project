package org.example.controller;

import org.example.entity.Release;

import java.io.IOException;
import java.util.List;

public class Metrics {
    private Metrics() {}

    public static void dataExtraction(String projectName) throws IOException {
        JiraExtraction jira = new JiraExtraction(projectName);

        List<Release> releaseList = jira.getReleaseInfo();  // fetch all project's releases
    }
}
