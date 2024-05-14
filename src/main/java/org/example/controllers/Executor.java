package org.example.controllers;

import org.eclipse.jgit.revwalk.RevCommit;
import org.example.entities.Release;
import org.example.entities.Ticket;
import org.example.tool.CommitTool;
import org.example.tool.FileARFFGenerator;
import org.example.tool.FileCSVGenerator;
import org.example.tool.TicketTool;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Executor {

    private Executor() {}

    public static void dataExtraction(String projectName) throws IOException {
        JiraExtraction jira = new JiraExtraction(projectName);

        List<Release> releaseList = jira.getReleaseInfo();  // fetch all project's releases
        Logger.getAnonymousLogger().log(Level.INFO, "Release list fetched!");

        /* Generate CSV file of releases */
        FileCSVGenerator.generateReleaseInfo(projectName, releaseList);

        List<Ticket> ticketList = jira.fetchTickets(releaseList);  // fetch all project's list
        Logger.getAnonymousLogger().log(Level.INFO, "Ticket list fetched!");
        TicketTool.fixInconsistentTickets(ticketList, releaseList);  // fix tickets inconsistency
        ticketList.sort(Comparator.comparing(Ticket::getCreationDate)); // order ticket by creation date

        ProportionMethod.calculateProportion(ticketList, releaseList);  // compute proportion
        Logger.getAnonymousLogger().log(Level.INFO, "Proportion computed!");
        TicketTool.fixInconsistentTickets(ticketList, releaseList);

        GitExtraction git = new GitExtraction();
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

        Logger.getAnonymousLogger().log(Level.INFO, "Linking tickets and commits...");
        TicketTool.linkTicketsToCommits(ticketList, commitList);    // link tickets to commits and now tickets are in their "final stage"

        /* Generate CSV file of tickets */
        FileCSVGenerator.generateTicketInfo(projectName, ticketList);

        List<RevCommit> filteredCommit = CommitTool.filterCommit(commitList, ticketList); // filter commits

        Logger.getAnonymousLogger().log(Level.INFO, "Retrieving java classes...");
        git.getClasses(releaseList);
        Logger.getAnonymousLogger().log(Level.INFO, "Classes fetched!");

        for (Release release : releaseList)
            git.assignCommitsToClasses(release.getJavaClassList(), release.getCommitList(), releaseList);

        /* Evaluate buggyness */
        Buggyness buggyness = new Buggyness();
        buggyness.evaluateBuggy(ticketList);

        /* Compute metrics for each java class in each release */
        for (Release release : releaseList) {
            EvaluateMetrics compMetrics = new EvaluateMetrics(release.getJavaClassList(), filteredCommit);
            compMetrics.evaluateMetrics();
        }

        FileCSVGenerator.generateTrainingSet(projectName, releaseList);
        Logger.getAnonymousLogger().log(Level.INFO, "Training set file generated!");

        /* Generate .arff file */
        FileARFFGenerator arffGenerator = new FileARFFGenerator(projectName);
        arffGenerator.csvToARFF();
    }
}
