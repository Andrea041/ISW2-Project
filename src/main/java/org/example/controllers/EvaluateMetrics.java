package org.example.controllers;

import org.eclipse.jgit.revwalk.RevCommit;
import org.example.entities.JavaClass;

import java.util.ArrayList;
import java.util.List;

public class EvaluateMetrics {
    public static void evaluateMetrics(JavaClass javaClass, List<RevCommit> commitList) {
        fixNumber(javaClass, commitList);
        authorNumber(javaClass);
        findSize(javaClass);
        revisionNumber(javaClass);
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

    private static void authorNumber(JavaClass javaClass) {
        List<String> authorNames = new ArrayList<>();
        for (RevCommit commit : javaClass.getCommitList()) {
            if (!authorNames.contains(commit.getAuthorIdent().getName())) {
                authorNames.add(commit.getAuthorIdent().getName());
            }
        }
        javaClass.setAuthorNumber(authorNames.size());
    }

    private static void findSize(JavaClass javaClass) {
        String[] size = javaClass.getContent().split("\r\n|\r|\n");
        javaClass.setLOCSize(size.length);
    }

    private static void revisionNumber(JavaClass javaClass) {
        javaClass.setRevisionNumber(javaClass.getCommitList().size());
    }
}