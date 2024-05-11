package org.example.tool;

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

    private static void writeToFile(FileWriter fileWriter, String content) throws IOException {
        fileWriter.append(content);
        fileWriter.append("\n");
    }

    private static void closeWriter(FileWriter fileWriter) {
        if (fileWriter != null) {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
            }
        }
    }

    public static void generateReleaseInfo(String projName, List<Release> releases) {
        FileWriter fileWriter = null;

        try {
            String fileTitle = projName + "_releaseList.csv";
            fileWriter = new FileWriter(fileTitle);

            writeToFile(fileWriter, "Index,Version ID,Version Name,Date");

            for (int i = 0; i < releases.size(); i++) {
                Release release = releases.get(i);
                int index = i + 1;

                writeToFile(fileWriter, index + "," + release.getVersionID() + "," +
                        release.getName() + "," + release.getDate().toString());
            }

        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "An error occurred while generating release info", e);
        } finally {
            closeWriter(fileWriter);
        }
    }

    public static void generateTicketInfo(String projName, List<Ticket> ticketList) {
        FileWriter fileWriter = null;

        try {
            String fileTitle = projName + "_ticketList.csv";
            fileWriter = new FileWriter(fileTitle);

            writeToFile(fileWriter, "Key,Injected Version,Opening Version,Fixed Version, Affected Version List");

            for (Ticket ticket : ticketList) {
                String affectedVersions = getAffectedVersionsAsString(ticket);
                writeToFile(fileWriter, ticket.getTicketKey() + "," + ticket.getInjectedVersion().getName() + "," +
                        ticket.getOpeningVersion().getName() + "," + ticket.getFixedVersion().getName() + "," +
                        affectedVersions);
            }

        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        } finally {
            closeWriter(fileWriter);
        }
    }

    private static String getAffectedVersionsAsString(Ticket ticket) {
        List<Release> affectedVersions = ticket.getAffectedVersionsList();
        StringBuilder stringBuilder = new StringBuilder();
        for (Release release : affectedVersions) {
            stringBuilder.append(release.getName()).append(" ");
        }
        return stringBuilder.toString();
    }

    public static void generateTrainingSet(String projName, List<Release> releaseList) {
        FileWriter fileWriter = null;

        try {
            String fileTitle = projName + "_trainingSet.csv";
            fileWriter = new FileWriter(fileTitle);

            writeToFile(fileWriter, "Version ID,Class Name,Version,LOC,LOC_touched,Age,Revision number,Fix number,Author Number,LOC_added,MAX_LOC_added,AVG_LOC_added,churn,MAX_churn,Buggyness");

            for (int i = 0; i < releaseList.size(); i++) {
                Release release = releaseList.get(i);
                int index = i + 1;

                for (JavaClass javaClass : release.getJavaClassList()) {
                    writeToFile(fileWriter, index + "," + javaClass.getName() + "," +
                            javaClass.getLocSize() + "," + javaClass.getLocTouched() + "," +
                            javaClass.getAge().getYears() + "," + javaClass.getRevisionNumber() + "," +
                            javaClass.getFixNumber() + "," + javaClass.getAuthorNumber() + "," +
                            javaClass.getLocAdded() + "," + javaClass.getMaxLOCAdded() + "," +
                            javaClass.getAvgLOCAdded() + "," + javaClass.getChurn() + "," +
                            javaClass.getMaxChurn() + "," + javaClass.getBuggy());
                }
            }

        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        } finally {
            closeWriter(fileWriter);
        }
    }
}
