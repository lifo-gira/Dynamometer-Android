package com.example.dynamopush;

import java.util.List;

public class ReportClass {
    private String forceCount;
    private String muscleName;
    private List<Float> chartData; // Example placeholder for LineChart data

    public ReportClass(String forceCount, String muscleName, List<Float> chartData) {
        this.forceCount = forceCount;
        this.muscleName = muscleName;
        this.chartData = chartData;
    }

    public String getForceCount() {
        return forceCount;
    }

    public String getMuscleName() {
        return muscleName;
    }

    public List<Float> getChartData() {
        return chartData;
    }
}
