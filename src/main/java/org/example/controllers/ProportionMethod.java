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
        float pColdStart = coldStartProportion();

        for (Ticket ticket : fixedTicketList) {
            /* Check if ticket has an injected version */
            if (ticket.getInjectedVersion() != null) {
                ticketListToProportion.add(ticket);
            }
            /* Compute proportion */
            else {
                computeProportion(ticket, ticketListToProportion, releaseList, pColdStart);
            }
        }
    }

    private static void computeProportion(Ticket ticket, List<Ticket> ticketListProp, List<Release> releaseList, float pColdStart) {
        float p;

        if (ticketListProp.size() < THRESHOLD) {
            p = pColdStart;
        }
        else {
            p = incrementProportion(ticketListProp);
        }
        settingIV(ticket, releaseList, p);
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
        int iv;

        if (ticket.getOpeningVersion().getIndex() == ticket.getFixedVersion().getIndex()
                && ticket.getInjectedVersion() == null) {
            iv = (int) (ticket.getFixedVersion().getIndex() - p);
        } else {
            /* IV=FV-((FV-OV)*P) */
            iv = (int) (ticket.getFixedVersion().getIndex()-((ticket.getFixedVersion().getIndex()-ticket.getOpeningVersion().getIndex())*p));
        }

        if (iv < 1.0) {
            iv = 1;
        }

        ticket.setInjectedVersion(releaseList.get(iv-1));
    }

    private static float incrementProportion(List<Ticket> list) {
        List<Float> proportionValue = new ArrayList<>();
        float pIncrement;
        float p;
        float pSum = 0;

        for (Ticket ticket : list) {
            p = computeP(ticket);
            proportionValue.add(p);
        }

        for (Float pValue : proportionValue) {
            pSum += pValue;
        }
        pIncrement = pSum/(proportionValue.size());

        return pIncrement;
    }

    private static float coldStartProportion() throws IOException {
        List<Float> proportionValueProjects = new ArrayList<>();    // List for other projects
        float p;
        float pColdStart;

        for (ProjectNames name : ProjectNames.values()) {
            float pSum = 0;
            List<Float> proportionValue = new ArrayList<>();    // List for single project

            JiraExtraction jira = new JiraExtraction(name.toString());

            List<Release> releaseList = jira.getReleaseInfo();  // fetch all project's releases

            List<Ticket> ticketList = jira.fetchTickets(releaseList, name.toString());  // fetch all project's list
            TicketTool.fixInconsistentTickets(ticketList, releaseList);  // fix tickets inconsistency
            ticketList.removeIf(ticket -> ticket.getInjectedVersion() == null);

            for (Ticket ticket : ticketList) {
                p = computeP(ticket);
                proportionValue.add(p);
            }

            for (Float pValue : proportionValue) {
                pSum += pValue;
            }

            proportionValueProjects.add(pSum/(proportionValue.size()));
        }

        float pSum = 0;
        for (Float P_valueTotal : proportionValueProjects) {
            pSum += P_valueTotal;
        }
        pColdStart = pSum/(proportionValueProjects.size());

        return pColdStart;
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