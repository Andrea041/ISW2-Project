package org.example.controllers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.example.entities.JavaClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EvaluateMetrics {
    private final List<JavaClass> javaClassList;
    private final List<RevCommit> commitList;
    private final String repoPath;
    private final String projectName;
    private static final String REPO_EXTENSION = "/.git";

    public EvaluateMetrics(List<JavaClass> javaClassList, List<RevCommit> commitList, String repoPath, String projectName) {
        this.javaClassList = javaClassList;
        this.commitList = commitList;
        this.repoPath = repoPath;
        this.projectName = projectName;
    }

    public void evaluateMetrics() throws IOException {
        fixNumber();
        authorNumber();
        findSize();
        revisionNumber();
        getLOCMetrics();
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
            javaClass.setLocSize(size.length);
        }
    }

    /* Revision number on the class is the number of commits that touch it */
    private void revisionNumber() {
        for (JavaClass javaClass : javaClassList)
            javaClass.setRevisionNumber(javaClass.getCommitList().size());
    }

    private void getLOCMetrics() throws IOException {
        for (JavaClass javaClass : javaClassList) {
            List<Integer> addedLines = new ArrayList<>();
            List<Integer> deletedLines = new ArrayList<>();

            for (RevCommit commit : javaClass.getCommitList()) {
                try (DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                    RevCommit parentCommit = commit.getParent(0);

                    df.setRepository(Git.open(new File(repoPath + projectName + REPO_EXTENSION)).getRepository());
                    df.setDiffComparator(RawTextComparator.DEFAULT);
                    List<DiffEntry> diffEntryList = df.scan(parentCommit.getTree(), commit.getTree());
                    
                    for (DiffEntry diffEntry : diffEntryList) {
                        if (diffEntry.getNewPath().equals(javaClass.getName())) {
                            int line = 0;
                            int delLine = 0;
                            for (Edit edit : df.toFileHeader(diffEntry).toEditList()) {
                                line += edit.getEndB() - edit.getBeginB();
                                delLine += edit.getEndA() - edit.getBeginA();
                            }
                            addedLines.add(line);
                            deletedLines.add(delLine);
                        }
                    }
                }
            }

            locAddedMetrics(javaClass, addedLines);
            locTouched(javaClass, addedLines, deletedLines);
            churnMetrics(javaClass, addedLines, deletedLines);
        }
    }

    private void locAddedMetrics(JavaClass javaClass, List<Integer> addedLines) {
        int maxLOC = 0;
        int sumLines = 0;

        for (Integer line : addedLines) {
            sumLines += line;

            if (sumLines > maxLOC)
                maxLOC = line;
        }

        javaClass.setLocAdded(sumLines);

        double avgLOCAdded = 1.0 * sumLines /javaClass.getRevisionNumber();
        if (javaClass.getRevisionNumber() != 0)
            javaClass.setAvgLOCAdded(avgLOCAdded);
        else
            javaClass.setAvgLOCAdded(0);

        javaClass.setMaxLOCAdded(maxLOC);
    }

    private void locTouched(JavaClass javaClass, List<Integer> addedLines, List<Integer> deletedLines) {
        int totAddedLines = 0;
        int totDeletedLines = 0;

        for (Integer line : addedLines)
            totAddedLines += line;

        for (Integer line : deletedLines)
            totDeletedLines += line;

        javaClass.setLocTouched(totAddedLines + totDeletedLines);
    }

    private void churnMetrics(JavaClass javaClass, List<Integer> addedLines, List<Integer> deletedLines) {
        int sizeLines = addedLines.size();  // or equivalent: int sizeLines = deletedLines.size()
        List<Integer> churnValues = new ArrayList<>();
        int churnSum = 0;
        int maxChurn = 0;

        for (int i = 0; i < sizeLines; i++)
            churnValues.add(Math.abs(addedLines.get(i) - deletedLines.get(i)));

        for (Integer churn : churnValues) {
            churnSum += churn;

            if (churn > maxChurn)
                maxChurn = churn;
        }

        javaClass.setChurn(churnSum);
        javaClass.setMaxChurn(maxChurn);
    }
}