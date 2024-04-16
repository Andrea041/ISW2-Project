package org.example.tool;

import org.example.entity.Release;
import org.example.entity.Ticket;

import java.util.List;

public class TicketTool {
    private TicketTool() {}

    public static void fixInconsistentTickets(List<Ticket> ticketListOG) {
        for (Ticket ticket : ticketListOG) {
            /* Check if affected version's list is not empty */
            if (!ticket.getAffectedVersionsList().isEmpty()) {
                checkTicket(ticket);
            }
        }
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
