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

    public Release(int id, String name, LocalDateTime date) {
        this.index = id;
        this.name = name;
        this.date = date;

        commitList = new ArrayList<>();
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
}
