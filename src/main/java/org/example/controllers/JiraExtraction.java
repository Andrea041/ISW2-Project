package org.example.controllers;

import org.example.entities.Release;
import org.example.entities.Ticket;
import org.example.tool.Json;
import org.example.tool.ReleaseTool;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class JiraExtraction {
    private Map<LocalDateTime, String> releaseNames;
    private Map<LocalDateTime, String> releaseID;
    private List<LocalDateTime> listOfReleaseDates;
    private final String projectName;

    public JiraExtraction(String projectName) {
        this.projectName = projectName.toUpperCase();
        this.releaseNames = new HashMap<>();
        this.releaseID = new HashMap<>();
        this.listOfReleaseDates = new ArrayList<>();
    }

    public List<Release> getReleaseInfo() throws IOException {
        List<Release> releases = new ArrayList<>();
        String url = "https://issues.apache.org/jira/rest/api/2/project/" + this.projectName;
        JSONObject json = Json.readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("versions");

        for (int i = 0; i < versions.length(); i++) {
            JSONObject jsonObject = versions.getJSONObject(i);
            if (jsonObject.has("releaseDate") && jsonObject.has("name")) {
                String releaseDate = jsonObject.getString("releaseDate");
                String releaseName = jsonObject.getString("name");
                String releaseID = jsonObject.getString("id");
                addRelease(releaseDate, releaseName, releaseID);
            }
        }

        Collections.sort(listOfReleaseDates);

        for (int i = 0; i < listOfReleaseDates.size(); i++) {
            Release release = new Release(i + 1, releaseNames.get(listOfReleaseDates.get(i)), listOfReleaseDates.get(i), releaseID.get(listOfReleaseDates.get(i)));
            releases.add(release);
        }

        return releases;
    }

    public List<Ticket> fetchTickets(List<Release> releasesList) throws IOException {
        List<Ticket> listOfTickets = new ArrayList<>();
        int startAt = 0;
        int maxResults = 1000;
        int total;

        do {
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + projectName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
                    + startAt + "&maxResults=" + maxResults;
            JSONObject json = Json.readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");

            for (int i = 0; i < Math.min(total - startAt, maxResults); i++) {
                JSONObject issue = issues.getJSONObject(i);
                String key = issue.getString("key");
                JSONObject fields = issue.getJSONObject("fields");

                LocalDateTime creationDate = LocalDateTime.parse(fields.getString("created").substring(0, 16));
                LocalDateTime resolutionDate = LocalDateTime.parse(fields.getString("resolutiondate").substring(0, 16));
                List<Release> affectedVersions = getAffectedVersions(fields.getJSONArray("versions"), releasesList);

                Ticket ticket = new Ticket(creationDate, resolutionDate, key);
                ticket.setAffectedVersionsList(affectedVersions);
                ticket.setOpeningVersion(ReleaseTool.fetchVersion(creationDate, releasesList));
                ticket.setFixedVersion(ReleaseTool.fetchVersion(resolutionDate, releasesList));

                listOfTickets.add(ticket);
            }

            startAt += maxResults;
        } while (startAt < total);

        return listOfTickets;
    }

    private List<Release> getAffectedVersions(JSONArray affectedVersions, List<Release> releasesList) {
        List<Release> avReleaseList = new ArrayList<>();
        if (affectedVersions == null || affectedVersions.isEmpty())
            return avReleaseList;

        for (int k = 0; k < affectedVersions.length(); k++) {
            String av = affectedVersions.getJSONObject(k).getString("name");
            for (Release release : releasesList) {
                if (av.equals(release.getName()))
                    avReleaseList.add(release);
            }
        }
        return avReleaseList;
    }

    private void addRelease(String strDate, String name, String id) {
        LocalDate date = LocalDate.parse(strDate);
        LocalDateTime dateTime = date.atStartOfDay();
        if (!listOfReleaseDates.contains(dateTime))
            listOfReleaseDates.add(dateTime);
        releaseNames.put(dateTime, name);
        releaseID.put(dateTime, id);
    }
}

