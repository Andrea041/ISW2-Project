package org.example.tool;

import org.eclipse.jgit.revwalk.RevCommit;
import org.example.entities.Release;
import org.example.entities.Ticket;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public static void linkTicketsToCommits(List<Ticket> ticketList, List<RevCommit> commitList) {
        String msg;

        for (Iterator<Ticket> iterator = ticketList.iterator(); iterator.hasNext();) {
            Ticket ticket = iterator.next();
            for (RevCommit commit : commitList) {
                if (checkCommit(commit.getFullMessage(), ticket.getTicketKey())) {
                    ticket.getCommitList().add(commit);
                    msg = "Ticket " + ticket.getTicketKey() + " has been linked";
                    Logger.getAnonymousLogger().log(Level.INFO, msg);
                }
            }

            /* Remove ticket without any associated commit cause are usefully */
            if (ticket.getCommitList().isEmpty())
                iterator.remove();
        }
    }

    private static boolean checkCommit(String commitMessage, String ticketKey) {
        return commitMessage.contains(ticketKey + "\n")
                || commitMessage.contains(ticketKey + ":")
                || commitMessage.contains(ticketKey + " ")
                || commitMessage.contains(ticketKey + "_")
                || commitMessage.contains(ticketKey + "]")
                || commitMessage.contains(ticketKey + ".")
                || commitMessage.contains(ticketKey + "/")
                || commitMessage.endsWith(ticketKey);
    }
}
