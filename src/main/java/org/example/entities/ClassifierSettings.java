package org.example.entities;

public class ClassifierSettings {
    private String costSensitive;
    private String sampling;
    private String featureSelection;

    public ClassifierSettings() {
        this.costSensitive = "";
        this.sampling = "";
        this.featureSelection = "";
    }

    public void setCostSensitive(String costSensitive) {
        this.costSensitive = costSensitive;
    }

    public void setFeatureSelection(String featureSelection) {
        this.featureSelection = featureSelection;
    }

    public void setSampling(String sampling) {
        this.sampling = sampling;
    }

    public String getSampling() {
        return sampling;
    }

    public String getCostSensitive() {
        return costSensitive;
    }

    public String getFeatureSelection() {
        return featureSelection;
    }

    public void reset() {
        costSensitive = "";
        sampling = "";
        featureSelection = "";
    }
}
