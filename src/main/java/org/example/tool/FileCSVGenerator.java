package org.example.tool;

import org.example.controllers.JiraExtraction;
import org.example.entities.Ticket;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileCSVGenerator {
    private FileCSVGenerator() {}

    public static void generateReleaseInfo(String projName) {
        FileWriter fileWriter = null;
        int numVersions;
        int index;

        try {
            String fileTitle = projName + "_releaseList.csv";

            //Name of CSV for output
            fileWriter = new FileWriter(fileTitle);
            fileWriter.append("Index,Version ID,Version Name,Date");
            fileWriter.append("\n");

            numVersions = JiraExtraction.listOfReleasesDate.size();
            for (int i = 0; i < numVersions; i++) {
                index = i + 1;
                fileWriter.append(Integer.toString(index));
                fileWriter.append(",");
                fileWriter.append(JiraExtraction.releaseID.get(JiraExtraction.listOfReleasesDate.get(i)));
                fileWriter.append(",");
                fileWriter.append(JiraExtraction.releaseNames.get(JiraExtraction.listOfReleasesDate.get(i)));
                fileWriter.append(",");
                fileWriter.append(JiraExtraction.listOfReleasesDate.get(i).toString());
                fileWriter.append("\n");
            }

        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        } finally {
            try {
                assert fileWriter != null;
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
            }
        }
    }

    public static void generateTicketInfo(String projName, List<Ticket> ticketList) {
        FileWriter fileWriter = null;
        int numTickets;

        try {
            String fileTitle = projName + "_ticketList.csv";

            //Name of CSV for output
            fileWriter = new FileWriter(fileTitle);
            fileWriter.append("Key,Injected Version,Opening Version,Fixed Version, Affected Version List");
            fileWriter.append("\n");

            numTickets = ticketList.size();
            for (int i = 0; i < numTickets; i++) {
                fileWriter.append(ticketList.get(i).getTicketKey());
                fileWriter.append(",");
                fileWriter.append(ticketList.get(i).getInjectedVersion().getName());
                fileWriter.append(",");
                fileWriter.append(ticketList.get(i).getOpeningVersion().getName());
                fileWriter.append(",");
                fileWriter.append(ticketList.get(i).getFixedVersion().getName());
                fileWriter.append(",");
                fileWriter.append("{");
                for (int j = 0; j < ticketList.get(i).getAffectedVersionsList().size(); j++) {
                    fileWriter.append(ticketList.get(i).getAffectedVersionsList().get(j).getName());
                    if (j != ticketList.get(i).getAffectedVersionsList().size() - 1) {fileWriter.append(" / ");}
                }
                fileWriter.append("}");
                fileWriter.append("\n");
            }

        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        } finally {
            try {
                assert fileWriter != null;
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
            }
        }
    }
}
