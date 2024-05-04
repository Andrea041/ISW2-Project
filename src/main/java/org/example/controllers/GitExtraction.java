package org.example.controllers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
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
    public static Repository repository = null;
    private final Git git;

    public GitExtraction(String pathToRepo, String projectName) throws IOException {
        InitCommand gitInit = Git.init();
        gitInit.setDirectory(new File(pathToRepo + "/" + projectName + "/.git"));

        this.git = Git.open(new File(pathToRepo + "/" + projectName + "/.git"));
        repository = git.getRepository();
    }

    public List<RevCommit> getCommits(List<Release> releaseList) throws IOException {
        // TODO potresti fare che la repository se non presente viene clonata
        List<RevCommit> commits = new ArrayList<>();

        try {
            Iterable<RevCommit> commitIterable = git.log().all().call();
            commitIterable.forEach(commits::add);
        } catch (GitAPIException e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        }

        /* Assign each commit to each release */
        for (RevCommit commit : commits) {
            Release release = ReleaseTool.fetchCommitRelease(commit.getAuthorIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), releaseList);

            if (release != null)
                release.getCommitList().add(commit);
        }

        return commits;
    }

    public void getClasses(List<Release> releaseList) throws IOException {
        for (Release release : releaseList) {
            List<String> classNameList = new ArrayList<>();
            for (RevCommit commit : release.getCommitList()) {
                RevTree tree = commit.getTree();
                TreeWalk treeWalk = new TreeWalk(repository);
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);

                while (treeWalk.next())
                    newJavaClass(treeWalk, release, classNameList);
            }
        }
    }

    private void newJavaClass(TreeWalk treeWalk, Release release, List<String> classNameList) throws IOException {
        if (treeWalk.getPathString().endsWith(".java") && !treeWalk.getPathString().contains("/test/") && !classNameList.contains(treeWalk.getPathString())) {
            classNameList.add(treeWalk.getPathString());

            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repository.open(objectId);
            byte[] bytes = loader.getBytes();

            JavaClass javaClass = new JavaClass(treeWalk.getPathString(), new String(bytes, StandardCharsets.UTF_8), release);
            release.getJavaClassList().add(javaClass);
        }
    }

    public void assignCommitsToClasses(List<JavaClass> javaClassList, List<RevCommit> commitList, List<Release> releaseList) throws IOException {
        for (RevCommit commit : commitList) {
            Release commitRelease = CommitTool.getCommitRelease(commit, releaseList);

            if (commitRelease != null) {
                List<String> classNameList = ClassTool.getModifiedClass(commit);
                for (String classes : classNameList)
                    CommitTool.assignCommitClass(javaClassList, classes, commit);
            }
        }
    }
}
