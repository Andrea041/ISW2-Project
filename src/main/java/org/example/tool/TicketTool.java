package org.example.tool;

import org.example.entities.Release;
import org.example.entities.Ticket;

import java.util.ArrayList;
import java.util.List;

public class TicketTool {
    private TicketTool() {}

    public static void fixInconsistentTickets(List<Ticket> ticketListOG, List<Release> releaseList) {
        List<Ticket> ticketListOT = new ArrayList<>();

        for (Ticket ticket : ticketListOG) {
            /* Check if affected version's list is not empty */
            if (!ticket.getAffectedVersionsList().isEmpty() && ticket.getOpeningVersion() != null && ticket.getFixedVersion() != null) {
                checkTicket(ticket);
                if (ticket.getAffectedVersionsList().getFirst().getDate().isAfter(ticket.getCreationDate()))
                    ticketListOT.add(ticket);
            }
        }

        for (Ticket ticket : ticketListOT) {
            ticketListOG.remove(ticket);
        }

        ticketListOG.removeIf(ticket -> ticket.getOpeningVersion() == null
                || ticket.getFixedVersion() == null
                || !ticket.getOpeningVersion().getDate().isAfter(releaseList.getFirst().getDate())
                || ticket.getOpeningVersion().getDate().isAfter(ticket.getFixedVersion().getDate())
                || ticket.getOpeningVersion().getIndex() == releaseList.getFirst().getIndex());
    }

    private static void checkTicket(Ticket ticket) {
        /* Testing validity of release in affected version's list */
        if (ticket.getAffectedVersionsList().getFirst().getDate().isBefore(ticket.getResolutionDate())
                && !ticket.getAffectedVersionsList().getFirst().getDate().isAfter(ticket.getCreationDate())
                && ticket.getAffectedVersionsList().getFirst().getIndex() != ticket.getOpeningVersion().getIndex()) {
            /* Setting injected version as the first one in affected version's list */
            ticket.setInjectedVersion(ticket.getAffectedVersionsList().getFirst());
        }
    }
}
