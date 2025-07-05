package ca.nutrisci.application.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ChartDTO - Data Transfer Object for chart visualization data
 * Part of the Application Layer
 */
public class ChartDTO {
    
    private String chartType; // "bar", "pie", "line", "scatter"
    private String title;
    private List<DataPoint> dataPoints;
    private List<String> labels;
    private Map<String, Object> metadata;
    
    // Default constructor
    public ChartDTO() {
        this.dataPoints = new ArrayList<>();
        this.labels = new ArrayList<>();
        this.metadata = new HashMap<>();
    }
    
    // Constructor with basic info
    public ChartDTO(String chartType, String title) {
        this();
        this.chartType = chartType;
        this.title = title;
    }
    
    // Full constructor
    public ChartDTO(String chartType, String title, List<DataPoint> dataPoints, 
                   List<String> labels, Map<String, Object> metadata) {
        this.chartType = chartType;
        this.title = title;
        this.dataPoints = new ArrayList<>(dataPoints);
        this.labels = new ArrayList<>(labels);
        this.metadata = new HashMap<>(metadata);
    }
    
    // Getters and Setters
    public String getChartType() { return chartType; }
    public void setChartType(String chartType) { this.chartType = chartType; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public List<DataPoint> getDataPoints() { return new ArrayList<>(dataPoints); }
    public void setDataPoints(List<DataPoint> dataPoints) { 
        this.dataPoints = new ArrayList<>(dataPoints); 
    }
    
    public List<String> getLabels() { return new ArrayList<>(labels); }
    public void setLabels(List<String> labels) { 
        this.labels = new ArrayList<>(labels); 
    }
    
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public void setMetadata(Map<String, Object> metadata) { 
        this.metadata = new HashMap<>(metadata); 
    }
    
    // Data management methods
    public void addDataPoint(String label, double value) {
        dataPoints.add(new DataPoint(label, value));
        if (!labels.contains(label)) {
            labels.add(label);
        }
    }
    
    public void addDataPoint(String label, double value, String category) {
        dataPoints.add(new DataPoint(label, value, category));
        if (!labels.contains(label)) {
            labels.add(label);
        }
    }
    
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    // Chart configuration methods
    public void setXAxisLabel(String label) {
        metadata.put("xAxisLabel", label);
    }
    
    public String getXAxisLabel() {
        return (String) metadata.getOrDefault("xAxisLabel", "");
    }
    
    public void setYAxisLabel(String label) {
        metadata.put("yAxisLabel", label);
    }
    
    public String getYAxisLabel() {
        return (String) metadata.getOrDefault("yAxisLabel", "");
    }
    
    public void setShowLegend(boolean showLegend) {
        metadata.put("showLegend", showLegend);
    }
    
    public boolean isShowLegend() {
        return (Boolean) metadata.getOrDefault("showLegend", true);
    }
    
    // Validation and utility methods
    public boolean isValid() {
        return chartType != null && !chartType.trim().isEmpty() &&
               title != null && !title.trim().isEmpty() &&
               !dataPoints.isEmpty();
    }
    
    public static boolean isValidChartType(String chartType) {
        return "bar".equalsIgnoreCase(chartType) ||
               "pie".equalsIgnoreCase(chartType) ||
               "line".equalsIgnoreCase(chartType) ||
               "scatter".equalsIgnoreCase(chartType);
    }
    
    public int getDataPointCount() {
        return dataPoints.size();
    }
    
    public double getMaxValue() {
        return dataPoints.stream()
                .mapToDouble(DataPoint::getValue)
                .max()
                .orElse(0.0);
    }
    
    public double getMinValue() {
        return dataPoints.stream()
                .mapToDouble(DataPoint::getValue)
                .min()
                .orElse(0.0);
    }
    
    @Override
    public String toString() {
        return String.format("ChartDTO{type='%s', title='%s', dataPoints=%d}",
                chartType, title, dataPoints.size());
    }
    
    // Inner class for data points
    public static class DataPoint {
        private String label;
        private double value;
        private String category;
        
        public DataPoint(String label, double value) {
            this(label, value, null);
        }
        
        public DataPoint(String label, double value, String category) {
            this.label = label;
            this.value = value;
            this.category = category;
        }
        
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        @Override
        public String toString() {
            return String.format("DataPoint{label='%s', value=%.2f, category='%s'}", 
                    label, value, category);
        }
    }
} 