package org.example.tool;

import org.eclipse.jgit.revwalk.RevCommit;
import org.example.entities.Ticket;

import java.util.ArrayList;
import java.util.List;

public class CommitTool {
    public static List<RevCommit> filterCommit(List<RevCommit> commitList, List<Ticket> ticketList) {
        List<RevCommit> filteredCommitList = new ArrayList<>();

        for (RevCommit commit : commitList) {
            for (Ticket ticket : ticketList) {
                if (ticket.getCommitList().contains(commit) && !filteredCommitList.contains(commit)) {
                    filteredCommitList.add(commit);
                }
            }
        }

        return filteredCommitList;
    }
}
