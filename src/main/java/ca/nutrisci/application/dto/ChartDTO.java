package ca.nutrisci.application.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data Transfer Object for chart visualization data.
 * Contains all necessary information to render a chart.
 */
public class ChartDTO {
    public enum ChartType {
        PIE,
        BAR,
        LINE,
        GROUPED_BAR
    }

    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    private ChartType chartType;
    private Map<String, Double> dataPoints;
    private boolean hasData;
    private String message;

    public ChartDTO(String title, String xAxisLabel, String yAxisLabel, ChartType chartType) {
        this.title = title;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.chartType = chartType;
        this.dataPoints = new LinkedHashMap<>(); // maintains insertion order
        this.hasData = true;
    }

    public void addDataPoint(String key, Double value) {
        dataPoints.put(key, value);
    }

    public String getTitle() {
        return title;
    }

    public String getXAxisLabel() {
        return xAxisLabel;
    }

    public String getYAxisLabel() {
        return yAxisLabel;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public Map<String, Double> getDataPoints() {
        return new LinkedHashMap<>(dataPoints); // return a copy
    }

    public boolean hasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static ChartDTO createEmptyChart(String title, ChartType chartType) {
        ChartDTO dto = new ChartDTO(title, "", "", chartType);
        dto.setHasData(false);
        return dto;
    }
} 