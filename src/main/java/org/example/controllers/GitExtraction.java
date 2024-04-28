package org.example.controllers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import org.eclipse.jgit.treewalk.TreeWalk;
import org.example.entities.Release;
import org.example.tool.ReleaseTool;
import org.example.entities.JavaClass;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitExtraction {
    private final Repository repository;
    private final Git git;

    public GitExtraction(String pathToRepo, String projectName) throws IOException {
        InitCommand gitInit = Git.init();
        gitInit.setDirectory(new File(pathToRepo + "/" + projectName + "/.git"));

        this.git = Git.open(new File(pathToRepo + "/" + projectName + "/.git"));
        this.repository = git.getRepository();
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

        /* Order commits in each release by date */
        for (Release release : releaseList) {
            release.getCommitList().sort(Comparator.comparing(RevCommit::getCommitTime));
        }

        return commits;
    }

    public JavaClass getClass(RevCommit commit, Release release, List<String> classList) throws IOException {
        ObjectId tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        JavaClass cl = null;

        while (treeWalk.next()) {
            if (treeWalk.getPathString().contains(".java") && !treeWalk.getPathString().contains("/test/") && !classList.contains(treeWalk.getPathString())) {
                cl = new JavaClass(treeWalk.getPathString(),
                                new String(repository.open(treeWalk.getObjectId(0)).getBytes(),
                                StandardCharsets.UTF_8), release);   // (class_name, class_code, class_release)
                classList.add(treeWalk.getPathString());
            }
        }

        return cl;
    }
}
