package org.example.controllers;

import org.eclipse.jgit.revwalk.RevCommit;
import org.example.entities.Release;
import org.example.entities.Ticket;
import org.example.tool.FileCSVGenerator;
import org.example.tool.TicketTool;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Executor {
    private static final String PATH_TO_REPO = "/Users/andreaandreoli/OneDrive - Universita' degli Studi di Roma Tor Vergata";

    private Executor() {}

    public static void dataExtraction(String projectName) throws IOException {
        JiraExtraction jira = new JiraExtraction(projectName);

        List<Release> releaseList = jira.getReleaseInfo();  // fetch all project's releases

        /* Generate CSV file of releases */
        FileCSVGenerator.generateReleaseInfo(projectName);

        List<Ticket> ticketList = jira.fetchTickets(releaseList, projectName);  // fetch all project's list
        TicketTool.fixInconsistentTickets(ticketList, releaseList);  // fix tickets inconsistency
        ticketList.sort(Comparator.comparing(Ticket::getCreationDate)); // order ticket by creation date

        ProportionMethod.calculateProportion(ticketList, releaseList);  // compute proportion

        /* Generate CSV file of tickets */
        FileCSVGenerator.generateTicketInfo(projectName, ticketList);

        List<RevCommit> commitList = GitExtraction.getCommits(projectName.toLowerCase(), PATH_TO_REPO, releaseList);

    }
}
