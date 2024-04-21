package org.example.controllers;

import org.example.entities.Release;
import org.example.entities.Ticket;
import org.example.enumeration.ProjectNames;
import org.example.tool.TicketTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProportionMethod {
    private static final int THRESHOLD = 5;

    private ProportionMethod() {}

    /* Proportion formula used: P = (FV-IV)/(FV-OV) */
    public static void calculateProportion(List<Ticket> fixedTicketList, List<Release> releaseList) throws IOException {
        List<Ticket> ticketListToProportion = new ArrayList<>();    // list to put ticket used with proportion
        float P_coldStart = coldStartProportion();

        for (Ticket ticket : fixedTicketList) {
            /* Check if ticket has an injected version */
            if (ticket.getInjectedVersion() != null) {
                ticketListToProportion.add(ticket);
            }
            /* Compute proportion */
            else {
                computeProportion(ticket, ticketListToProportion, releaseList, P_coldStart);
            }
        }
    }

    private static void computeProportion(Ticket ticket, List<Ticket> ticketListProp, List<Release> releaseList, float P_coldStart) throws IOException {
        float P;

        if (ticketListProp.size() < THRESHOLD) {
            P = P_coldStart;
        }
        else {
            P = incrementProportion(ticketListProp);
        }
        settingIV(ticket, releaseList, P);
        settingAV(ticket, releaseList);
    }

    private static void settingAV(Ticket ticket, List<Release> releaseList) {
        List<Release> tempAV = new ArrayList<>();

        for (int i = ticket.getInjectedVersion().getIndex(); i < ticket.getFixedVersion().getIndex(); i++) {
            tempAV.add(releaseList.get(i-1));
        }

        ticket.setAffectedVersionsList(tempAV);
    }

    private static void settingIV(Ticket ticket, List<Release> releaseList, float p) {
        int IV;

        if (ticket.getOpeningVersion().getIndex() == ticket.getFixedVersion().getIndex()
                && ticket.getInjectedVersion() == null) {
            IV = (int) (ticket.getFixedVersion().getIndex() - p);
        } else {
            /* IV=FV-((FV-OV)*P) */
            IV = (int) (ticket.getFixedVersion().getIndex()-((ticket.getFixedVersion().getIndex()-ticket.getOpeningVersion().getIndex())*p));
        }

        if (IV < 1.0) {
            IV = 1;
        }

        ticket.setInjectedVersion(releaseList.get(IV-1));
    }

    private static float incrementProportion(List<Ticket> list) {
        List<Float> proportionValue = new ArrayList<>();
        float P_Increment;
        float P;
        float P_Sum = 0;

        for (Ticket ticket : list) {
            P = computeP(ticket);
            proportionValue.add(P);
        }

        for (Float P_value : proportionValue) {
            P_Sum += P_value;
        }
        P_Increment = P_Sum/(proportionValue.size());

        return P_Increment;
    }

    private static float coldStartProportion() throws IOException {
        List<Float> proportionValueProjects = new ArrayList<>();    // List for other projects
        float P;
        float P_ColdStart;

        for (ProjectNames name : ProjectNames.values()) {
            float P_Sum = 0;
            List<Float> proportionValue = new ArrayList<>();    // List for single project

            JiraExtraction jira = new JiraExtraction(name.toString());

            List<Release> releaseList = jira.getReleaseInfo();  // fetch all project's releases
            Logger.getAnonymousLogger().log(Level.INFO, "Releases extracted on "+name);

            List<Ticket> ticketList = jira.fetchTickets(releaseList, name.toString());  // fetch all project's list
            Logger.getAnonymousLogger().log(Level.INFO, "Tickets extracted on "+name);
            TicketTool.fixInconsistentTickets(ticketList, releaseList);  // fix tickets inconsistency
            ticketList.removeIf(ticket -> ticket.getInjectedVersion() == null);
            Logger.getAnonymousLogger().log(Level.INFO, "Tickets fixed on "+name);

            for (Ticket ticket : ticketList) {
                P = computeP(ticket);
                proportionValue.add(P);
            }

            for (Float P_value : proportionValue) {
                P_Sum += P_value;
            }

            proportionValueProjects.add(P_Sum/(proportionValue.size()));
        }

        float P_Sum = 0;
        for (Float P_valueTotal : proportionValueProjects) {
            P_Sum += P_valueTotal;
        }
        P_ColdStart = P_Sum/(proportionValueProjects.size());

        return P_ColdStart;
    }

    private static float computeP(Ticket ticket) {
        /* Check if FV=OV then set FV-OV=1 */
        if (ticket.getOpeningVersion().getIndex() == ticket.getFixedVersion().getIndex()) {
            return (ticket.getFixedVersion().getIndex() - ticket.getInjectedVersion().getIndex());
        }

        /* General case */
        return (ticket.getFixedVersion().getIndex() - ticket.getInjectedVersion().getIndex()) * 1.0f/(ticket.getFixedVersion().getIndex()-ticket.getOpeningVersion().getIndex());
    }
}