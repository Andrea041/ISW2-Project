package org.example.tool;

import org.example.controllers.JiraExtraction;
import org.example.entities.Release;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReleaseTool {
    public static void addRelease(String strDate, String name, String id) {
        LocalDate date = LocalDate.parse(strDate);
        LocalDateTime dateTime = date.atStartOfDay();   // Date format: 2011-12-07T00:00

        if (!JiraExtraction.listOfReleasesDate.contains(dateTime))
            JiraExtraction.listOfReleasesDate.add(dateTime);    // Date added to date list

        JiraExtraction.releaseNames.put(dateTime, name);    // key-value association: name to date
        JiraExtraction.releaseID.put(dateTime, id); //// key-value association: id to date
    }

    public static Release fetchVersion(LocalDateTime dateTime, List<Release> releaseList) {
        for (Release release : releaseList) {
            if (!release.getDate().isBefore(dateTime)) {
                return release;
            }
        }
        return null;
    }

    public static Release fetchCommitRelease(LocalDateTime commitDate, List<Release> releaseList) {
        for (int i = 0; i < releaseList.size() - 1; i++) {
            Release release = releaseList.get(i);
            Release nextRelease = releaseList.get(i + 1);

            if (nextRelease != null && commitDate.isAfter(release.getDate()) && commitDate.isBefore(nextRelease.getDate())){
                return release;
            } else if (nextRelease == null) {
                return releaseList.getLast();
            }
        }

        if (commitDate.isAfter(releaseList.getLast().getDate())) {
            return releaseList.getLast();
        }

        return null;
    }
}
