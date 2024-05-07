package org.example.controllers;

import org.example.entities.Release;
import org.example.entities.Ticket;
import org.example.tool.Json;
import org.example.tool.ReleaseTool;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class JiraExtraction {
    public static final Map<LocalDateTime, String> releaseNames = Map.of();
    public static final Map<LocalDateTime, String> releaseID = Map.of();
    private final List<LocalDateTime> listOfReleasesDate = new ArrayList<>();  // this list will contain only the release's dates
    private final String projectName;

    public JiraExtraction(String projectName){
        this.projectName = projectName.toUpperCase();
    }

    public List<Release> getReleaseInfo() throws IOException {
        List<Release> releases = new ArrayList<>();

        int i = 0;

        String url = "https://issues.apache.org/jira/rest/api/2/project/"
                + this.projectName;
        JSONObject json = Json.readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("versions");

        for (; i<versions.length(); i++) {
            String releaseName;
            String releaseDate;
            String releaseID;
            JSONObject jsonObject = versions.getJSONObject(i);

            if(jsonObject.has("releaseDate") && jsonObject.has("name")) {
                releaseDate = jsonObject.get("releaseDate").toString();
                releaseName = jsonObject.get("name").toString();
                releaseID = jsonObject.get("id").toString();

                ReleaseTool.addRelease(releaseDate, releaseName, releaseID, listOfReleasesDate);
            }
        }

        listOfReleasesDate.sort(LocalDateTime::compareTo); // order releases by date

        /* Build new releases list */
        for (i = 0; i < listOfReleasesDate.size(); i++) {
            Release release = new Release(i+1, releaseNames.get(listOfReleasesDate.get(i)), listOfReleasesDate.get(i));
            releases.add(release);
        }

        return releases;
    }


    public List<Ticket> fetchTickets(List<Release> releasesList, String projectName) throws IOException {
        int j;
        int i = 0;
        int total;

        List<Ticket> listOfTicket = new ArrayList<>();

        //Get JSON API for closed bugs w/ AV in the project
        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + projectName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
                    + i + "&maxResults=" + j;
            JSONObject json = Json.readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");

            for (; i < total && i < j; i++) {
                //Iterate through each bug
                String key = issues.getJSONObject(i % 1000).get("key").toString();    // Print of key: "BOOKKEEPER-1105"
                JSONObject jsonIssues = issues.getJSONObject(i % 1000).getJSONObject("fields");

                LocalDateTime creationDate = LocalDateTime.parse(jsonIssues.getString("created").substring(0, 16));
                LocalDateTime resolutionDate = LocalDateTime.parse(jsonIssues.getString("resolutiondate").substring(0, 16));

                JSONArray affectedVersion = jsonIssues.getJSONArray("versions");
                List<Integer> listAV = new ArrayList<>();   // affected version list that contain index
                List<Release> avReleaseList = new ArrayList<>();

                if (affectedVersion.isEmpty())
                    listAV.add(null); // no affected version
                else {
                    /* Iterating through each version in fields */
                    for (int k = 0; k < affectedVersion.length(); k++) {
                        String av = affectedVersion.getJSONObject(k).getString("name");    // Like: "4.5.0"

                        /* Adding index of AV release to affected version list */
                        for (Release release : releasesList) {
                            if (av.equals(release.getName()))
                                listAV.add(release.getIndex());
                        }
                    }
                }

                Ticket ticket = new Ticket(creationDate, resolutionDate, key);

                /* Update affected version list with Release */
                for (Integer index : listAV) {
                    if (index != null)
                        avReleaseList.add(releasesList.get(index - 1));

                    // else == empty affected version's list
                }


                ticket.setAffectedVersionsList(avReleaseList);  // setting the related affected version list to the ticket
                ticket.setOpeningVersion(ReleaseTool.fetchVersion(creationDate, releasesList));
                ticket.setFixedVersion(ReleaseTool.fetchVersion(resolutionDate, releasesList));

                listOfTicket.add(ticket);
            }
        } while (i < total);
        return listOfTicket;
    }
}
