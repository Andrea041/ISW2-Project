package org.example.tool;

import org.example.controllers.JiraExtraction;
import org.example.entities.JavaClass;
import org.example.entities.Release;
import org.example.entities.Ticket;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileCSVGenerator {
    private FileCSVGenerator() {}

    public static void generateReleaseInfo(String projName, List<Release> releases) {
        FileWriter fileWriter = null;
        int numVersions;
        int index;

        try {
            String fileTitle = projName + "_releaseList.csv";

            //Name of CSV for output
            fileWriter = new FileWriter(fileTitle);
            fileWriter.append("Index,Version ID,Version Name,Date");
            fileWriter.append("\n");

            numVersions = releases.size();
            for (int i = 0; i < numVersions; i++) {
                index = i + 1;
                fileWriter.append(Integer.toString(index));
                fileWriter.append(",");
                fileWriter.append(JiraExtraction.releaseID.get(releases.get(i).getDate()));
                fileWriter.append(",");
                fileWriter.append(JiraExtraction.releaseNames.get(releases.get(i).getDate()));
                fileWriter.append(",");
                fileWriter.append(releases.get(i).toString());
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

    public static void generateTrainingSet(String projName, List<Release> releaseList) {
        FileWriter fileWriter = null;
        int index;

        try {
            String fileTitle = projName + "_trainingSet.csv";

            //Name of CSV for output
            fileWriter = new FileWriter(fileTitle);
            fileWriter.append("Version ID,Class Name,Version,LOC,LOC_touched,Age,Revision number,Fix number,Author Number,LOC_added,MAX_LOC_added,AVG_LOC_added,churn,MAX_churn,Buggyness");
            fileWriter.append("\n");

            int i = 0;

            for(Release release : releaseList) {
               for (JavaClass javaClass : release.getJavaClassList()) {
                   index = i + 1;
                   fileWriter.append(Integer.toString(index));
                   fileWriter.append(",");
                   fileWriter.append(javaClass.getName());
                   fileWriter.append(",");
                   fileWriter.append(release.getName());
                   fileWriter.append(",");
                   fileWriter.append(Integer.toString(javaClass.getLocSize()));
                   fileWriter.append(",");
                   fileWriter.append(Integer.toString(javaClass.getLocTouched()));
                   fileWriter.append(",");
                   fileWriter.append(Integer.toString(javaClass.getAge().getYears()));
                   fileWriter.append(",");
                   fileWriter.append(Integer.toString(javaClass.getRevisionNumber()));
                   fileWriter.append(",");
                   fileWriter.append(Integer.toString(javaClass.getFixNumber()));
                   fileWriter.append(",");
                   fileWriter.append(Integer.toString(javaClass.getAuthorNumber()));
                   fileWriter.append(",");
                   fileWriter.append(Integer.toString(javaClass.getLocAdded()));
                   fileWriter.append(",");
                   fileWriter.append(Integer.toString(javaClass.getMaxLOCAdded()));
                   fileWriter.append(",");
                   fileWriter.append(Double.toString(javaClass.getAvgLOCAdded()));
                   fileWriter.append(",");
                   fileWriter.append(Integer.toString(javaClass.getChurn()));
                   fileWriter.append(",");
                   fileWriter.append(Integer.toString(javaClass.getMaxChurn()));
                   fileWriter.append(",");
                   fileWriter.append(javaClass.getBuggy());
                   fileWriter.append("\n");
               }
               i++;
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