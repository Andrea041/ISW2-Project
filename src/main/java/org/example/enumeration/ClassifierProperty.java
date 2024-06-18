package org.example.enumeration;

public enum ClassifierProperty {
    COST_SENSITIVE("sensitive learning"),
    SAMPLING_TYPE("SMOTE"),
    SELECTION("feature selection");

    private final String value;

    ClassifierProperty(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
