package org.example.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Release {
    private int index;
    private final String name;
    private final LocalDateTime date;
    private final List<Commit> listOfCommit; // must be initialized in constructor

    public Release(int id, String name, LocalDateTime date) {
        this.index = id;
        this.name = name;
        this.date = date;
        this.listOfCommit = new ArrayList<>();
    }

    public Release(String name, LocalDateTime date) {
        this.name = name;
        this.date = date;
        this.listOfCommit = new ArrayList<>();
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

    public List<Commit> getListOfCommit() {
        return listOfCommit;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
