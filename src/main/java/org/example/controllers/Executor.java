package org.example.controllers;

import org.eclipse.jgit.revwalk.RevCommit;
import org.example.entities.Release;
import org.example.entities.Ticket;
import org.example.tool.CommitTool;
import org.example.tool.FileCSVGenerator;
import org.example.tool.TicketTool;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Executor {

    private Executor() {}

    public static void dataExtraction(String projectName, String pathToRepo) throws IOException {
        JiraExtraction jira = new JiraExtraction(projectName);

        List<Release> releaseList = jira.getReleaseInfo();  // fetch all project's releases
        Logger.getAnonymousLogger().log(Level.INFO, "Release list fetched!");

        /* Generate CSV file of releases */
        FileCSVGenerator.generateReleaseInfo(projectName, releaseList);

        List<Ticket> ticketList = jira.fetchTickets(releaseList, projectName);  // fetch all project's list
        Logger.getAnonymousLogger().log(Level.INFO, "Ticket list fetched!");
        TicketTool.fixInconsistentTickets(ticketList, releaseList);  // fix tickets inconsistency
        ticketList.sort(Comparator.comparing(Ticket::getCreationDate)); // order ticket by creation date

        ProportionMethod.calculateProportion(ticketList, releaseList);  // compute proportion
        Logger.getAnonymousLogger().log(Level.INFO, "Proportion done!");
        TicketTool.fixInconsistentTickets(ticketList, releaseList);

        GitExtraction git = new GitExtraction(pathToRepo, projectName.toLowerCase());
        List<RevCommit> commitList = git.getCommits(releaseList);    // fetch commit list
        Logger.getAnonymousLogger().log(Level.INFO, "Commit list fetched!");
        releaseList.removeIf(release -> release.getCommitList().isEmpty()); // deleting releases without commits

        /* Reassign index to each release */
        int index = 1;
        for (Release release : releaseList) {
            release.setIndex(index);
            index++;
        }

        /* Keep half past releases */
        int half = releaseList.size() / 2;
        releaseList.removeIf(release -> release.getIndex() > half);

        TicketTool.linkTicketsToCommits(ticketList, commitList);    // link tickets to commits and now tickets are in their "final stage"

        /* Generate CSV file of tickets */
        FileCSVGenerator.generateTicketInfo(projectName, ticketList);

        List<RevCommit> filteredCommit = CommitTool.filterCommit(commitList, ticketList); // filter commits

        git.getClasses(releaseList);
        Logger.getAnonymousLogger().log(Level.INFO, "Classes fetched!");

        for (Release release : releaseList)
            git.assignCommitsToClasses(release.getJavaClassList(), release.getCommitList(), releaseList);

        /* Evaluate buggyness */
        Buggyness buggyness = new Buggyness(pathToRepo, projectName.toLowerCase());
        buggyness.evaluateBuggy(ticketList, releaseList);

        /* Compute metrics for each java class in each release */
        for (Release release : releaseList) {
            EvaluateMetrics compMetrics = new EvaluateMetrics(release.getJavaClassList(), filteredCommit);
            compMetrics.evaluateMetrics();
        }

        FileCSVGenerator.generateTrainingSet(projectName, releaseList);
        Logger.getAnonymousLogger().log(Level.INFO, "Training set file generated!");
    }
}
