package org.example.controllers;

import org.example.entities.Release;
import org.example.entities.Ticket;

import java.util.ArrayList;
import java.util.List;

public class ProportionMethod {
    private static final int THRESHOLD = 5;

    private ProportionMethod() {}

    /* Proportion formula used: P = (FV-IV)/(FV-OV) */
    public static void calculateProportion(List<Ticket> fixedTicketList, List<Release> releaseList) {
        List<Ticket> ticketListToProportion = new ArrayList<>();    // list to put ticket used with proportion

        for (Ticket ticket : fixedTicketList) {
            /* Check if ticket has an injected version */
            if (ticket.getInjectedVersion() != null) {
                ticketListToProportion.add(ticket);
            }
            /* Compute proportion */
            else {
                computeProportion(ticket, ticketListToProportion, releaseList);
            }
        }
    }

    private static void computeProportion(Ticket ticket, List<Ticket> ticketListProp, List<Release> releaseList) {
        float P;
        float IV;

        if (ticketListProp.size() < THRESHOLD) {
            P = coldStartProportion(ticketListProp);

            /* Check for FV=OV case */
            if (ticket.getOpeningVersion().getIndex() == ticket.getFixedVersion().getIndex()
                    && ticket.getInjectedVersion() == null) {
                IV = P;

                ticket.setInjectedVersion(releaseList.get(((int) IV)-1));
            }

            IV = (ticket.getFixedVersion().getIndex()-ticket.getOpeningVersion().getIndex())*P;
            ticket.setInjectedVersion(releaseList.get(((int) IV)-1));
        }
        else {
            P = incrementProportion(ticketListProp);

            /* Check for FV=OV case */
            if (ticket.getOpeningVersion().getIndex() == ticket.getFixedVersion().getIndex()
                    && ticket.getInjectedVersion() == null) {
                IV = P;

                ticket.setInjectedVersion(releaseList.get(((int) IV)-1));
            }

            IV = (ticket.getFixedVersion().getIndex()-ticket.getOpeningVersion().getIndex())*P;
            ticket.setInjectedVersion(releaseList.get(((int) IV)-1));
        }
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

    private static float coldStartProportion(List<Ticket> list) {
        // TODO devo fare la procedura dell'increment ma estendendo a più progetti e la media sui ticket totali dei vari progetti??
    }

    private static float computeP(Ticket ticket) {
        /* Check if FV=OV then set FV-OV=1 */
        if (ticket.getOpeningVersion().getIndex() == ticket.getFixedVersion().getIndex()) {
            return (float) (ticket.getFixedVersion().getIndex() - ticket.getInjectedVersion().getIndex());
        }

        /* General case */
        return (float) (ticket.getFixedVersion().getIndex() - ticket.getInjectedVersion().getIndex())/(ticket.getFixedVersion().getIndex()-ticket.getOpeningVersion().getIndex());
    }
}
