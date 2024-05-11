package org.example.controllers;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.example.entities.JavaClass;
import org.example.entities.Release;
import org.example.entities.Ticket;
import org.example.tool.ClassTool;
import org.example.tool.RepoFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Buggyness {
    private final Repository repository;

    public Buggyness() {
        this.repository = RepoFactory.getRepo();
    }

    public void evaluateBuggy(List<Ticket> ticketList) throws IOException {
        for (Ticket ticket : ticketList) {
            List<Release> affectedReleaseList = new ArrayList<>(ticket.getAffectedVersionsList());
            for (Release release : affectedReleaseList) {
                labelClasses(release, ticket);
            }
        }
    }

    private void labelClasses(Release release, Ticket ticket) throws IOException {
        for (RevCommit commit : ticket.getCommitList()) {
            List<String> classNameList = ClassTool.getModifiedClass(commit, repository);
            for (JavaClass javaClass : release.getJavaClassList()) {
                if (classNameList.contains(javaClass.getName()))
                    javaClass.setBuggy(true);
            }
        }
    }
}
