package org.example.controllers;

import org.eclipse.jgit.revwalk.RevCommit;
import org.example.entities.JavaClass;

import java.util.ArrayList;
import java.util.List;

public class EvaluateMetrics {
    private final List<JavaClass> javaClassList;
    private final List<RevCommit> commitList;

    public EvaluateMetrics(List<JavaClass> javaClassList, List<RevCommit> commitList) {
        this.javaClassList = javaClassList;
        this.commitList = commitList;
    }

    public void evaluateMetrics() {
        fixNumber();
        authorNumber();
        findSize();
        revisionNumber();
    }

    private void fixNumber() {
        for (JavaClass javaClass : javaClassList) {
            int count = 0;
            for (RevCommit commit : javaClass.getCommitList()) {
                if (commitList.contains(commit)) {
                    count++;
                }
            }
            javaClass.setFixNumber(count);
        }
    }

    private void authorNumber() {
        for (JavaClass javaClass : javaClassList) {
            List<String> authorNames = new ArrayList<>();
            for (RevCommit commit : javaClass.getCommitList()) {
                if (!authorNames.contains(commit.getAuthorIdent().getName())) {
                    authorNames.add(commit.getAuthorIdent().getName());
                }
            }
            javaClass.setAuthorNumber(authorNames.size());
        }
    }

    private void findSize() {
        for (JavaClass javaClass : javaClassList) {
            String[] size = javaClass.getContent().split("\r\n|\r|\n");
            javaClass.setLOCSize(size.length);
        }
    }

    private void revisionNumber() {
        for (JavaClass javaClass : javaClassList)
            javaClass.setRevisionNumber(javaClass.getCommitList().size());
    }
}