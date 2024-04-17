package org.example.tool;

import org.example.controllers.JiraExtraction;

import java.io.FileWriter;
import java.io.IOException;
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
}
