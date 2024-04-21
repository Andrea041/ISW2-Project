package org.example.controllers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import org.example.entities.Release;
import org.example.tool.ReleaseTool;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitExtraction {

    private GitExtraction() {}

    public static List<RevCommit> getCommits(String projectName, String pathToRepo, List<Release> releaseList) throws IOException {
        // TODO potresti fare che la repository se non presente viene clonata
        Repository repository = new FileRepositoryBuilder().setGitDir(new java.io.File(pathToRepo + "/" + projectName + "/.git")).build();
        Git git = new Git(repository);
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
}
