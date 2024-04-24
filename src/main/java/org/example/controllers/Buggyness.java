package org.example.controllers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.example.entities.JavaClass;
import org.example.entities.Release;
import org.example.entities.Ticket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Buggyness {
    private final Repository repository;

    private static final String DELETE = "DELETE";
    private static final String MODIFY = "MODIFY";

    public Buggyness (String pathToRepo, String projectName) throws IOException {
        InitCommand gitInit = Git.init();
        gitInit.setDirectory(new File(pathToRepo + "/" + projectName + "/.git"));

        Git git = Git.open(new File(pathToRepo + "/" + projectName + "/.git"));
        this.repository = git.getRepository();
    }

    public void evaluateBuggy(List<Ticket> ticketList, List<Release> releaseList) throws IOException {
        for (Ticket ticket : ticketList) {
            /* For each ticket fetch the affected versions */
            List<Release> affectedVersions = new ArrayList<>(ticket.getAffectedVersionsList());

            /* Each ticket can be linked to one or more commit */
            for (RevCommit commit : ticket.getCommitList()) {
                /* Evaluate commit difference with his parent,
                if a parent commit exist and there are some differences with respect to its subsequent commit, then it's probably buggy */
                List<DiffEntry> diffEntryList = getDiffs(commit);
                if (diffEntryList != null) {
                    findBug(diffEntryList, releaseList, affectedVersions);
                }
            }
        }
    }

    private List<DiffEntry> getDiffs(RevCommit commit) throws IOException {
        List<DiffEntry> diffEntries;

        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(repository);
        diffFormatter.setContext(0);
        diffFormatter.setDetectRenames(true);

        /* Check if there are some differences between commit and parent commit (if exists) */
        if (commit.getParentCount() != 0) {
            RevCommit parentCommit = (RevCommit) commit.getParent(0).getId();   // fetch parent commit
            diffEntries = diffFormatter.scan(parentCommit.getTree(), commit.getTree());
        } else {
            RevWalk revWalk = new RevWalk(repository);
            diffEntries = diffFormatter.scan(new EmptyTreeIterator(), new CanonicalTreeParser(null, revWalk.getObjectReader(), commit.getTree()));
        }

        return diffEntries;
    }

    private void findBug(List<DiffEntry> diffEntryList, List<Release> releaseList, List<Release> affectedReleases) {
        for (DiffEntry diffEntry : diffEntryList) {
            /* Check if file in the commit is with extension .java, else discard it */
            if (diffEntry.getNewPath().endsWith(".java")) {
                String filePath;

                /* Check commit change type */
                if (diffEntry.getChangeType().toString().equals(DELETE) || diffEntry.getChangeType().toString().equals(MODIFY)) {
                    filePath = diffEntry.getOldPath();  // old path is probably where was the bug
                } else {    // this is in case of an ADD type cause old path is /dev/null, maybe an added file can be buggy
                    filePath = diffEntry.getNewPath();
                }

                /* Check for buggy classes */
                for (Release release : releaseList) {
                    for (JavaClass javaClass : release.getJavaClassList()) {
                        if (javaClass.getName().equals(filePath) && affectedReleases.contains(release)) {
                            javaClass.setBuggy(true);
                        }
                    }
                }
            }
        }
    }
}
