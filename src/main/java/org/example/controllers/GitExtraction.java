package org.example.controllers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.example.entities.JavaClass;
import org.example.entities.Release;
import org.example.tool.ClassTool;
import org.example.tool.CommitTool;
import org.example.tool.ReleaseTool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitExtraction {
    private final Git git;
    private final Repository repository;
    private static final String REPO_EXTENSION = "/.git";

    public GitExtraction(String pathToRepo, String projectName) throws IOException {
        this.git = Git.open(new File(pathToRepo + projectName + REPO_EXTENSION));
        this.repository = git.getRepository();
    }

    public List<RevCommit> getCommits(List<Release> releaseList) {
        List<RevCommit> commits = new ArrayList<>();
        try {
            Iterable<RevCommit> commitIterable = git.log().all().call();
            commitIterable.forEach(commits::add);
        } catch (GitAPIException | IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Error while extracting commits", e);
        }
        assignCommitsToReleases(commits, releaseList);
        return commits;
    }

    private void assignCommitsToReleases(List<RevCommit> commits, List<Release> releaseList) {
        for (RevCommit commit : commits) {
            Release release = ReleaseTool.fetchCommitRelease(commit.getAuthorIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), releaseList);
            if (release != null) {
                release.getCommitList().add(commit);
            }
        }
    }

    public void getClasses(List<Release> releaseList) {
        for (Release release : releaseList) {
            List<String> classNameList = new ArrayList<>();
            for (RevCommit commit : release.getCommitList()) {
                extractJavaClasses(commit, release, classNameList);
            }
        }
    }

    private void extractJavaClasses(RevCommit commit, Release release, List<String> classNameList) {
        try {
            RevTree tree = commit.getTree();
            TreeWalk treeWalk = new TreeWalk(repository);
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);

            while (treeWalk.next()) {
                String pathString = treeWalk.getPathString();
                if (pathString.endsWith(".java") && !pathString.contains("/test/") && !classNameList.contains(pathString)) {
                    classNameList.add(pathString);
                    extractAndAssignJavaClass(treeWalk, release);
                }
            }
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Error while extracting Java classes", e);
        }
    }

    private void extractAndAssignJavaClass(TreeWalk treeWalk, Release release) {
        try {
            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repository.open(objectId);
            byte[] bytes = loader.getBytes();
            JavaClass javaClass = new JavaClass(treeWalk.getPathString(), new String(bytes, StandardCharsets.UTF_8), release);
            release.getJavaClassList().add(javaClass);
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Error while extracting Java class", e);
        }
    }

    public void assignCommitsToClasses(List<JavaClass> javaClassList, List<RevCommit> commitList, List<Release> releaseList) throws IOException {
        for (RevCommit commit : commitList) {
            Release commitRelease = CommitTool.getCommitRelease(commit, releaseList);
            if (commitRelease != null) {
                List<String> classNameList = ClassTool.getModifiedClass(commit, repository);
                for (String classes : classNameList) {
                    CommitTool.assignCommitClass(javaClassList, classes, commit);
                }
            }
        }
    }
}
