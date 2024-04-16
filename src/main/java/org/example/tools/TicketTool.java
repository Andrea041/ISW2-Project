package org.example.tools;

import org.example.entities.Release;
import org.example.entities.Ticket;

import java.util.List;

public class TicketTool {
    private TicketTool() {}

    public static void fixInconsistentTickets(List<Ticket> ticketListOG, List<Release> releaseList) {
        for (Ticket ticket : ticketListOG) {
            /* Check if affected version's list is not empty */
            if (!ticket.getAffectedVersionsList().isEmpty()) {
                checkTicket(ticket);
            }
        }

        ticketListOG.removeIf(ticket -> !ticket.getOpeningVersion().getDate().isAfter(releaseList.get(0).getDate()));
    }

    private static void checkTicket(Ticket ticket) {
        /* Testing validity of release in affected version's list */
        if (ticket.getAffectedVersionsList().getFirst().getDate().isBefore(ticket.getResolutionDate()) &&
                !ticket.getAffectedVersionsList().getFirst().getDate().isAfter(ticket.getCreationDate())) {
            /* Setting injected version as the first one in affected version's list */
            ticket.setInjectedVersion(ticket.getAffectedVersionsList().get(0));
        }
    }
}
