package org.example.controllers;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.example.entities.JavaClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.example.controllers.GitExtraction.repository;

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

    /* Fix number is the number of "valid" commits associated to the class */
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

    /* Class size (LOC) */
    private void findSize() {
        for (JavaClass javaClass : javaClassList) {
            String[] size = javaClass.getContent().split("\r\n|\r|\n");
            javaClass.setLOCSize(size.length);
        }
    }

    /* Revision number on the class is the number of commits that touch it */
    private void revisionNumber() {
        for (JavaClass javaClass : javaClassList)
            javaClass.setRevisionNumber(javaClass.getCommitList().size());
    }

    private void locAdded() throws IOException {
        for (JavaClass javaClass : javaClassList) {
            for (RevCommit commit : javaClass.getCommitList()) {
                try (DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                    RevCommit parentCommit = commit.getParent(0);

                    df.setRepository(repository);
                    df.setDiffComparator(RawTextComparator.DEFAULT);
                    List<DiffEntry> diffEntryList = df.scan(parentCommit.getTree(), commit.getTree());
                    for (DiffEntry diffEntry : diffEntryList) {
                        if (diffEntry.getNewPath().equals(javaClass.getName())) {

                        }
                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {}
            }
        }
    }
}