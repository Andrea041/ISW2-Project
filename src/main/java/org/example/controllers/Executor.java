package org.example.controllers;

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
    private Executor() {}

    public static void dataExtraction(String projectName) throws IOException {
        JiraExtraction jira = new JiraExtraction(projectName);

        List<Release> releaseList = jira.getReleaseInfo();  // fetch all project's releases

        /* Generate CSV file */
        FileCSVGenerator.generateReleaseInfo(projectName);

        List<Ticket> ticketList = jira.fetchTickets(releaseList, projectName);  // fetch all project's list
        TicketTool.fixInconsistentTickets(ticketList, releaseList);  // fix tickets inconsistency
        ticketList.sort(Comparator.comparing(Ticket::getCreationDate));

        ProportionMethod.calculateProportion(ticketList, releaseList);

        for (Ticket ticket : ticketList) {
            System.out.printf("Ticket key: %s, IV: %s, OV: %s, FV: %s\n", ticket.getTicketKey(),
                    ticket.getInjectedVersion().getName(),
                    ticket.getOpeningVersion().getName(),
                    ticket.getFixedVersion().getName());
        }
    }
}
