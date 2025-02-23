package com.example.dynamopush;

import org.json.JSONObject;

public class ReportListClass {
    private String testCount;
    private String repCount;
    private int musclesCovered;
    private JSONObject record;  // Add this field to hold the entire record data

    // Constructor
    public ReportListClass(String testCount, String repCount, int musclesCovered, JSONObject record) {
        this.testCount = testCount;
        this.repCount = repCount;
        this.musclesCovered = musclesCovered;
        this.record = record;  // Save the entire record
    }

    // Getters
    public String getTestCount() {
        return testCount;
    }

    public String getRepCount() {
        return repCount;
    }

    public int getMusclesCovered() {
        return musclesCovered;
    }

    public JSONObject getRecord() {  // Add a getter for the record
        return record;
    }
}
