package org.example.controller;

import org.example.entity.Release;
import org.example.tool.FileCSVGenerator;
import org.example.tool.Json;
import org.example.tool.ReleaseTool;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JiraExtraction {
    public static HashMap<LocalDateTime, String> releaseNames;
    public static HashMap<LocalDateTime, String> releaseID;
    public static ArrayList<LocalDateTime> listOfReleasesDate;  // this list will contain only the release's dates
    private final String projectName;
    public JiraExtraction(String projectName){
        this.projectName = projectName.toUpperCase();
    }

    public List<Release> getReleaseInfo() throws IOException {
        List<Release> releases = new ArrayList<>();
        listOfReleasesDate = new ArrayList<>();

        int i = 0;

        String url = "https://issues.apache.org/jira/rest/api/2/project/"
                + this.projectName;
        JSONObject json = Json.readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("versions");
        releaseNames = new HashMap<>();
        releaseID = new HashMap<>();

        for (; i<versions.length(); i++) {
            String releaseName;
            String releaseDate;
            String releaseID;
            JSONObject jsonObject = versions.getJSONObject(i);

            if(jsonObject.has("releaseDate") && jsonObject.has("name")) {
                releaseDate = jsonObject.get("releaseDate").toString();
                releaseName = jsonObject.get("name").toString();
                releaseID = jsonObject.get("id").toString();

                ReleaseTool.addRelease(releaseDate, releaseName, releaseID);
            }
        }

        listOfReleasesDate.sort(LocalDateTime::compareTo); // order releases by date

        /* Generate CSV file */
        FileCSVGenerator.generateReleaseInfo(projectName);

        /* Build new releases list */
        for (i = 0; i < listOfReleasesDate.size(); i++) {
            Release release = new Release(i+1, releaseNames.get(listOfReleasesDate.get(i)), listOfReleasesDate.get(i));
            releases.add(release);
        }

        return releases;
    }

    /*
    public List<Ticket> fetchTickets(List<Release> releaseList) throws IOException, JSONException {
        int j;
        int i = 0;
        int total;

        List<Ticket> listOfTicket = new ArrayList<>();

        //Get JSON API for closed bugs w/ AV in the project
        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + this.projectName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
                    + i + "&maxResults=" + j;
            JSONObject json = Json.readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");

            for (; i < total && i < j; i++) {
                //Iterate through each bug
                String key = issues.getJSONObject(i%1000).get("key").toString();

            }
        } while (i < total);
    }
    */
}
