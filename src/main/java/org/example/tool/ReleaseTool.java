package org.example.tool;

import org.example.controllers.JiraExtraction;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReleaseTool {
    public static void addRelease(String strDate, String name, String id) {
        LocalDate date = LocalDate.parse(strDate);
        LocalDateTime dateTime = date.atStartOfDay();   // Date format: 2011-12-07T00:00

        if (!JiraExtraction.listOfReleasesDate.contains(dateTime))
            JiraExtraction.listOfReleasesDate.add(dateTime);    // Date added to date list

        JiraExtraction.releaseNames.put(dateTime, name);    // key-value association: name to date
        JiraExtraction.releaseID.put(dateTime, id); //// key-value association: id to date
    }
}
