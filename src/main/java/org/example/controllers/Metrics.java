package org.example.controllers;

import org.example.entities.Release;
import org.example.entities.Ticket;
import org.example.tools.TicketTool;

import java.io.IOException;
import java.util.List;

public class Metrics {
    private Metrics() {}

    public static void dataExtraction(String projectName) throws IOException {
        JiraExtraction jira = new JiraExtraction(projectName);

        List<Release> releaseList = jira.getReleaseInfo();  // fetch all project's releases
        List<Ticket> ticketList = jira.fetchTickets(releaseList, projectName);  // fetch all project's list

        TicketTool.fixInconsistentTickets(ticketList, releaseList);  // fix tickets inconsistency

        ProportionMethod.calculateProportion(ticketList, releaseList);

        for (Ticket ticket : ticketList) {
            if (ticket.getInjectedVersion() != null) {
                System.out.printf("Ticket key: %s, IV: %s, OV: %s, FV: %s\n", ticket.getTicketKey(), ticket.getInjectedVersion().getName(), ticket.getOpeningVersion().getName(), ticket.getFixedVersion().getName());
            }
        }
    }
}
