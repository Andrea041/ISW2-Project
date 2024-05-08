package org.example.tool;

import org.eclipse.jgit.revwalk.RevCommit;
import org.example.entities.JavaClass;
import org.example.entities.Release;
import org.example.entities.Ticket;

import java.util.ArrayList;
import java.util.List;

public class CommitTool {
    private CommitTool() {}

    public static List<RevCommit> filterCommit(List<RevCommit> commitList, List<Ticket> ticketList) {
        List<RevCommit> filteredCommitList = new ArrayList<>();

        for (RevCommit commit : commitList) {
            for (Ticket ticket : ticketList) {
                if (ticket.getCommitList().contains(commit) && !filteredCommitList.contains(commit)) {
                    filteredCommitList.add(commit);
                }
            }
        }

        return filteredCommitList;
    }

    public static Release getCommitRelease(RevCommit commit, List<Release> releaseList) {
        for (Release release : releaseList) {
            for (RevCommit releaseCommit : release.getCommitList()) {
                if (commit.getId().equals(releaseCommit.getId()))
                    return release;
            }
        }
        return null;
    }

    public static void assignCommitClass(List<JavaClass> javaClassList, String classModified, RevCommit commit) {
        for (JavaClass javaClass : javaClassList) {
            if (javaClass.getName().equals(classModified) && !javaClass.getCommitList().contains(commit)) {
                javaClass.getCommitList().add(commit);
            }
        }
    }
}
