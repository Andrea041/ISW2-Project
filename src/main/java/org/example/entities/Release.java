package org.example.entities;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Release {
    private int index;
    private final String name;
    private final LocalDateTime date;
    private final List<RevCommit> commitList;
    private List<JavaClass> javaClassList;
    private final String versionID;

    public Release(int id, String name, LocalDateTime date, String versionID) {
        this.index = id;
        this.name = name;
        this.date = date;
        this.versionID = versionID;

        commitList = new ArrayList<>();
        javaClassList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public List<RevCommit> getCommitList() {
        return commitList;
    }

    public void setJavaClassList(List<JavaClass> javaClassList) {
        this.javaClassList = javaClassList;
    }

    public List<JavaClass> getJavaClassList() {
        return javaClassList;
    }

    public String getVersionID() {
        return versionID;
    }
}
