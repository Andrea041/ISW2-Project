package org.example.controllers;

import org.eclipse.jgit.revwalk.RevCommit;
import org.example.entities.JavaClass;
import org.example.entities.Release;
import org.example.entities.Ticket;
import org.example.tool.CommitTool;
import org.example.tool.FileCSVGenerator;
import org.example.tool.TicketTool;

import java.io.IOException;
import java.util.*;

public class Executor {
    private static final String PATH_TO_REPO = "/Users/andreaandreoli/ISW2_REPO";

    private Executor() {}

    public static void dataExtraction(String projectName) throws IOException {
        JiraExtraction jira = new JiraExtraction(projectName);

        List<Release> releaseList = jira.getReleaseInfo();  // fetch all project's releases

        /* Generate CSV file of releases */
        FileCSVGenerator.generateReleaseInfo(projectName);

        List<Ticket> ticketList = jira.fetchTickets(releaseList, projectName);  // fetch all project's list
        TicketTool.fixInconsistentTickets(ticketList, releaseList);  // fix tickets inconsistency
        ticketList.sort(Comparator.comparing(Ticket::getCreationDate)); // order ticket by creation date

        /* Generate CSV file of tickets */
        FileCSVGenerator.generateTicketInfo(projectName, ticketList);

        ProportionMethod.calculateProportion(ticketList, releaseList);  // compute proportion

        GitExtraction git = new GitExtraction(PATH_TO_REPO, projectName.toLowerCase());
        List<RevCommit> commitList = git.getCommits(releaseList);    // fetch commit list

        for (RevCommit commit : commitList) {
            for (Release release : releaseList) {
                if (release.getCommitList().contains(commit)) {
                    release.setJavaClassList(git.getClasses(commit, release));
                    for (JavaClass javaClass : release.getJavaClassList()) {
                        javaClass.setCommitList(commit);
                    }
                }
            }
        }

        TicketTool.linkTicketsToCommits(ticketList, commitList);    // link tickets to commits and now tickets are in their final "stage"
        List<RevCommit> filteredCommit = CommitTool.filterCommit(commitList, ticketList); // filter commits

        Buggyness buggyness = new Buggyness(PATH_TO_REPO, projectName.toLowerCase());
        buggyness.evaluateBuggy(ticketList, releaseList);

        /* Compute metrics for each java class in each release */
        for (Release release : releaseList) {
            for (JavaClass javaClass : release.getJavaClassList()) {
                EvaluateMetrics.evaluateMetrics(javaClass, release, filteredCommit);
                System.out.println(javaClass.getFixNumber());
            }
        }
    }
}
