package org.example.controllers;

import org.eclipse.jgit.revwalk.RevCommit;
import org.example.entities.JavaClass;
import org.example.entities.Release;

import java.util.List;

public class EvaluateMetrics {
    public static void evaluateMetrics(JavaClass javaClass, Release release, List<RevCommit> commitList) {
        fixNumber(javaClass, commitList);
    }

    private static void fixNumber(JavaClass javaClass, List<RevCommit> commitList) {
        int count = 0;
        for (RevCommit commit : javaClass.getCommitList()) {
            if (commitList.contains(commit)) {
                count++;
            }
        }
        javaClass.setFixNumber(count);
    }
}