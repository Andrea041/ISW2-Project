package org.example.controllers;

import org.eclipse.jgit.lib.Repository;
import org.example.tool.RepoFactory;

public class Buggyness {
    private final Repository repository;

    public Buggyness() {
        this.repository = RepoFactory.getRepo();
    }


}
