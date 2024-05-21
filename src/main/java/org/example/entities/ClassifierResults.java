package org.example.entities;

public class ClassifierResults {
    private final String projName;
    private final int index;
    private final String classifierName;

    private final double percTrainingInstances;
    private double precision;
    private double recall;
    private double kappa;
    private double truePositives;
    private double falsePositives;
    private double falseNegatives;
    private double trueNegatives;
    private double AUC;
    private double fMeasure;

    private final boolean costSensitive;
    private final boolean sampling;
    private final boolean selection;

    public ClassifierResults(String projName, int index, String classifierName, boolean costSensitive, boolean sampling, boolean selection, int trainInstances, int testInstances) {
        this.projName = projName;
        this.index = index;
        this.classifierName = classifierName;
        this.costSensitive = costSensitive;
        this.sampling = sampling;
        this.selection = selection;

        this.percTrainingInstances = 100.0 * trainInstances /(trainInstances + testInstances);
    }

    public void setAUC(double AUC) {
        this.AUC = AUC;
    }


    public void setFMeasure(double fMeasure) {
        this.fMeasure = fMeasure;
    }

    public void setKappa(double kappa) {
        this.kappa = kappa;
    }

    public void setPreci(double precision) {
        this.precision = precision;
    }

    public void setRec(double recall) {
        this.recall = recall;
    }

    public void setTruePositives(double truePositives) {
        this.truePositives = truePositives;
    }

    public void setFalsePositives(double falsePositives) {
        this.falsePositives = falsePositives;
    }

    public void setFalseNegatives(double falseNegatives) {
        this.falseNegatives = falseNegatives;
    }

    public void setTrueNegatives(double trueNegatives) {
        this.trueNegatives = trueNegatives;
    }

    public String isCostSensitive() {
        if (!costSensitive)
            return "No";
        return "Yes";
    }

    public String isSampling() {
        if (!sampling)
            return "No";
        return "Yes";
    }

    public String isSelection() {
        if (!selection)
            return "No";
        return "Yes";
    }

    public double getPreci() {
        return precision;
    }

    public double getRec() {
        return recall;
    }

    public double getTruePositives() {
        return truePositives;
    }

    public double getFalsePositives() {
        return falsePositives;
    }

    public double getFalseNegatives() {
        return falseNegatives;
    }

    public double getTrueNegatives() {
        return trueNegatives;
    }

    public String getProjName() {
        return projName;
    }

    public int getIndex() {
        return index;
    }

    public String getClassifierName() {
        return classifierName;
    }

    public double getAUC() {
        return AUC;
    }

    public double getFMeasure() {
        return fMeasure;
    }

    public double getKappa() {
        return kappa;
    }

    public double getPercTrainingInstances() {
        return percTrainingInstances;
    }
}
