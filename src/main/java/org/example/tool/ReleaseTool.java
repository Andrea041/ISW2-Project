package org.example.tool;

import org.example.entities.Release;

import java.time.LocalDateTime;
import java.util.List;

public class ReleaseTool {

    public static Release fetchVersion(LocalDateTime dateTime, List<Release> releaseList) {
        for (Release release : releaseList) {
            if (!release.getDate().isBefore(dateTime)) {
                return release;
            }
        }

        return null;
    }

    public static Release fetchCommitRelease(LocalDateTime commitDate, List<Release> releaseList) {
        for (int i = 0; i < releaseList.size(); i++) {
            Release release = releaseList.get(i);

            if (commitDate.isBefore(release.getDate())) {
                return release;
            } else if (commitDate.isAfter(releaseList.getLast().getDate())) {
                return releaseList.getLast();
            }
        }

        return null;
    }
}
