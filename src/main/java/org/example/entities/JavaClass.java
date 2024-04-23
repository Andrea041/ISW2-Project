package org.example.entities;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

public class JavaClass {
    private String name;
    private String content;
    private List<RevCommit> commitList;
    private boolean buggy;
    private Release release;

    /* metrics */
    private int LOCSize;
    private int LOCTouched;
    private int revisionNumber;
    private int authorNumber;
    private int fixNumber;
    private int LOCAdded;
    private int maxLOCAdded;
    private double avgLOCAdded;
    private int churn;
    private int maxChurn;

    public JavaClass(String name, String content, Release release) {
        this.name = name;
        this.content = content;
        this.commitList = new ArrayList<>();
        this.release = release;
    }

    public List<RevCommit> getCommitList() {
        return commitList;
    }

    public boolean getBuggy() {
        return buggy;
    }

    public double getAvgLOCAdded() {
        return avgLOCAdded;
    }

    public int getAuthorNumber() {
        return authorNumber;
    }

    public int getChurn() {
        return churn;
    }

    public int getFixNumber() {
        return fixNumber;
    }

    public int getLOCAdded() {
        return LOCAdded;
    }

    public int getLOCSize() {
        return LOCSize;
    }

    public int getLOCTouched() {
        return LOCTouched;
    }

    public int getMaxChurn() {
        return maxChurn;
    }

    public int getMaxLOCAdded() {
        return maxLOCAdded;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public Release getRelease() {
        return release;
    }

    public String getContent() {
        return content;
    }

    public String getName() {
        return name;
    }

    public void setAuthorNumber(int authorNumber) {
        this.authorNumber = authorNumber;
    }

    public void setAvgLOCAdded(double avgLOCAdded) {
        this.avgLOCAdded = avgLOCAdded;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }

    public void setChurn(int churn) {
        this.churn = churn;
    }

    public void setCommitList(List<RevCommit> commitList) {
        this.commitList = commitList;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFixNumber(int fixNumber) {
        this.fixNumber = fixNumber;
    }

    public void setLOCAdded(int LOCAdded) {
        this.LOCAdded = LOCAdded;
    }

    public void setLOCSize(int LOCSize) {
        this.LOCSize = LOCSize;
    }

    public void setLOCTouched(int LOCTouched) {
        this.LOCTouched = LOCTouched;
    }

    public void setMaxChurn(int maxChurn) {
        this.maxChurn = maxChurn;
    }

    public void setMaxLOCAdded(int maxLOCAdded) {
        this.maxLOCAdded = maxLOCAdded;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRelease(Release release) {
        this.release = release;
    }

    public void setRevisionNumber(int revisionNumber) {
        this.revisionNumber = revisionNumber;
    }
}
