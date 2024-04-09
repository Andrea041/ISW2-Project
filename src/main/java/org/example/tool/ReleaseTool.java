package org.example.tool;

import org.example.controller.JiraExtraction;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReleaseTool {
    public static void addRelease(String strDate, String name, String id) {
        LocalDate date = LocalDate.parse(strDate);
        LocalDateTime dateTime = date.atStartOfDay();

        if (!JiraExtraction.listOfReleasesDate.contains(dateTime))
            JiraExtraction.listOfReleasesDate.add(dateTime);

        JiraExtraction.releaseNames.put(dateTime, name);
        JiraExtraction.releaseID.put(dateTime, id);
    }
}
