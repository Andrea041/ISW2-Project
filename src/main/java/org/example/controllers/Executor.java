package org.example.controllers;

import org.eclipse.jgit.revwalk.RevCommit;
import org.example.entities.ClassifierResults;
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
    private static final String DIRECTORY = "src/main/resources/";

    private Executor() {}

    public static void dataExtraction(String projectName) throws Exception {
        JiraExtraction jira = new JiraExtraction(projectName);
        FileCSVGenerator csv = new FileCSVGenerator(DIRECTORY, projectName);

        List<Release> releaseList = jira.getReleaseInfo();  // fetch all project's releases
        Logger.getAnonymousLogger().log(Level.INFO, "Release list fetched!");

        /* Generate CSV file of releases */
        csv.generateReleaseInfo(releaseList);

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
        List<Release> halfReleaseList = new ArrayList<>(releaseList);
        halfReleaseList.removeIf(release -> release.getIndex() > half);

        Logger.getAnonymousLogger().log(Level.INFO, "Linking tickets and commits...");
        TicketTool.linkTicketsToCommits(ticketList, commitList);    // link tickets to commits and now tickets are in their "final stage"

        /* Generate CSV file of tickets */
        csv.generateTicketInfo(ticketList);

        List<RevCommit> filteredCommit = CommitTool.filterCommit(commitList, ticketList); // filter commits

        git.getClasses(releaseList);

        for (Release release : releaseList)
            git.assignCommitsToClasses(release.getJavaClassList(), release.getCommitList(), releaseList);

        for (Release release : releaseList) {
            EvaluateMetrics compMetrics = new EvaluateMetrics(release.getJavaClassList(), filteredCommit);
            compMetrics.evaluateMetrics();
        }

        /* Walk forward */
        for (int i = 2; i <= halfReleaseList.size(); i++) {
            List<Release> walkRelease = new ArrayList<>(halfReleaseList);
            int limit = i;
            walkRelease.removeIf(release -> release.getIndex() > limit);

            List<Ticket> walkTicket = new ArrayList<>(ticketList);
            walkTicket.removeIf(ticket -> ticket.getFixedVersion().getIndex() > walkRelease.getLast().getIndex());

            Buggyness buggyness = new Buggyness();

            /* Evaluate buggyness with walk forward */
            buggyness.evaluateBuggy(walkTicket, releaseList);

            /* Generate training .csv file */
            csv.generateTrainingSet(walkRelease, i);

            FileARFFGenerator arffGenerator = new FileARFFGenerator(projectName, limit);

            /* Generate training .arff file */
            arffGenerator.csvToARFFTraining();

            List<Release> testingRelease = new ArrayList<>();
            for (Release release : releaseList) {
                if (release.getIndex() == walkRelease.getLast().getIndex()+1) {
                    testingRelease.add(release);
                    break;
                }
            }

            /* Evaluate buggyness for all classes with full ticket list */
            buggyness.evaluateBuggy(ticketList, releaseList);

            /* Generate testing .csv file */
            csv.generateTestingSet(testingRelease.getFirst().getJavaClassList(), i);

            /* Generate testing .arff file */
            arffGenerator.csvToARFFTesting();
        }
        Logger.getAnonymousLogger().log(Level.INFO, "Training set and testing set files generated!");

        List<ClassifierResults> classifierResultsList = new ArrayList<>();
        try {
            WekaClassifiers weka = new WekaClassifiers(projectName, half);
            classifierResultsList = weka.fetchWekaAnalysis();
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        }

        csv.generateWekaResultFile(classifierResultsList);

        Logger.getAnonymousLogger().log(Level.INFO, "Weka analysis completed!");
    }
}
